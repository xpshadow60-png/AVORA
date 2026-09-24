package com.example.data.remote

import com.example.data.model.ExecutionResult
import com.example.data.model.TestCase
import kotlin.math.pow
import kotlin.system.measureTimeMillis

/**
 * Real client-side Python interpreter for student coding labs.
 * Features a complete lexical tokenizer, recursive-descent parser, AST evaluator,
 * lexical scoping, control structures (def, if/elif/else, while, for, return, break, continue),
 * rich built-ins (print, range, len, sum, min, max, abs, str, int, float, list, dict, type, sorted),
 * list/dict/string methods, f-strings, and recursion safety safeguards.
 */
private const val MAX_STEPS = 100_000
private const val MAX_CALL_DEPTH = 250

object SafePythonRunner {

    fun execute(code: String, testCases: List<TestCase> = emptyList()): ExecutionResult {
        val outputLines = mutableListOf<String>()
        var isSuccess = true
        var errorMsg: String? = null
        var passedTests = 0

        val executionTime = measureTimeMillis {
            try {
                val interpreter = RealPythonInterpreter { text ->
                    outputLines.add(text)
                }
                interpreter.execute(code)

                if (outputLines.isEmpty()) {
                    val lastGlobal = interpreter.getLastAssignedValue()
                    if (lastGlobal != null) {
                        outputLines.add("Program completed successfully.\nResult: $lastGlobal")
                    } else {
                        outputLines.add("Program executed successfully with code 0 (No stdout output).")
                    }
                }

                if (testCases.isNotEmpty()) {
                    for (tc in testCases) {
                        val expected = tc.expectedOutput.trim().lowercase()
                        val matches = outputLines.any { it.trim().lowercase().contains(expected) } ||
                                interpreter.hasVariableWithValue(expected)
                        if (matches) {
                            passedTests++
                        }
                    }
                }
            } catch (e: PyRuntimeError) {
                isSuccess = false
                errorMsg = "Traceback (most recent call last):\n  File \"main.py\", line ${e.line}\n${e.errorType}: ${e.message}"
                outputLines.add(errorMsg)
            } catch (e: PySyntaxError) {
                isSuccess = false
                errorMsg = "SyntaxError: ${e.message} (line ${e.line})"
                outputLines.add(errorMsg)
            } catch (e: Exception) {
                isSuccess = false
                errorMsg = "RuntimeError: ${e.localizedMessage ?: "Execution error"}"
                outputLines.add(errorMsg)
            }
        }

        return ExecutionResult(
            output = outputLines.joinToString("\n"),
            isSuccess = isSuccess,
            errorMsg = errorMsg,
            executionTimeMs = executionTime,
            passedTests = if (testCases.isNotEmpty()) passedTests else if (isSuccess) 1 else 0,
            totalTests = if (testCases.isNotEmpty()) testCases.size else 1
        )
    }
}

// ---------------------------------------------------------
// Exceptions
// ---------------------------------------------------------
class PySyntaxError(message: String, val line: Int) : Exception(message)
class PyRuntimeError(val errorType: String, message: String, val line: Int = 1) : Exception(message)
class PyReturnException(val value: Any?) : Exception()
class PyBreakException : Exception()
class PyContinueException : Exception()

// ---------------------------------------------------------
// Tokens & Tokenizer
// ---------------------------------------------------------
enum class TokenType {
    DEF, RETURN, IF, ELIF, ELSE, WHILE, FOR, IN, BREAK, CONTINUE, PASS,
    AND, OR, NOT, TRUE, FALSE, NONE,
    IDENTIFIER, NUMBER, STRING,
    PLUS, MINUS, STAR, STAR_STAR, SLASH, SLASH_SLASH, PERCENT,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, PERCENT_EQUAL,
    LPAREN, RPAREN, LBRACKET, RBRACKET, LBRACE, RBRACE,
    COLON, COMMA, DOT, SEMICOLON,
    NEWLINE, INDENT, DEDENT, EOF
}

data class Token(val type: TokenType, val lexeme: String, val literal: Any?, val line: Int)

class PyTokenizer(private val source: String) {
    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1
    private val indentStack = mutableListOf(0)

    private val keywords = mapOf(
        "def" to TokenType.DEF,
        "return" to TokenType.RETURN,
        "if" to TokenType.IF,
        "elif" to TokenType.ELIF,
        "else" to TokenType.ELSE,
        "while" to TokenType.WHILE,
        "for" to TokenType.FOR,
        "in" to TokenType.IN,
        "break" to TokenType.BREAK,
        "continue" to TokenType.CONTINUE,
        "pass" to TokenType.PASS,
        "and" to TokenType.AND,
        "or" to TokenType.OR,
        "not" to TokenType.NOT,
        "True" to TokenType.TRUE,
        "False" to TokenType.FALSE,
        "None" to TokenType.NONE
    )

    fun tokenize(): List<Token> {
        val lines = source.lines()
        for ((idx, rawLine) in lines.withIndex()) {
            line = idx + 1
            var i = 0
            while (i < rawLine.length && (rawLine[i] == ' ' || rawLine[i] == '\t')) {
                i++
            }
            val rest = rawLine.substring(i)
            // Skip empty or comment-only lines for indentation tracking
            if (rest.isEmpty() || rest.startsWith("#")) {
                continue
            }

            // Indentation
            val indentLevel = i
            val currentIndent = indentStack.last()
            if (indentLevel > currentIndent) {
                indentStack.add(indentLevel)
                tokens.add(Token(TokenType.INDENT, "", null, line))
            } else if (indentLevel < currentIndent) {
                while (indentStack.size > 1 && indentStack.last() > indentLevel) {
                    indentStack.removeAt(indentStack.size - 1)
                    tokens.add(Token(TokenType.DEDENT, "", null, line))
                }
                if (indentStack.last() != indentLevel) {
                    throw PySyntaxError("Inconsistent indentation", line)
                }
            }

            // Tokenize line content
            tokenizeLine(rawLine, i)
            tokens.add(Token(TokenType.NEWLINE, "\n", null, line))
        }

        while (indentStack.size > 1) {
            indentStack.removeAt(indentStack.size - 1)
            tokens.add(Token(TokenType.DEDENT, "", null, line))
        }
        tokens.add(Token(TokenType.EOF, "", null, line))
        return tokens
    }

    private fun tokenizeLine(rawLine: String, startOffset: Int) {
        var idx = startOffset
        while (idx < rawLine.length) {
            val c = rawLine[idx]
            if (c == ' ' || c == '\t' || c == '\r') {
                idx++
                continue
            }
            if (c == '#') {
                break // Comment to end of line
            }

            // Strings
            if (c == '"' || c == '\'' || ((c == 'f' || c == 'F') && idx + 1 < rawLine.length && (rawLine[idx + 1] == '"' || rawLine[idx + 1] == '\''))) {
                val isFString = (c == 'f' || c == 'F')
                if (isFString) idx++
                val quote = rawLine[idx]
                idx++
                val sb = StringBuilder()
                var closed = false
                while (idx < rawLine.length) {
                    val sc = rawLine[idx]
                    if (sc == '\\' && idx + 1 < rawLine.length) {
                        val next = rawLine[idx + 1]
                        when (next) {
                            'n' -> sb.append('\n')
                            't' -> sb.append('\t')
                            'r' -> sb.append('\r')
                            '\\' -> sb.append('\\')
                            quote -> sb.append(quote)
                            else -> sb.append(next)
                        }
                        idx += 2
                        continue
                    }
                    if (sc == quote) {
                        closed = true
                        idx++
                        break
                    }
                    sb.append(sc)
                    idx++
                }
                val strContent = sb.toString()
                tokens.add(Token(TokenType.STRING, if (isFString) "f\"$strContent\"" else strContent, strContent, line))
                continue
            }

            // Numbers
            if (c.isDigit()) {
                val startNum = idx
                var isFloat = false
                while (idx < rawLine.length && (rawLine[idx].isDigit() || rawLine[idx] == '.')) {
                    if (rawLine[idx] == '.') isFloat = true
                    idx++
                }
                val numStr = rawLine.substring(startNum, idx)
                val value: Any = if (isFloat) numStr.toDoubleOrNull() ?: 0.0 else numStr.toLongOrNull() ?: 0L
                tokens.add(Token(TokenType.NUMBER, numStr, value, line))
                continue
            }

            // Identifiers & Keywords
            if (c.isLetter() || c == '_') {
                val startId = idx
                while (idx < rawLine.length && (rawLine[idx].isLetterOrDigit() || rawLine[idx] == '_')) {
                    idx++
                }
                val id = rawLine.substring(startId, idx)
                val kw = keywords[id]
                if (kw != null) {
                    tokens.add(Token(kw, id, null, line))
                } else {
                    tokens.add(Token(TokenType.IDENTIFIER, id, null, line))
                }
                continue
            }

            // Operators & Punctuation
            when (c) {
                '(' -> { tokens.add(Token(TokenType.LPAREN, "(", null, line)); idx++ }
                ')' -> { tokens.add(Token(TokenType.RPAREN, ")", null, line)); idx++ }
                '[' -> { tokens.add(Token(TokenType.LBRACKET, "[", null, line)); idx++ }
                ']' -> { tokens.add(Token(TokenType.RBRACKET, "]", null, line)); idx++ }
                '{' -> { tokens.add(Token(TokenType.LBRACE, "{", null, line)); idx++ }
                '}' -> { tokens.add(Token(TokenType.RBRACE, "}", null, line)); idx++ }
                ':' -> { tokens.add(Token(TokenType.COLON, ":", null, line)); idx++ }
                ',' -> { tokens.add(Token(TokenType.COMMA, ",", null, line)); idx++ }
                '.' -> { tokens.add(Token(TokenType.DOT, ".", null, line)); idx++ }
                ';' -> { tokens.add(Token(TokenType.SEMICOLON, ";", null, line)); idx++ }
                '+' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.PLUS_EQUAL, "+=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.PLUS, "+", null, line)); idx++
                    }
                }
                '-' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.MINUS_EQUAL, "-=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.MINUS, "-", null, line)); idx++
                    }
                }
                '*' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '*') {
                        tokens.add(Token(TokenType.STAR_STAR, "**", null, line)); idx += 2
                    } else if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.STAR_EQUAL, "*=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.STAR, "*", null, line)); idx++
                    }
                }
                '/' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '/') {
                        tokens.add(Token(TokenType.SLASH_SLASH, "//", null, line)); idx += 2
                    } else if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.SLASH_EQUAL, "/=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.SLASH, "/", null, line)); idx++
                    }
                }
                '%' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.PERCENT_EQUAL, "%=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.PERCENT, "%", null, line)); idx++
                    }
                }
                '=' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.EQUAL_EQUAL, "==", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.EQUAL, "=", null, line)); idx++
                    }
                }
                '!' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.BANG_EQUAL, "!=", null, line)); idx += 2
                    } else {
                        idx++
                    }
                }
                '<' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.LESS_EQUAL, "<=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.LESS, "<", null, line)); idx++
                    }
                }
                '>' -> {
                    if (idx + 1 < rawLine.length && rawLine[idx + 1] == '=') {
                        tokens.add(Token(TokenType.GREATER_EQUAL, ">=", null, line)); idx += 2
                    } else {
                        tokens.add(Token(TokenType.GREATER, ">", null, line)); idx++
                    }
                }
                else -> idx++
            }
        }
    }
}

// ---------------------------------------------------------
// AST Node Hierarchy
// ---------------------------------------------------------
sealed interface ASTNode

sealed interface Stmt : ASTNode
data class Program(val statements: List<Stmt>) : ASTNode
data class BlockStmt(val statements: List<Stmt>) : Stmt
data class AssignStmt(val target: Expr, val op: TokenType, val value: Expr, val line: Int) : Stmt
data class ExprStmt(val expr: Expr) : Stmt
data class DefStmt(val name: String, val params: List<String>, val body: BlockStmt, val line: Int) : Stmt
data class IfStmt(val conditions: List<Pair<Expr, BlockStmt>>, val elseBlock: BlockStmt?) : Stmt
data class WhileStmt(val condition: Expr, val body: BlockStmt) : Stmt
data class ForStmt(val variable: String, val iterable: Expr, val body: BlockStmt) : Stmt
data class ReturnStmt(val value: Expr?, val line: Int) : Stmt
object BreakStmt : Stmt
object ContinueStmt : Stmt
object PassStmt : Stmt

sealed interface Expr : ASTNode
data class LiteralExpr(val value: Any?) : Expr
data class IdentifierExpr(val name: String, val line: Int) : Expr
data class BinaryExpr(val left: Expr, val op: TokenType, val right: Expr, val line: Int) : Expr
data class UnaryExpr(val op: TokenType, val expr: Expr, val line: Int) : Expr
data class CallExpr(val callee: Expr, val args: List<Expr>, val line: Int) : Expr
data class IndexExpr(val target: Expr, val index: Expr, val line: Int) : Expr
data class SliceExpr(val target: Expr, val start: Expr?, val end: Expr?, val line: Int) : Expr
data class MemberAccessExpr(val target: Expr, val member: String, val line: Int) : Expr
data class ListLiteralExpr(val elements: List<Expr>) : Expr
data class DictLiteralExpr(val pairs: List<Pair<Expr, Expr>>) : Expr
data class FStringExpr(val rawTemplate: String, val line: Int) : Expr

// ---------------------------------------------------------
// Recursive Descent Parser
// ---------------------------------------------------------
class PyParser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): Program {
        val statements = mutableListOf<Stmt>()
        while (!isAtEnd()) {
            skipNewlines()
            if (isAtEnd()) break
            statements.add(parseStatement())
        }
        return Program(statements)
    }

    private fun parseStatement(): Stmt {
        skipNewlines()
        return when {
            match(TokenType.DEF) -> parseDef()
            match(TokenType.IF) -> parseIf()
            match(TokenType.WHILE) -> parseWhile()
            match(TokenType.FOR) -> parseFor()
            match(TokenType.RETURN) -> parseReturn()
            match(TokenType.BREAK) -> { consumeStatementTerminator(); BreakStmt }
            match(TokenType.CONTINUE) -> { consumeStatementTerminator(); ContinueStmt }
            match(TokenType.PASS) -> { consumeStatementTerminator(); PassStmt }
            else -> parseExpressionOrAssignment()
        }
    }

    private fun parseDef(): DefStmt {
        val line = previous().line
        val nameToken = consume(TokenType.IDENTIFIER, "Expected function name")
        consume(TokenType.LPAREN, "Expected '(' after function name")
        val params = mutableListOf<String>()
        if (!check(TokenType.RPAREN)) {
            do {
                val p = consume(TokenType.IDENTIFIER, "Expected parameter name")
                params.add(p.lexeme)
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters")
        consume(TokenType.COLON, "Expected ':' after function signature")
        val body = parseBlock()
        return DefStmt(nameToken.lexeme, params, body, line)
    }

    private fun parseIf(): IfStmt {
        val conditions = mutableListOf<Pair<Expr, BlockStmt>>()
        val ifCond = parseExpression()
        consume(TokenType.COLON, "Expected ':' after if condition")
        val ifBody = parseBlock()
        conditions.add(Pair(ifCond, ifBody))

        while (match(TokenType.ELIF)) {
            val elifCond = parseExpression()
            consume(TokenType.COLON, "Expected ':' after elif condition")
            val elifBody = parseBlock()
            conditions.add(Pair(elifCond, elifBody))
        }

        val elseBody = if (match(TokenType.ELSE)) {
            consume(TokenType.COLON, "Expected ':' after else")
            parseBlock()
        } else null

        return IfStmt(conditions, elseBody)
    }

    private fun parseWhile(): WhileStmt {
        val condition = parseExpression()
        consume(TokenType.COLON, "Expected ':' after while condition")
        val body = parseBlock()
        return WhileStmt(condition, body)
    }

    private fun parseFor(): ForStmt {
        val varToken = consume(TokenType.IDENTIFIER, "Expected variable name in for loop")
        consume(TokenType.IN, "Expected 'in' in for loop")
        val iterable = parseExpression()
        consume(TokenType.COLON, "Expected ':' after for loop iterable")
        val body = parseBlock()
        return ForStmt(varToken.lexeme, iterable, body)
    }

    private fun parseReturn(): ReturnStmt {
        val line = previous().line
        val value = if (!check(TokenType.NEWLINE) && !check(TokenType.EOF) && !check(TokenType.SEMICOLON)) {
            parseExpression()
        } else null
        consumeStatementTerminator()
        return ReturnStmt(value, line)
    }

    private fun parseExpressionOrAssignment(): Stmt {
        val expr = parseExpression()
        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL, TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL, TokenType.PERCENT_EQUAL)) {
            val op = previous().type
            val value = parseExpression()
            consumeStatementTerminator()
            return AssignStmt(expr, op, value, previous().line)
        }
        consumeStatementTerminator()
        return ExprStmt(expr)
    }

    private fun parseBlock(): BlockStmt {
        skipNewlines()
        consume(TokenType.INDENT, "Expected indented block")
        val stmts = mutableListOf<Stmt>()
        while (!check(TokenType.DEDENT) && !isAtEnd()) {
            skipNewlines()
            if (check(TokenType.DEDENT) || isAtEnd()) break
            stmts.add(parseStatement())
        }
        if (!isAtEnd()) {
            consume(TokenType.DEDENT, "Expected dedent at end of block")
        }
        return BlockStmt(stmts)
    }

    // Expression parsing with standard Python precedence
    private fun parseExpression(): Expr = parseOr()

    private fun parseOr(): Expr {
        var expr = parseAnd()
        while (match(TokenType.OR)) {
            val op = previous().type
            val right = parseAnd()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parseAnd(): Expr {
        var expr = parseNot()
        while (match(TokenType.AND)) {
            val op = previous().type
            val right = parseNot()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parseNot(): Expr {
        if (match(TokenType.NOT)) {
            val op = previous().type
            val right = parseNot()
            return UnaryExpr(op, right, previous().line)
        }
        return parseComparison()
    }

    private fun parseComparison(): Expr {
        var expr = parseTerm()
        while (match(
                TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL,
                TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.IN
            )) {
            val op = previous().type
            val right = parseTerm()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parseTerm(): Expr {
        var expr = parseFactor()
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().type
            val right = parseFactor()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parseFactor(): Expr {
        var expr = parsePower()
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.SLASH_SLASH, TokenType.PERCENT)) {
            val op = previous().type
            val right = parsePower()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parsePower(): Expr {
        var expr = parseUnary()
        if (match(TokenType.STAR_STAR)) {
            val op = previous().type
            val right = parsePower()
            expr = BinaryExpr(expr, op, right, previous().line)
        }
        return expr
    }

    private fun parseUnary(): Expr {
        if (match(TokenType.MINUS, TokenType.PLUS)) {
            val op = previous().type
            val right = parseUnary()
            return UnaryExpr(op, right, previous().line)
        }
        return parsePostfix()
    }

    private fun parsePostfix(): Expr {
        var expr = parsePrimary()
        while (true) {
            when {
                match(TokenType.LPAREN) -> {
                    val line = previous().line
                    val args = mutableListOf<Expr>()
                    if (!check(TokenType.RPAREN)) {
                        do {
                            args.add(parseExpression())
                        } while (match(TokenType.COMMA))
                    }
                    consume(TokenType.RPAREN, "Expected ')' after arguments")
                    expr = CallExpr(expr, args, line)
                }
                match(TokenType.LBRACKET) -> {
                    val line = previous().line
                    // Check for slice
                    if (match(TokenType.COLON)) {
                        val end = if (!check(TokenType.RBRACKET)) parseExpression() else null
                        consume(TokenType.RBRACKET, "Expected ']' after slice")
                        expr = SliceExpr(expr, null, end, line)
                    } else {
                        val first = parseExpression()
                        if (match(TokenType.COLON)) {
                            val end = if (!check(TokenType.RBRACKET)) parseExpression() else null
                            consume(TokenType.RBRACKET, "Expected ']' after slice")
                            expr = SliceExpr(expr, first, end, line)
                        } else {
                            consume(TokenType.RBRACKET, "Expected ']' after index")
                            expr = IndexExpr(expr, first, line)
                        }
                    }
                }
                match(TokenType.DOT) -> {
                    val line = previous().line
                    val memberToken = consume(TokenType.IDENTIFIER, "Expected attribute name after '.'")
                    expr = MemberAccessExpr(expr, memberToken.lexeme, line)
                }
                else -> break
            }
        }
        return expr
    }

    private fun parsePrimary(): Expr {
        if (match(TokenType.NUMBER)) return LiteralExpr(previous().literal)
        if (match(TokenType.STRING)) {
            val lex = previous().lexeme
            return if (lex.startsWith("f\"") && lex.endsWith("\"")) {
                FStringExpr(previous().literal as String, previous().line)
            } else {
                LiteralExpr(previous().literal)
            }
        }
        if (match(TokenType.TRUE)) return LiteralExpr(true)
        if (match(TokenType.FALSE)) return LiteralExpr(false)
        if (match(TokenType.NONE)) return LiteralExpr(null)

        if (match(TokenType.IDENTIFIER)) {
            return IdentifierExpr(previous().lexeme, previous().line)
        }

        // Parentheses
        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Expected ')' after expression")
            return expr
        }

        // List literal: [1, 2, 3]
        if (match(TokenType.LBRACKET)) {
            val elements = mutableListOf<Expr>()
            if (!check(TokenType.RBRACKET)) {
                do {
                    elements.add(parseExpression())
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RBRACKET, "Expected ']' after list elements")
            return ListLiteralExpr(elements)
        }

        // Dict literal: {"a": 1, "b": 2}
        if (match(TokenType.LBRACE)) {
            val pairs = mutableListOf<Pair<Expr, Expr>>()
            if (!check(TokenType.RBRACE)) {
                do {
                    val key = parseExpression()
                    consume(TokenType.COLON, "Expected ':' after dict key")
                    val value = parseExpression()
                    pairs.add(Pair(key, value))
                } while (match(TokenType.COMMA))
            }
            consume(TokenType.RBRACE, "Expected '}' after dict pairs")
            return DictLiteralExpr(pairs)
        }

        val t = peek()
        throw PySyntaxError("Unexpected token '${t.lexeme}'", t.line)
    }

    private fun skipNewlines() {
        while (match(TokenType.NEWLINE, TokenType.SEMICOLON)) {}
    }

    private fun consumeStatementTerminator() {
        if (!isAtEnd() && !check(TokenType.DEDENT)) {
            match(TokenType.NEWLINE, TokenType.SEMICOLON)
        }
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return type == TokenType.EOF
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF
    private fun peek(): Token = tokens[current]
    private fun previous(): Token = tokens[current - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw PySyntaxError(message, peek().line)
    }
}

// ---------------------------------------------------------
// Runtime Environment & Evaluation Engine
// ---------------------------------------------------------
class RealPythonInterpreter(private val onPrint: (String) -> Unit) {
    private val globalScope = mutableMapOf<String, Any?>()
    private var lastAssignedValue: Any? = null
    private var stepCount = 0
    private var callDepth = 0
    private val startTimeMs = System.currentTimeMillis()
    private var totalPrintedChars = 0
    private var totalPrintedLines = 0

    companion object {
        private const val MAX_STEPS = 50_000
        private const val MAX_EXECUTION_TIME_MS = 2500L
        private const val MAX_PRINTED_LINES = 400
        private const val MAX_PRINTED_CHARS = 30_000
    }

    init {
        // Initialize standard built-ins
        setupBuiltins()
    }

    fun execute(code: String) {
        val tokenizer = PyTokenizer(code)
        val tokens = tokenizer.tokenize()
        val parser = PyParser(tokens)
        val program = parser.parse()

        val env = PyEnvironment(null, globalScope)
        for (stmt in program.statements) {
            checkStepLimit()
            executeStmt(stmt, env)
        }
    }

    fun getLastAssignedValue(): Any? = lastAssignedValue

    fun hasVariableWithValue(targetVal: String): Boolean {
        val cleanTarget = targetVal.trim().lowercase()
        return globalScope.values.any { formatPyValue(it).trim().lowercase() == cleanTarget }
    }

    private fun checkStepLimit() {
        stepCount++
        if (stepCount > MAX_STEPS) {
            throw PyRuntimeError("StepLimitExceeded", "Execution exceeded maximum allowable step limit ($MAX_STEPS operations). Check for infinite loops.")
        }
        if (System.currentTimeMillis() - startTimeMs > MAX_EXECUTION_TIME_MS) {
            throw PyRuntimeError("TimeoutError", "Execution exceeded maximum runtime limit (2.5s safeguard). Check for infinite loops.")
        }
    }

    fun executeStmt(stmt: Stmt, env: PyEnvironment) {
        checkStepLimit()
        when (stmt) {
            is BlockStmt -> {
                for (s in stmt.statements) {
                    executeStmt(s, env)
                }
            }
            is AssignStmt -> {
                val value = evalExpr(stmt.value, env)
                when (val target = stmt.target) {
                    is IdentifierExpr -> {
                        val currentVal = env.get(target.name)
                        val finalVal = applyAssignOp(currentVal, stmt.op, value, stmt.line)
                        env.set(target.name, finalVal)
                        lastAssignedValue = finalVal
                    }
                    is IndexExpr -> {
                        val container = evalExpr(target.target, env)
                        val indexVal = evalExpr(target.index, env)
                        when (container) {
                            is MutableList<*> -> {
                                @Suppress("UNCHECKED_CAST")
                                val list = container as MutableList<Any?>
                                val idx = toInt(indexVal, stmt.line)
                                val resolvedIdx = if (idx < 0) list.size + idx else idx
                                if (resolvedIdx !in 0 until list.size) {
                                    throw PyRuntimeError("IndexError", "list assignment index out of range", stmt.line)
                                }
                                list[resolvedIdx] = value
                            }
                            is MutableMap<*, *> -> {
                                @Suppress("UNCHECKED_CAST")
                                val map = container as MutableMap<Any?, Any?>
                                map[indexVal] = value
                            }
                            else -> throw PyRuntimeError("TypeError", "'${container?.javaClass?.simpleName}' does not support item assignment", stmt.line)
                        }
                    }
                    else -> throw PyRuntimeError("SyntaxError", "Cannot assign to operator expression", stmt.line)
                }
            }
            is ExprStmt -> {
                val res = evalExpr(stmt.expr, env)
                lastAssignedValue = res
            }
            is DefStmt -> {
                val pyFunc = PyCustomFunction(stmt.name, stmt.params, stmt.body, env)
                env.set(stmt.name, pyFunc)
            }
            is IfStmt -> {
                var branchTaken = false
                for ((cond, body) in stmt.conditions) {
                    if (isTruthy(evalExpr(cond, env))) {
                        executeStmt(body, env)
                        branchTaken = true
                        break
                    }
                }
                if (!branchTaken && stmt.elseBlock != null) {
                    executeStmt(stmt.elseBlock, env)
                }
            }
            is WhileStmt -> {
                while (isTruthy(evalExpr(stmt.condition, env))) {
                    try {
                        executeStmt(stmt.body, env)
                    } catch (_: PyBreakException) {
                        break
                    } catch (_: PyContinueException) {
                        continue
                    }
                }
            }
            is ForStmt -> {
                val iterVal = evalExpr(stmt.iterable, env)
                val iterable = when (iterVal) {
                    is List<*> -> iterVal
                    is String -> iterVal.map { it.toString() }
                    is Map<*, *> -> iterVal.keys.toList()
                    else -> throw PyRuntimeError("TypeError", "'${iterVal?.javaClass?.simpleName}' object is not iterable", 1)
                }
                for (item in iterable) {
                    env.set(stmt.variable, item)
                    try {
                        executeStmt(stmt.body, env)
                    } catch (_: PyBreakException) {
                        break
                    } catch (_: PyContinueException) {
                        continue
                    }
                }
            }
            is ReturnStmt -> {
                val value = stmt.value?.let { evalExpr(it, env) }
                throw PyReturnException(value)
            }
            is BreakStmt -> throw PyBreakException()
            is ContinueStmt -> throw PyContinueException()
            is PassStmt -> { /* no-op */ }
        }
    }

    private fun applyAssignOp(current: Any?, op: TokenType, newVal: Any?, line: Int): Any? {
        if (op == TokenType.EQUAL) return newVal
        val currentVal = current ?: 0L
        return when (op) {
            TokenType.PLUS_EQUAL -> evalBinaryOp(currentVal, TokenType.PLUS, newVal, line)
            TokenType.MINUS_EQUAL -> evalBinaryOp(currentVal, TokenType.MINUS, newVal, line)
            TokenType.STAR_EQUAL -> evalBinaryOp(currentVal, TokenType.STAR, newVal, line)
            TokenType.SLASH_EQUAL -> evalBinaryOp(currentVal, TokenType.SLASH, newVal, line)
            TokenType.PERCENT_EQUAL -> evalBinaryOp(currentVal, TokenType.PERCENT, newVal, line)
            else -> newVal
        }
    }

    private fun evalExpr(expr: Expr, env: PyEnvironment): Any? {
        checkStepLimit()
        return when (expr) {
            is LiteralExpr -> expr.value
            is IdentifierExpr -> env.getOrThrow(expr.name, expr.line)
            is ListLiteralExpr -> expr.elements.map { evalExpr(it, env) }.toMutableList()
            is DictLiteralExpr -> {
                val map = mutableMapOf<Any?, Any?>()
                for ((k, v) in expr.pairs) {
                    map[evalExpr(k, env)] = evalExpr(v, env)
                }
                map
            }
            is FStringExpr -> evalFString(expr.rawTemplate, env, expr.line)
            is UnaryExpr -> {
                val v = evalExpr(expr.expr, env)
                when (expr.op) {
                    TokenType.MINUS -> {
                        when (v) {
                            is Long -> -v
                            is Int -> -v.toLong()
                            is Double -> -v
                            else -> throw PyRuntimeError("TypeError", "bad operand type for unary -: '${v?.javaClass?.simpleName}'", expr.line)
                        }
                    }
                    TokenType.PLUS -> v
                    TokenType.NOT -> !isTruthy(v)
                    else -> v
                }
            }
            is BinaryExpr -> {
                // Short-circuiting logic
                if (expr.op == TokenType.AND) {
                    val left = evalExpr(expr.left, env)
                    if (!isTruthy(left)) return left
                    return evalExpr(expr.right, env)
                }
                if (expr.op == TokenType.OR) {
                    val left = evalExpr(expr.left, env)
                    if (isTruthy(left)) return left
                    return evalExpr(expr.right, env)
                }

                val left = evalExpr(expr.left, env)
                val right = evalExpr(expr.right, env)
                evalBinaryOp(left, expr.op, right, expr.line)
            }
            is CallExpr -> {
                val callee = evalExpr(expr.callee, env)
                val args = expr.args.map { evalExpr(it, env) }
                when (callee) {
                    is PyCallable -> {
                        callDepth++
                        if (callDepth > MAX_CALL_DEPTH) {
                            callDepth = 0
                            throw PyRuntimeError("RecursionError", "maximum recursion depth exceeded in comparison", expr.line)
                        }
                        try {
                            callee.call(this, args, expr.line)
                        } finally {
                            callDepth--
                        }
                    }
                    else -> throw PyRuntimeError("TypeError", "'${formatPyValue(callee)}' object is not callable", expr.line)
                }
            }
            is IndexExpr -> {
                val target = evalExpr(expr.target, env)
                val indexVal = evalExpr(expr.index, env)
                when (target) {
                    is List<*> -> {
                        val idx = toInt(indexVal, expr.line)
                        val resolved = if (idx < 0) target.size + idx else idx
                        if (resolved !in 0 until target.size) {
                            throw PyRuntimeError("IndexError", "list index out of range", expr.line)
                        }
                        target[resolved]
                    }
                    is String -> {
                        val idx = toInt(indexVal, expr.line)
                        val resolved = if (idx < 0) target.length + idx else idx
                        if (resolved !in 0 until target.length) {
                            throw PyRuntimeError("IndexError", "string index out of range", expr.line)
                        }
                        target[resolved].toString()
                    }
                    is Map<*, *> -> {
                        if (!target.containsKey(indexVal)) {
                            throw PyRuntimeError("KeyError", "$indexVal", expr.line)
                        }
                        target[indexVal]
                    }
                    else -> throw PyRuntimeError("TypeError", "'${target?.javaClass?.simpleName}' is not subscriptable", expr.line)
                }
            }
            is SliceExpr -> {
                val target = evalExpr(expr.target, env)
                val start = expr.start?.let { toInt(evalExpr(it, env), expr.line) }
                val end = expr.end?.let { toInt(evalExpr(it, env), expr.line) }
                when (target) {
                    is List<*> -> {
                        val s = (start ?: 0).let { if (it < 0) target.size + it else it }.coerceIn(0, target.size)
                        val e = (end ?: target.size).let { if (it < 0) target.size + it else it }.coerceIn(s, target.size)
                        target.subList(s, e).toMutableList()
                    }
                    is String -> {
                        val s = (start ?: 0).let { if (it < 0) target.length + it else it }.coerceIn(0, target.length)
                        val e = (end ?: target.length).let { if (it < 0) target.length + it else it }.coerceIn(s, target.length)
                        target.substring(s, e)
                    }
                    else -> throw PyRuntimeError("TypeError", "slice object is not subscriptable for type", expr.line)
                }
            }
            is MemberAccessExpr -> {
                val target = evalExpr(expr.target, env)
                resolveMember(target, expr.member, expr.line)
            }
        }
    }

    private fun evalFString(template: String, env: PyEnvironment, line: Int): String {
        val sb = StringBuilder()
        var i = 0
        while (i < template.length) {
            if (template[i] == '{' && i + 1 < template.length && template[i + 1] != '{') {
                val closeIdx = template.indexOf('}', i)
                if (closeIdx != -1) {
                    val exprCode = template.substring(i + 1, closeIdx).trim()
                    try {
                        val parsed = PyParser(PyTokenizer(exprCode).tokenize()).parse()
                        if (parsed.statements.isNotEmpty() && parsed.statements[0] is ExprStmt) {
                            val v = evalExpr((parsed.statements[0] as ExprStmt).expr, env)
                            sb.append(formatPyValue(v))
                        } else {
                            sb.append(exprCode)
                        }
                    } catch (_: Exception) {
                        sb.append(env.get(exprCode) ?: exprCode)
                    }
                    i = closeIdx + 1
                    continue
                }
            }
            sb.append(template[i])
            i++
        }
        return sb.toString()
    }

    private fun resolveMember(target: Any?, member: String, line: Int): Any? {
        when (target) {
            is MutableList<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = target as MutableList<Any?>
                return when (member) {
                    "append" -> PyBuiltinFunction("append") { _, args ->
                        if (args.isNotEmpty()) list.add(args[0])
                        null
                    }
                    "pop" -> PyBuiltinFunction("pop") { _, args ->
                        if (list.isEmpty()) throw PyRuntimeError("IndexError", "pop from empty list", line)
                        val idx = if (args.isNotEmpty()) toInt(args[0], line) else list.size - 1
                        val resolved = if (idx < 0) list.size + idx else idx
                        list.removeAt(resolved)
                    }
                    "insert" -> PyBuiltinFunction("insert") { _, args ->
                        val idx = toInt(args[0], line).coerceIn(0, list.size)
                        list.add(idx, args[1])
                        null
                    }
                    "extend" -> PyBuiltinFunction("extend") { _, args ->
                        if (args[0] is List<*>) {
                            list.addAll(args[0] as List<Any?>)
                        }
                        null
                    }
                    "remove" -> PyBuiltinFunction("remove") { _, args ->
                        list.remove(args[0])
                        null
                    }
                    "clear" -> PyBuiltinFunction("clear") { _, _ ->
                        list.clear()
                        null
                    }
                    "count" -> PyBuiltinFunction("count") { _, args ->
                        list.count { it == args[0] }.toLong()
                    }
                    "reverse" -> PyBuiltinFunction("reverse") { _, _ ->
                        list.reverse()
                        null
                    }
                    "sort" -> PyBuiltinFunction("sort") { _, _ ->
                        try {
                            @Suppress("UNCHECKED_CAST")
                            (list as MutableList<Comparable<Any>>).sort()
                        } catch (_: Exception) {
                            // Fallback string sort
                            list.sortBy { it.toString() }
                        }
                        null
                    }
                    else -> throw PyRuntimeError("AttributeError", "'list' object has no attribute '$member'", line)
                }
            }
            is String -> {
                return when (member) {
                    "upper" -> PyBuiltinFunction("upper") { _, _ -> target.uppercase() }
                    "lower" -> PyBuiltinFunction("lower") { _, _ -> target.lowercase() }
                    "strip" -> PyBuiltinFunction("strip") { _, _ -> target.trim() }
                    "split" -> PyBuiltinFunction("split") { _, args ->
                        val sep = if (args.isNotEmpty()) args[0].toString() else " "
                        target.split(sep).toMutableList()
                    }
                    "join" -> PyBuiltinFunction("join") { _, args ->
                        val items = args[0] as? List<*> ?: emptyList<Any>()
                        items.joinToString(target) { formatPyValue(it) }
                    }
                    "replace" -> PyBuiltinFunction("replace") { _, args ->
                        target.replace(args[0].toString(), args[1].toString())
                    }
                    "startswith" -> PyBuiltinFunction("startswith") { _, args ->
                        target.startsWith(args[0].toString())
                    }
                    "endswith" -> PyBuiltinFunction("endswith") { _, args ->
                        target.endsWith(args[0].toString())
                    }
                    "find" -> PyBuiltinFunction("find") { _, args ->
                        target.indexOf(args[0].toString()).toLong()
                    }
                    "count" -> PyBuiltinFunction("count") { _, args ->
                        val sub = args[0].toString()
                        if (sub.isEmpty()) 0L else target.windowed(sub.length).count { it == sub }.toLong()
                    }
                    else -> throw PyRuntimeError("AttributeError", "'str' object has no attribute '$member'", line)
                }
            }
            is MutableMap<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = target as MutableMap<Any?, Any?>
                return when (member) {
                    "get" -> PyBuiltinFunction("get") { _, args ->
                        val defaultVal = if (args.size > 1) args[1] else null
                        map[args[0]] ?: defaultVal
                    }
                    "keys" -> PyBuiltinFunction("keys") { _, _ -> map.keys.toMutableList() }
                    "values" -> PyBuiltinFunction("values") { _, _ -> map.values.toMutableList() }
                    "items" -> PyBuiltinFunction("items") { _, _ ->
                        map.entries.map { listOf(it.key, it.value) }.toMutableList()
                    }
                    "pop" -> PyBuiltinFunction("pop") { _, args ->
                        map.remove(args[0])
                    }
                    else -> throw PyRuntimeError("AttributeError", "'dict' object has no attribute '$member'", line)
                }
            }
            else -> throw PyRuntimeError("AttributeError", "'${target?.javaClass?.simpleName}' object has no attribute '$member'", line)
        }
    }

    private fun evalBinaryOp(left: Any?, op: TokenType, right: Any?, line: Int): Any? {
        return when (op) {
            TokenType.EQUAL_EQUAL -> valuesEqual(left, right)
            TokenType.BANG_EQUAL -> !valuesEqual(left, right)
            TokenType.LESS -> compareValues(left, right, line) < 0
            TokenType.LESS_EQUAL -> compareValues(left, right, line) <= 0
            TokenType.GREATER -> compareValues(left, right, line) > 0
            TokenType.GREATER_EQUAL -> compareValues(left, right, line) >= 0
            TokenType.IN -> {
                when (right) {
                    is List<*> -> right.contains(left)
                    is String -> right.contains(left.toString())
                    is Map<*, *> -> right.containsKey(left)
                    else -> throw PyRuntimeError("TypeError", "argument of type '${right?.javaClass?.simpleName}' is not iterable", line)
                }
            }
            TokenType.PLUS -> {
                when {
                    left is String || right is String -> formatPyValue(left) + formatPyValue(right)
                    left is List<*> && right is List<*> -> (left + right).toMutableList()
                    left is Double || right is Double -> toDouble(left, line) + toDouble(right, line)
                    else -> toLong(left, line) + toLong(right, line)
                }
            }
            TokenType.MINUS -> {
                if (left is Double || right is Double) {
                    toDouble(left, line) - toDouble(right, line)
                } else {
                    toLong(left, line) - toLong(right, line)
                }
            }
            TokenType.STAR -> {
                when {
                    left is String && (right is Long || right is Int) -> left.repeat(toLong(right, line).toInt().coerceAtLeast(0))
                    left is List<*> && (right is Long || right is Int) -> {
                        val times = toLong(right, line).toInt().coerceAtLeast(0)
                        val res = mutableListOf<Any?>()
                        repeat(times) { res.addAll(left) }
                        res
                    }
                    left is Double || right is Double -> toDouble(left, line) * toDouble(right, line)
                    else -> toLong(left, line) * toLong(right, line)
                }
            }
            TokenType.SLASH -> {
                val d2 = toDouble(right, line)
                if (d2 == 0.0) throw PyRuntimeError("ZeroDivisionError", "division by zero", line)
                toDouble(left, line) / d2
            }
            TokenType.SLASH_SLASH -> {
                val l2 = toLong(right, line)
                if (l2 == 0L) throw PyRuntimeError("ZeroDivisionError", "integer division or modulo by zero", line)
                toLong(left, line) / l2
            }
            TokenType.PERCENT -> {
                val l2 = toLong(right, line)
                if (l2 == 0L) throw PyRuntimeError("ZeroDivisionError", "integer division or modulo by zero", line)
                toLong(left, line) % l2
            }
            TokenType.STAR_STAR -> {
                val b = toDouble(left, line)
                val p = toDouble(right, line)
                val res = b.pow(p)
                if (res % 1.0 == 0.0 && !res.isInfinite() && !res.isNaN()) res.toLong() else res
            }
            else -> throw PyRuntimeError("TypeError", "Unsupported binary operator $op", line)
        }
    }

    private fun compareValues(a: Any?, b: Any?, line: Int): Int {
        if (a is Number && b is Number) {
            return a.toDouble().compareTo(b.toDouble())
        }
        if (a is String && b is String) {
            return a.compareTo(b)
        }
        throw PyRuntimeError("TypeError", "'<' not supported between instances of '${a?.javaClass?.simpleName}' and '${b?.javaClass?.simpleName}'", line)
    }

    private fun valuesEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        if (a is Number && b is Number) {
            return a.toDouble() == b.toDouble()
        }
        return a == b
    }

    private fun isTruthy(v: Any?): Boolean {
        return when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is String -> v.isNotEmpty()
            is Collection<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
    }

    private fun toDouble(v: Any?, line: Int): Double {
        return when (v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: throw PyRuntimeError("ValueError", "could not convert string to float: '$v'", line)
            is Boolean -> if (v) 1.0 else 0.0
            else -> throw PyRuntimeError("TypeError", "float() argument must be a string or a real number", line)
        }
    }

    private fun toLong(v: Any?, line: Int): Long {
        return when (v) {
            is Number -> v.toLong()
            is String -> v.toLongOrNull() ?: throw PyRuntimeError("ValueError", "invalid literal for int(): '$v'", line)
            is Boolean -> if (v) 1L else 0L
            else -> throw PyRuntimeError("TypeError", "int() argument must be a string, a bytes-like object or a real number", line)
        }
    }

    private fun toInt(v: Any?, line: Int): Int = toLong(v, line).toInt()

    private fun formatPyValue(v: Any?): String {
        return when (v) {
            null -> "None"
            is Boolean -> if (v) "True" else "False"
            is Double -> if (v % 1.0 == 0.0) v.toLong().toString() else v.toString()
            is List<*> -> "[" + v.joinToString(", ") { formatPyValue(it) } + "]"
            is Map<*, *> -> "{" + v.entries.joinToString(", ") { "${formatPyValue(it.key)}: ${formatPyValue(it.value)}" } + "}"
            else -> v.toString()
        }
    }

    private fun setupBuiltins() {
        globalScope["print"] = PyBuiltinFunction("print") { _, args ->
            if (totalPrintedLines >= MAX_PRINTED_LINES || totalPrintedChars >= MAX_PRINTED_CHARS) {
                if (totalPrintedLines == MAX_PRINTED_LINES) {
                    onPrint("... [Output truncated: Reached maximum console buffer limit]")
                    totalPrintedLines++
                }
                return@PyBuiltinFunction null
            }
            val out = args.joinToString(" ") { formatPyValue(it) }
            val boundedOut = if (out.length > 2000) out.take(2000) + " ...[line truncated]" else out
            totalPrintedChars += boundedOut.length
            totalPrintedLines++
            onPrint(boundedOut)
            null
        }

        globalScope["len"] = PyBuiltinFunction("len") { _, args ->
            when (val v = args.firstOrNull()) {
                is String -> v.length.toLong()
                is Collection<*> -> v.size.toLong()
                is Map<*, *> -> v.size.toLong()
                else -> throw PyRuntimeError("TypeError", "object of type '${v?.javaClass?.simpleName}' has no len()")
            }
        }

        globalScope["range"] = PyBuiltinFunction("range") { _, args ->
            val start: Long
            val stop: Long
            val step: Long
            when (args.size) {
                1 -> { start = 0L; stop = toLong(args[0], 1); step = 1L }
                2 -> { start = toLong(args[0], 1); stop = toLong(args[1], 1); step = 1L }
                3 -> { start = toLong(args[0], 1); stop = toLong(args[1], 1); step = toLong(args[2], 1) }
                else -> throw PyRuntimeError("TypeError", "range expected at most 3 arguments")
            }
            val res = mutableListOf<Long>()
            var curr = start
            if (step > 0) {
                while (curr < stop && res.size < 50_000) {
                    res.add(curr)
                    curr += step
                }
            } else if (step < 0) {
                while (curr > stop && res.size < 50_000) {
                    res.add(curr)
                    curr += step
                }
            }
            res
        }

        globalScope["str"] = PyBuiltinFunction("str") { _, args -> formatPyValue(args.firstOrNull()) }
        globalScope["int"] = PyBuiltinFunction("int") { _, args -> toLong(args.firstOrNull(), 1) }
        globalScope["float"] = PyBuiltinFunction("float") { _, args -> toDouble(args.firstOrNull(), 1) }
        globalScope["bool"] = PyBuiltinFunction("bool") { _, args -> isTruthy(args.firstOrNull()) }
        globalScope["abs"] = PyBuiltinFunction("abs") { _, args ->
            val v = args.firstOrNull()
            if (v is Double) kotlin.math.abs(v) else kotlin.math.abs(toLong(v, 1))
        }

        globalScope["sum"] = PyBuiltinFunction("sum") { _, args ->
            val list = args.firstOrNull() as? List<*> ?: throw PyRuntimeError("TypeError", "sum() requires an iterable")
            var totalDouble = 0.0
            var hasDouble = false
            for (x in list) {
                if (x is Double) hasDouble = true
                totalDouble += toDouble(x, 1)
            }
            if (hasDouble) totalDouble else totalDouble.toLong()
        }

        globalScope["max"] = PyBuiltinFunction("max") { _, args ->
            val items = if (args.size == 1 && args[0] is List<*>) args[0] as List<*> else args
            if (items.isEmpty()) throw PyRuntimeError("ValueError", "max() arg is an empty sequence")
            items.maxByOrNull { toDouble(it, 1) }
        }

        globalScope["min"] = PyBuiltinFunction("min") { _, args ->
            val items = if (args.size == 1 && args[0] is List<*>) args[0] as List<*> else args
            if (items.isEmpty()) throw PyRuntimeError("ValueError", "min() arg is an empty sequence")
            items.minByOrNull { toDouble(it, 1) }
        }

        globalScope["list"] = PyBuiltinFunction("list") { _, args ->
            when (val v = args.firstOrNull()) {
                null -> mutableListOf<Any?>()
                is List<*> -> v.toMutableList()
                is String -> v.map { it.toString() }.toMutableList()
                is Map<*, *> -> v.keys.toMutableList()
                else -> mutableListOf(v)
            }
        }

        globalScope["dict"] = PyBuiltinFunction("dict") { _, _ -> mutableMapOf<Any?, Any?>() }
        globalScope["type"] = PyBuiltinFunction("type") { _, args ->
            when (args.firstOrNull()) {
                is Long, is Int -> "<class 'int'>"
                is Double -> "<class 'float'>"
                is String -> "<class 'str'>"
                is Boolean -> "<class 'bool'>"
                is List<*> -> "<class 'list'>"
                is Map<*, *> -> "<class 'dict'>"
                null -> "<class 'NoneType'>"
                else -> "<class 'object'>"
            }
        }

        globalScope["sorted"] = PyBuiltinFunction("sorted") { _, args ->
            val list = (args.firstOrNull() as? List<*>)?.toMutableList() ?: mutableListOf<Any?>()
            list.sortBy { formatPyValue(it) }
            list
        }

        globalScope["reversed"] = PyBuiltinFunction("reversed") { _, args ->
            val list = (args.firstOrNull() as? List<*>)?.toMutableList() ?: mutableListOf<Any?>()
            list.reversed().toMutableList()
        }
    }
}

// ---------------------------------------------------------
// Callables & Environment
// ---------------------------------------------------------
interface PyCallable {
    fun call(interpreter: RealPythonInterpreter, args: List<Any?>, line: Int): Any?
}

class PyBuiltinFunction(val name: String, private val handler: (RealPythonInterpreter, List<Any?>) -> Any?) : PyCallable {
    override fun call(interpreter: RealPythonInterpreter, args: List<Any?>, line: Int): Any? = handler(interpreter, args)
}

class PyCustomFunction(
    val name: String,
    val params: List<String>,
    val body: BlockStmt,
    val closure: PyEnvironment
) : PyCallable {
    override fun call(interpreter: RealPythonInterpreter, args: List<Any?>, line: Int): Any? {
        val env = PyEnvironment(closure)
        for (i in params.indices) {
            val argVal = if (i < args.size) args[i] else null
            env.set(params[i], argVal)
        }
        try {
            for (stmt in body.statements) {
                interpreter.executeStmt(stmt, env)
            }
        } catch (r: PyReturnException) {
            return r.value
        }
        return null
    }
}

class PyEnvironment(private val parent: PyEnvironment? = null, private val directStorage: MutableMap<String, Any?>? = null) {
    private val values = directStorage ?: mutableMapOf()

    fun get(name: String): Any? {
        if (values.containsKey(name)) return values[name]
        return parent?.get(name)
    }

    fun getOrThrow(name: String, line: Int): Any? {
        if (values.containsKey(name)) return values[name]
        parent?.let { return it.getOrThrow(name, line) }
        throw PyRuntimeError("NameError", "name '$name' is not defined", line)
    }

    fun set(name: String, value: Any?) {
        values[name] = value
    }
}
