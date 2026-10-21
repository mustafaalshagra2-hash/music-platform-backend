# Music Platform Backend

A Spring Boot microservices project with three services:

- `music-backend` — REST API, authentication, playlists, songs and plans
- `discovery-service` — Eureka service registry (port 8761)
- `api-gateway` — API gateway (port 8080)

## Requirements

- Java 21 for `music-backend` and `discovery-service`; the gateway POM specifies Java 17
- Maven 3.9+ (or the included Maven Wrapper scripts)
- PostgreSQL with a `music_db` database

## Configuration

Set `DB_PASSWORD` and `JWT_SECRET` in the environment before starting `music-backend`. Configure the PostgreSQL URL and username in `music-backend/src/main/resources/application.properties` for your local environment. Never commit real credentials or signing keys.

## Start order

1. Start `discovery-service` on port 8761.
2. Start `music-backend` on port 8081.
3. Start `api-gateway` on port 8080.
