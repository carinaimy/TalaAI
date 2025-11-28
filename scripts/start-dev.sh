#!/bin/bash
# Start Development Environment

set -e

echo "🚀 Starting Tala Backend Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop."
    exit 1
fi

# Load environment variables
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Loaded .env file"
else
    echo "⚠️  No .env file found. Using default values."
    cp .env.example .env
    echo "✅ Created .env from .env.example"
fi

# Start infrastructure services
echo "📦 Starting infrastructure services..."
docker-compose up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🔍 Checking service health..."
services=("postgres" "clickhouse" "kafka" "redis")
for service in "${services[@]}"; do
    if docker ps | grep -q "tala-${service}-dev"; then
        echo "✅ ${service} is running"
    else
        echo "❌ ${service} failed to start"
        docker-compose logs ${service}
        exit 1
    fi
done

# Show service URLs
echo ""
echo "🎉 Development environment is ready!"
echo ""
echo "📊 Service URLs:"
echo "  PostgreSQL:  localhost:5432"
echo "  ClickHouse:  http://localhost:8123"
echo "  Kafka:       localhost:9092"
echo "  Redis:       localhost:6379"
echo "  Prometheus:  http://localhost:9090"
echo "  Grafana:     http://localhost:3000 (admin/admin)"
echo ""
echo "📝 Next steps:"
echo "  1. Build the project:    mvn clean install"
echo "  2. Run a service:        cd services/event-service && mvn spring-boot:run"
echo "  3. View logs:            docker-compose logs -f"
echo "  4. Stop services:        ./scripts/stop-dev.sh"
echo ""
