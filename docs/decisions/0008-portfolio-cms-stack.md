# 0008 Portfolio CMS Stack

Date: 2026-06-17

## Status

Accepted

## Context

`SPEC.md` defines a learning-first full-stack Portfolio CMS built with a Java
Spring Boot backend, PostgreSQL database, and React frontend. The repository had
Harness files and the product spec, but no application implementation.

The first implementation story needs concrete stack versions and a project
shape so future work can build one vertical slice at a time.

## Decision

Use this foundation stack:

- Backend: Java 21, Spring Boot 3.5.x, Maven, Spring Web, Spring Data JPA,
  Flyway, PostgreSQL, and Spring Boot validation/test tooling.
- Frontend: React 19, Vite 8, TypeScript, React Router 7, TanStack Query 5,
  React Hook Form 7, and Zod 4.
- Runtime containers: PostgreSQL 16, Maven with Eclipse Temurin 21 for backend
  builds, Node 24 plus nginx for frontend builds.

Spring Boot 3.5.x is chosen instead of Spring Boot 4.x for the initial learning
project because it keeps the system on the mature Spring Boot 3 ecosystem while
remaining current enough for Java 21 and modern Spring development.

## Alternatives Considered

1. Use Spring Boot 4.x immediately. This follows the newest Spring Initializr
   line, but increases breaking-change risk before the project has tests.
2. Use older React/Vite versions. This lowers dependency novelty, but gives the
   frontend a stale foundation before any product code exists.
3. Use Gradle instead of Maven. Maven is simpler for this learning-first
   scaffold and maps cleanly to Spring Initializr defaults.

## Consequences

Positive:

- The backend and frontend stacks match the product spec.
- Future stories can add auth, schema, and CMS modules without moving folders.
- Docker Compose can become the common local run path once Docker is available.

Tradeoffs:

- The current workspace cannot compile or run the scaffold until Node, Java,
  Maven, Docker, and PostgreSQL tooling are installed or made available.
- Boot 4 migration remains a future decision after tests exist.

## Follow-Up

- Install or expose Node, Java, Maven, Docker, and PostgreSQL tooling on PATH.
- Add executable validation once the local toolchain is available.
- Revisit Spring Boot 4 after MVP workflows are covered by tests.

