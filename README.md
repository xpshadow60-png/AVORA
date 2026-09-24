# Avora — Production Architecture & Pre-Launch Guide

Avora is an AI-powered academic and technology learning platform built with Kotlin, Jetpack Compose, Room DB, Firebase Auth, and an authenticated AI backend proxy.

## Architecture Overview

### 1. High-Resolution Homework Photo Pipeline (P0)
- **Capture Mechanism**: Uses `ActivityResultContracts.TakePicture()` with Android's `FileProvider` to capture full sensor-resolution images. Low-resolution thumbnail previews (`TakePicturePreview`) are strictly avoided.
- **Image Preprocessing (`HomeworkImageHelper`)**:
  - Automatically reads EXIF orientation metadata (`ExifInterface`) and applies corrective rotation matrix.
  - Scales high-res images to a maximum dimension of 1600px while maintaining original aspect ratio.
  - Compresses to standard JPEG with 82% quality to balance pedagogical legibility against transmission payload.
  - Validates minimum dimensions and non-blank luminance before transmission.
  - Encodes directly into Base64 for the `BackendAiRequest.imageBase64` payload.
- **Backend API**: Dispatches to `/api/ai/generate` with pedagogical instructions. If the image is illegible or blurry, Avora gracefully informs the student rather than hallucinating an answer.

### 2. Authentication & Data Isolation (P1)
- **Token-Based Verification**: Authenticated requests exchange Firebase Authentication ID tokens (`request.auth.token`) via `Authorization: Bearer <token>`.
- **Identity Isolation**: The server independently verifies the JWT and decodes `uid` directly from the token, discarding any unverified client-asserted `userId`.
- **Guest Support**: Guest mode operates with client-side isolation using the local Room database (`userId = "guest_user"`). When a guest user registers or logs in, local assets are migrated seamlessly to their verified account via `StudyRepository.claimGuestDataForUser`.
- **Account Deletion (GDPR/Play Compliance)**:
  - Invokes `CloudSyncManager.deleteUserCloudData(userId)` while the user's auth token is still active.
  - Purges all cloud subcollections (`tasks`, `flashcards`, `study_sessions`, `quiz_results`, `schedules`, `roadmap`) and the root user profile.
  - Verifies Firestore purge before executing `AuthManager.deleteAccount()` and local Room purge (`StudyRepository.deleteUserData(userId)`).

### 3. Server-Side Rate Limiting & Proxy Verification (P1)
- **Endpoint**: Configured in `GeminiClient.kt` via `BACKEND_AI_URL` (default: `https://avora-backend-proxy.app/api/ai/generate`).
- **Headers**:
  - `Authorization: Bearer <Firebase_ID_Token>`
  - `X-User-Id: <userId>`
  - `X-Avora-Guest: <boolean>`
  - `X-Task-Type: <HOMEWORK_PHOTO | SOCRATIC_CODING | PRODUCTIVITY_COACH | ...>`
- **HTTP 429 Handling**: Automatically intercepted by `GeminiClient` with a user-friendly cooldown warning.

### 4. Client-Side Python Sandbox Engine (P2)
- **Interpreter**: Custom-built recursive-descent parser and AST evaluation engine (`SafePythonRunner.kt`).
- **Guaranteed Isolation**: No Java reflection, no process execution, no network sockets, and no file system access.
- **Safeguard Thresholds**:
  - Step limit: `MAX_STEPS = 50,000` instructions.
  - Timeout limit: `MAX_EXECUTION_TIME_MS = 2,500ms` (wall-clock).
  - Recursion limit: `MAX_CALL_DEPTH = 250` stack frames.
  - Console limits: `MAX_PRINTED_LINES = 400`, `MAX_PRINTED_CHARS = 30,000`.

### 5. Creator Identity & Pedagogical Routing (P2)
- **Attribution**: Preserves creator attribution ("Sir Barie Bilal made me" / "Sir Barie Bilal is from Kashmir India").
- **Pedagogical Separation**: Compound inquiries (e.g. asking creator identity combined with a biology or computer science question) route the academic component to the AI reasoning engine so student inquiries receive comprehensive explanations.
