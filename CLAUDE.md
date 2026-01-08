# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot demo template project designed to showcase Huawei CodeArts CI/CD capabilities. It's a minimal Java Web application used for demonstrating cloud-based build pipelines and deployment workflows.

**Key Technologies:**
- Spring Boot 2.5.5
- Java 8 (JDK 1.8)
- Maven 3.3+
- Spring Web MVC

**Project Coordinates:**
- GroupId: `com.huawei.codearts`
- ArtifactId: `demoapp`
- Packaging: `jar`

## Build Commands

```bash
# Full clean build
mvn clean install

# Compile only
mvn clean compile

# Run unit tests
mvn test

# Run integration tests
mvn verify

# Package application
mvn package

# Run the application (Spring Boot plugin)
mvn spring-boot:run

# Run the packaged JAR
java -jar ./target/demoapp.jar
```

The application runs on `http://localhost:8080/` by default.

## Code Architecture

**Package Structure:**
```
com.huawei.codearts/
├── JavaWebDemoApplication.java  # Main Spring Boot application class
└── controller/
    └── TestController.java      # REST controllers
```

**Main Components:**
- [JavaWebDemoApplication.java](src/main/java/com/huawei/codearts/JavaWebDemoApplication.java) - Spring Boot entry point with `@SpringBootApplication`
- [TestController](src/main/java/com/huawei/codearts/controller/TestController.java) - REST controllers (currently has `/test` endpoint returning "hello world")

**Current Endpoints:**
- `GET /test` - Returns "hello world"

**Note:** The `apitest.yaml` file defines additional API documentation for CI/CD pipeline testing (references `/api/hello-world` endpoint), but this endpoint is not currently implemented in the codebase.

## CI/CD Pipeline

This project is configured for Huawei CodeArts with three pipeline stages:

1. **Source Stage** - Source code repository integration
2. **Build Stage** - Maven build + code check tasks
3. **Deployment Stage** - Deployment + API test tasks (using `apitest.yaml`)

## Build Output

The Maven build produces an executable JAR at `target/demoapp.jar` (configured via `<finalName>${project.artifactId}</finalName>` in pom.xml).

## Important Notes

- This is a **demo/template project** with minimal functionality
- Documentation is primarily in Chinese (README.md)
- The project uses standard Maven directory layout
- Spring Boot Maven Plugin creates the executable JAR with embedded Tomcat
- Maven Surefire Plugin 3.0.0-M3 is configured for test execution
