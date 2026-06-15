# Module Management System

A centralized platform for managing module proposals, reviews, and approvals within the School of Computation, Information and Technology (CIT) at TUM.

## Project Overview

The Module Management System streamlines the process of creating, reviewing, and approving academic modules. It replaces the inefficient email-based workflow with a structured digital platform that provides clear guidance and feedback mechanisms for all stakeholders. Further information can be found [here](https://confluence.ase.in.tum.de/spaces/CITMMAI/pages/225608115/CIT+Module+Management+with+AI+Home)

### Key Features

- **Module Proposal Creation**: Professors can create and save module proposals with all necessary fields.
- **Structured Feedback Process**: Reviewers can provide granular feedback on specific sections.
- **Version Management**: Support for creating new module versions based on feedback while maintaining version history.
- **AI-Assisted Description Generation**: Help professors create standardized module descriptions.
- **AI Proposal Review**: Generate an LLM-based review of a proposal against configurable guidelines, shared across all roles with one stored result per module version.
- **Module Overlap Detection**: Identify potential overlaps between proposed modules and existing curriculum.
- **PDF Export**: Export module information for offline use.

## Usage Guide

### Proposal State Workflow

![activity_proposal_states](https://github.com/user-attachments/assets/cba90205-be19-4d24-a239-84bd671e611d)

### For Professors

1. **Creating a Module Proposal**:
   - Log in to the system with professor credentials
   - Navigate to "Create New Proposal"
   - Fill in all required module information
   - Save your progress at any time
   - Use AI-assistance for generating standardized descriptions
   - Check for potential module overlaps
   - Submit when ready for review

2. **Handling Feedback**:
   - Review consolidated feedback from all stakeholders
   - Create a new module version addressing the feedback
   - Resubmit for approval

### For Reviewers

1. **Reviewing Module Proposals**:
   - Log in with reviewer credentials
   - View list of pending module proposals
   - Provide specific feedback for each field
   - Approve, request changes, or reject proposals

### AI Proposal Review

The AI review feature generates a structured LLM review of a module version, section by section (title, content, learning outcomes, etc.), with a severity rating (`OK`, `ATTENTION`, `CRITICAL`), findings, and suggestions per section.

- **Guidelines**: Users with the `AI_REVIEW_GUIDELINE_MANAGER` role (assignable by an admin) maintain a single shared list of review guidelines under "AI Review Guidelines". Each guideline targets either the whole proposal (`General`) or a specific section. Guidelines are injected into the LLM prompt; if none are configured, the review falls back to generic academic standards and the UI shows a hint.
- **Access**: The proposal owner can always generate a review. Quality management, examination board, and academic program advisors can review any proposal; program/specialization coordinators only proposals they are assigned to as reviewers.
- **Persistence**: One stored review per module version, shared by professors and reviewers. Opening the AI review page returns it instantly, or generates one automatically on first visit. "Regenerate review" forces a fresh LLM run via `?regenerate=true`.
- **Where to find it**: "AI review" buttons on the proposal page (current and previous versions), the module version view, the edit page, and the feedback view navigate to the dedicated AI review page.
- **Configuration**: Uses the same chat model configured for the other AI features (see "Using a Local LLM"). HTTP client timeouts for long LLM calls are configured in `Server/src/main/resources/application.yaml` under `spring.http.clients`.

## System Architecture

The system implements a modular client-server architecture with three primary components:

1. **Angular Client**: Provides role-specific user interfaces with responsive design
2. **Spring Boot Server**: Implements core business logic, workflow, and data persistence

![subsystem_diagram_v2](https://github.com/user-attachments/assets/1fa569ac-c179-4dea-9b04-e7141031f161)

### Technology Stack

- **Client-side**: Angular 19, TypeScript, Tailwind CSS
- **Server-side**: Java Spring Boot, Hibernate, PostgreSQL, SpringAI
- **Authentication**: Keycloak integration
- **Deployment**: Docker containerization

## Development Setup

### Prerequisites

Make sure you have the following installed:

- Docker and Docker Compose
- Node.js v20.19+ and npm
- Angular CLI
- Java JDK 21 (the server build uses Gradle’s Java 21 toolchain; JDK 25 alone is not enough)

#### Java 21 on macOS (Homebrew)

If `./gradlew bootRun` fails with “Cannot find a Java installation … matching languageVersion=21”, install JDK 21:

```bash
brew install openjdk@21
```

The `Server/gradle.properties` file registers the Homebrew JDK 21 path for Gradle. If you installed JDK 21 elsewhere, update `org.gradle.java.installations.paths` in that file, or run:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
```

### Environment Configuration

1. **Copy the example environment file** to create your `.env` file:

```bash
cp .example.env .env
```

2. **Edit `.env`** and update the values as needed.

### Running the Application

#### 1. Start Docker Services

From the project root directory, start PostgreSQL and Keycloak:

```bash
docker-compose -f docker/docker-compose.dev.yaml --env-file .env up
```

Ports are configured in your `.env` file.

#### 2. Start the Spring Boot Server

From the repository root, start the server from the `Server` directory (if your shell is already in `Server`, skip `cd Server`):

```bash
cd Server
./gradlew bootRun
```

**Note**: Make sure the server has execute permissions on `gradlew`. If not, run:

```bash
chmod +x gradlew
```

The server will start on `http://localhost:8080`.

#### 3. Start the Angular Client

From the `Client` directory:

```bash
cd Client
npm install   # First time only
npm start
```

The client will start on `http://localhost:4200`.

**Development Mode**: The client uses `environment.development.ts` which points to your local server and Keycloak instances. URLs are configured in the environment file.

#### Using a Local LLM (LM Studio)

SpringAI supports using local LLMs via LM Studio or other OpenAI-compatible local servers. This is useful for development when you don't want to use Azure OpenAI.

**Prerequisites:**

- [LM Studio](https://lmstudio.ai/) installed and running
- A model loaded in LM Studio

**Setup Steps:**

1. **Start LM Studio**:
   - Open LM Studio
   - Load a model of your choice
   - Start the local server (usually runs on `http://localhost:1234`)

2. **Configure Environment Variables**:

   In your `.env` file, set:

   ```bash
   CHAT_MODEL_URL=http://localhost:1234
   CHAT_MODEL_NAME=your-model-name
   ```

### Test Users

The Keycloak realm includes test users (see `module-management-realm.json`), which are also seeded to the database when you run the server:

**Professors:**

- `module_management_test_professor1` / `test` - Role: PROFESSOR (Max Mustermann)
- `module_management_test_professor2` / `test` - Role: PROFESSOR (Alice Wonderland)

**Academic Program Advisor:**

- `module_management_test_apa1` / `test` - Role: ACADEMIC_PROGRAM_ADVISOR (Academic Program Advisor)

**Quality Management:**

- `module_management_test_qm1` / `test` - Role: QUALITY_MANAGEMENT (Quirin Moos)

**Examination Board:**

- `module_management_test_eb1` / `test` - Role: EXAMINATION_BOARD (Erik Bert)

### Generating OpenAPI Client Code

If the API changes, regenerate the TypeScript client:

```bash
cd Client
npm run api:update
```

This requires the Spring Boot server to be running on port 8080.

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgements

This project was developed as part of a Master's thesis by Kilian Wimmer at the Technical University of Munich.
