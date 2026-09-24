package com.example

import com.example.data.remote.SafePythonRunner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PythonInterpreterTest {

    @Test
    fun testFunctionDefinitionAndRecursion() {
        val code = """
            def fib(n):
                if n <= 1:
                    return n
                return fib(n - 1) + fib(n - 2)
            
            print(fib(7))
        """.trimIndent()

        val result = SafePythonRunner.execute(code)
        assertTrue("Expected success but got: ${result.errorMsg}", result.isSuccess)
        assertEquals("13", result.output.trim())
    }

    @Test
    fun testListOperationsAndMethods() {
        val code = """
            nums = [10, 20, 30]
            nums.append(40)
            nums.append(50)
            print("Length:", len(nums))
            print("Sum:", sum(nums))
        """.trimIndent()

        val result = SafePythonRunner.execute(code)
        assertTrue("Expected success but got: ${result.errorMsg}", result.isSuccess)
        assertTrue(result.output.contains("Length: 5"))
        assertTrue(result.output.contains("Sum: 150"))
    }

    @Test
    fun testLoopAndConditionals() {
        val code = """
            evens = []
            for i in range(10):
                if i % 2 == 0:
                    evens.append(i)
            print(evens)
        """.trimIndent()

        val result = SafePythonRunner.execute(code)
        assertTrue("Expected success but got: ${result.errorMsg}", result.isSuccess)
        assertEquals("[0, 2, 4, 6, 8]", result.output.trim())
    }
}
