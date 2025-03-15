# Module Management System

A centralized platform for managing module proposals, reviews, and approvals within the School of Computation, Information and Technology (CIT) at TUM.

## Project Overview

The Module Management System streamlines the process of creating, reviewing, and approving academic modules. It replaces the inefficient email-based workflow with a structured digital platform that provides clear guidance and feedback mechanisms for all stakeholders. Further information can be found [here](https://confluence.ase.in.tum.de/spaces/CITMMAI/pages/225608115/CIT+Module+Management+with+AI+Home)

### Key Features

- **Module Proposal Creation**: Professors can create and save module proposals with all necessary fields.
- **Structured Feedback Process**: Reviewers can provide granular feedback on specific sections.
- **Version Management**: Support for creating new module versions based on feedback while maintaining version history.
- **AI-Assisted Description Generation**: Help professors create standardized module descriptions.
- **Module Overlap Detection**: Identify potential overlaps between proposed modules and existing curriculum.
- **PDF Export**: Export module information for offline use.

## System Architecture

The system implements a modular client-server architecture with three primary components:

1. **Angular Client**: Provides role-specific user interfaces with responsive design
2. **Spring Boot Server**: Implements core business logic, workflow, and data persistence
3. **Python AI Service**: Delivers module description generation and overlap detection capabilities

![subsystem_diagram_v2](https://github.com/user-attachments/assets/1fa569ac-c179-4dea-9b04-e7141031f161)

### Technology Stack

- **Frontend**: Angular 19, TypeScript, Tailwind CSS
- **Backend**: Java Spring Boot, Hibernate, PostgreSQL
- **AI Service**: Python FastAPI, LangChain, Sentence Transformers
- **Authentication**: Keycloak integration
- **Deployment**: Docker containerization

## Prerequisites

- Docker and Docker Compose
- Node.js 18+ and npm (for development)
- Java JDK 21 (for development)
- Python 3.11+ (for development)
- Configure `.env` file according to your needs

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

## License

This project is licensed under the [MIT License](LICENSE).

## Acknowledgements

This project was developed as part of a Master's thesis by Kilian Wimmer at the Technical University of Munich.
