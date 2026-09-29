# Thousand Online - development and production Compose targets.
# Everything runs inside containers; only Docker and make are required on the host.

COMPOSE := docker compose
COMPOSE_PROD := docker compose -f compose.yaml -f compose.prod.yaml
BACKEND := $(COMPOSE) run --rm --no-deps backend
FRONTEND := $(COMPOSE) run --rm --no-deps frontend

.PHONY: help env dev dev-build down logs ps config prod-up prod-down prod-logs prod-ps \
        test test-backend test-frontend \
        lint lint-backend lint-frontend format format-backend format-frontend typecheck \
        db-shell clean

# ===========================================
# Setup
# ===========================================

env: ## Create .env from .env.example if it does not exist
	@test -f .env || cp .env.example .env
	@echo ".env ready"

# ===========================================
# Development
# ===========================================

dev: ## Start the development stack (backend :8080, frontend :5173, db :5432)
	$(COMPOSE) up

dev-build: ## Rebuild images and start the development stack
	$(COMPOSE) up --build

down: ## Stop the development stack
	$(COMPOSE) down

logs: ## Follow logs of all services
	$(COMPOSE) logs -f

ps: ## Show service status
	$(COMPOSE) ps

config: ## Validate the development and production Compose configuration
	$(COMPOSE) config --quiet
	$(COMPOSE_PROD) config --quiet
	@echo "compose config OK (dev + prod)"

# ===========================================
# Production stack (compose.yaml + compose.prod.yaml)
# ===========================================

prod-up: ## Build and start the production stack in the background (frontend on HTTP_PORT, default 8080)
	$(COMPOSE_PROD) up -d --build

prod-down: ## Stop the production stack
	$(COMPOSE_PROD) down

prod-logs: ## Follow logs of the production stack
	$(COMPOSE_PROD) logs -f

prod-ps: ## Show production service status
	$(COMPOSE_PROD) ps

# ===========================================
# Testing
# ===========================================

test: test-backend test-frontend ## Run all tests

test-backend: ## Backend build, tests, Spotless check and Checkstyle (mvn verify)
	$(BACKEND) ./mvnw -B verify

test-frontend: ## Frontend unit tests (Vitest)
	$(FRONTEND) npm test

# ===========================================
# Code Quality
# ===========================================

lint: lint-backend lint-frontend typecheck ## Run all linters and the type check

lint-backend: ## Spotless check and Checkstyle
	$(BACKEND) ./mvnw -B spotless:check checkstyle:check

lint-frontend: ## ESLint and Prettier check
	$(FRONTEND) sh -c "npm run lint && npm run format:check"

typecheck: ## TypeScript type check
	$(FRONTEND) npm run typecheck

format: format-backend format-frontend ## Format all code

format-backend: ## Apply palantir-java-format via Spotless
	$(BACKEND) ./mvnw -B spotless:apply

format-frontend: ## Apply Prettier
	$(FRONTEND) npm run format

# ===========================================
# Database
# ===========================================
# Migrations are applied by Flyway at application start (added in TASK-003).

db-shell: ## Open psql in the database container
	$(COMPOSE) exec db sh -c 'psql -U "$$POSTGRES_USER" -d "$$POSTGRES_DB"'

# ===========================================
# Cleanup
# ===========================================

clean: ## Stop containers and remove volumes (DELETES the local database)
	$(COMPOSE) down -v

# ===========================================
# Help
# ===========================================

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-16s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help
