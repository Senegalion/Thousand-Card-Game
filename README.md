# Thousand Online

Online multiplayer implementation of the Polish card game Tysiąc.

## Tech stack

### Backend
- Java 25
- Spring Boot 4.1.x
- Maven
- JUnit 5

### Frontend
- React
- TypeScript
- Tailwind CSS

## Architecture

The project starts as a modular monolith. Game rules are implemented as a pure Java domain model, separated from Spring and infrastructure code.

## Development workflow

This project uses trunk-based development:
- `main` is always stable
- short-lived feature branches
- pull requests with CI checks
- small commits