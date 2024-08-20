# Makefile for FX Deals Processing Application

# Variables
APP_NAME = fxdeals
DOCKER_COMPOSE_FILE = docker-compose.yaml

# Default target
.PHONY: all
all: build run

# Build the Java project and create the JAR file
.PHONY: build
build:
	@echo "Building the Java project..."
	mvn clean package

# Build Docker images and run the application
.PHONY: run
run:
	@echo "Building and running the Docker containers..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) up --build

# Stop and remove the Docker containers
.PHONY: down
down:
	@echo "Stopping and removing Docker containers..."
	docker-compose -f $(DOCKER_COMPOSE_FILE) down

# Clean up the project by removing target directory and stopping containers
.PHONY: clean
clean: down
	@echo "Cleaning up the project..."
	mvn clean

# View logs of the application container
.PHONY: logs
logs:
	@echo "Showing logs of the application container..."
	docker-compose logs -f $(APP_NAME)-app




