-----

# 🎙️ Spring AI Audio Transcriber

A full-stack, intelligent audio transcription application capable of processing audio files using **Spring AI**. This application supports multi-model transcription (Google Gemini & OpenAI), includes a secure authentication system, and features a modern React frontend for managing transcription history.

-----

## ✨ Features

  * **Multi-Model Intelligence:**
      * **Google Gemini (via REST/WebClient):** Uses multimodal capabilities for direct audio understanding.
      * **OpenAI (via Spring AI):** Standard audio transcription and summarization.
  * **Secure Authentication:**
      * JWT (JSON Web Token) based Stateless Authentication.
      * Role-based access control (User/Admin).
  * **Robust Backend:**
      * Spring Boot 3.3.3 architecture.
      * Reactive WebClient for external API communication.
      * Rate limiting implementation (Sliding Window logic).
  * **Modern Frontend:**
      * React 19 with React Router 7.
      * Context API for global Authentication state management.
      * Bootstrap 5 & Lucide React icons for a clean UI.
  * **Persistent Storage:**
      * MySQL database for storing Users and Transcription History.

-----

## 🛠️ Tech Stack

### Backend (`ai-audio-transcriber`)

  * **Language:** Java 17
  * **Framework:** Spring Boot 3.3.3
  * **AI Integration:** Spring AI (OpenAI), WebClient (Gemini)
  * **Security:** Spring Security 6, JJWT 0.12.5
  * **Database:** MySQL with Spring Data JPA
  * **Build Tool:** Maven

### Frontend (`transcription-frontend`)

  * **Library:** React 19
  * **Routing:** React Router DOM 7
  * **Styling:** Bootstrap 5.3
  * **HTTP Client:** Axios
  * **Icons:** Lucide React

-----

## 📂 Project Structure

### Backend Architecture

```text
src/main/java/com/ai/audio/transcriber
├── config/           # Security (JWT), WebClient, and CORS config
├── controller/       # REST Endpoints (Auth, Transcription)
├── dto/              # Data Transfer Objects (Requests/Responses)
├── model/            # JPA Entities (User, TranscriptionRecord)
├── repository/       # Database Interfaces
└── service/          # Business Logic (AI processing, User details)
```

### Frontend Architecture

```text
src/
├── api/              # Axios instances & API service calls
├── components/       # Reusable UI components (Navbar, ProtectedRoute)
├── context/          # Global State (AuthContext)
├── pages/            # Views (Dashboard, Login, Upload, History)
└── App.jsx           # Main routing configuration
```

-----

## 🚀 Getting Started

### Prerequisites

  * **Java 17** SDK
  * **Node.js** (v18+ recommended)
  * **MySQL Server** running locally

### 1\. Database Setup

Create a MySQL database named `audio_transcriber_db`:

```sql
CREATE DATABASE audio_transcriber_db;
```

### 2\. Backend Setup

1.  Navigate to the backend directory:
    ```bash
    cd ai-audio-transcriber
    ```
2.  Update `src/main/resources/application.properties` with your credentials:
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/audio_transcriber_db
    spring.datasource.username=root
    spring.datasource.password=YOUR_DB_PASSWORD

    # AI Keys
    spring.ai.openai.api-key=sk-proj-...
    gemini.api.key=AIzaSy...
    ```
3.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```

### 3\. Frontend Setup

1.  Navigate to the frontend directory:
    ```bash
    cd transcription-frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Start the development server:
    ```bash
    npm run dev
    ```

-----

## 🔌 API Endpoints

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| **POST** | `/auth/register` | Register a new user | Public |
| **POST** | `/auth/login` | Login and receive JWT | Public |
| **POST** | `/transcribe` | Upload audio for processing | Authenticated |
| **GET** | `/history` | Get user's past transcriptions | Authenticated |

-----

## 🔮 Next Roadmap
 
  - [ ] **Fixing Front-End:** Fixing some frontEnd part.
  - [ ] **Fixing Backend:** Fixing Some Backend.
  - [ ] **Streaming Responses:** Implement Server-Sent Events (SSE) for real-time transcription updates.
  - [ ] **File Export:** Add functionality to download summaries as PDF.
  - [ ] **Admin Dashboard:** Create a view for admins to see usage statistics.
  - [ ] **Dockerization:** Add `Dockerfile` and `docker-compose.yml` for easy deployment.

-----

## 🤝 Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/NewFeature`).
3.  Commit your changes (`git commit -m 'Add NewFeature'`).
4.  Push to the branch (`git push origin feature/NewFeature`).
5.  Open a Pull Request.