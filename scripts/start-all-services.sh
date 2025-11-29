#!/bin/bash

# TalaAI - Start All Services Script
# This script builds and starts all microservices in containers

set -e

echo "======================================"
echo "TalaAI Backend - Starting All Services"
echo "======================================"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Change to backend directory
cd "$(dirname "$0")/.."

echo -e "${YELLOW}Step 1: Stopping existing services...${NC}"
docker-compose -f docker-compose.yml -f docker-compose.services.yml down

echo -e "${YELLOW}Step 2: Building all service images...${NC}"
echo "This may take 5-10 minutes on first run..."

# Build all services
docker-compose -f docker-compose.services.yml build --parallel

echo -e "${GREEN}✓ All images built successfully${NC}"

echo -e "${YELLOW}Step 3: Starting infrastructure services...${NC}"
docker-compose -f docker-compose.yml up -d postgres redis

echo "Waiting for PostgreSQL to be healthy..."
until docker exec tala-postgres-dev pg_isready -U tala > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo -e "${GREEN}✓ PostgreSQL is ready${NC}"

echo -e "${YELLOW}Step 4: Initializing database schemas...${NC}"
docker exec -i tala-postgres-dev psql -U tala -d tala_db < infrastructure/postgresql/init-db.sql

echo -e "${GREEN}✓ Database schemas initialized${NC}"

echo -e "${YELLOW}Step 5: Starting all microservices...${NC}"
docker-compose -f docker-compose.yml -f docker-compose.services.yml up -d

echo ""
echo "======================================"
echo "Waiting for services to be healthy..."
echo "======================================"

# Wait for services to be healthy
services=("user-service:8084" "event-service:8081" "reminder-service:8085" "media-service:8086" "file-service:8087" "query-service:8082" "ai-service:8083" "personalization-service:8088")

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    echo -n "Waiting for $name..."
    
    max_attempts=60
    attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -sf http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo -e " ${GREEN}✓ UP${NC}"
            break
        fi
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    if [ $attempt -eq $max_attempts ]; then
        echo -e " ${RED}✗ TIMEOUT${NC}"
    fi
done

echo ""
echo "======================================"
echo -e "${GREEN}All Services Started!${NC}"
echo "======================================"
echo ""
echo "Service URLs:"
echo "  User Service:           http://localhost:8084"
echo "  Event Service:          http://localhost:8081"
echo "  Query Service:          http://localhost:8082"
echo "  AI Service:             http://localhost:8083"
echo "  Reminder Service:       http://localhost:8085"
echo "  Media Service:          http://localhost:8086"
echo "  File Service:           http://localhost:8087"
echo "  Personalization Service: http://localhost:8088"
echo ""
echo "Infrastructure:"
echo "  PostgreSQL:             localhost:5432"
echo "  Redis:                  localhost:6379"
echo ""
echo "View logs:"
echo "  docker-compose -f docker-compose.yml -f docker-compose.services.yml logs -f [service-name]"
echo ""
echo "Stop all services:"
echo "  docker-compose -f docker-compose.yml -f docker-compose.services.yml down"
echo ""
