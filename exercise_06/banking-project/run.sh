#!/bin/bash
set -e

echo "=== 1. Dang bien dich (Build JAR) cac Microservices ==="
(cd account-service && ./gradlew clean bootJar)
(cd transaction-service && ./gradlew clean bootJar)
(cd api-gateway && ./gradlew clean bootJar)

echo "=== 2. Don dep he thong container cu ==="
docker compose down

echo "=== 3. Khoi chay he thong tuan tu voi Healthcheck ==="
docker compose up -d --build

echo "=== 4. Trang thai cac container ==="
docker compose ps