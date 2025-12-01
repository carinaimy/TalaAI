#!/bin/bash
# Start Development Environment
#
# Usage:
#   ./start-dev.sh              # Minimal (postgres + redis only)
#   ./start-dev.sh --analytics  # + ClickHouse
#   ./start-dev.sh --events     # + Kafka + Zookeeper
#   ./start-dev.sh --monitoring # + Prometheus + Grafana
#   ./start-dev.sh --full       # All services

set -e

# Parse arguments
MODE="minimal"
PROFILES=""

case "${1:-}" in
    --analytics)
        MODE="analytics"
        PROFILES="--profile analytics"
        ;;
    --events)
        MODE="events"
        PROFILES="--profile events"
        ;;
    --monitoring)
        MODE="monitoring"
        PROFILES="--profile monitoring"
        ;;
    --full)
        MODE="full"
        PROFILES="--profile analytics --profile events --profile monitoring"
        ;;
    --minimal|"")
        MODE="minimal"
        PROFILES=""
        ;;
    *)
        echo "❌ Invalid option: $1"
        echo ""
        echo "Usage: $0 [--minimal|--analytics|--events|--monitoring|--full]"
        echo ""
        echo "Modes:"
        echo "  --minimal     PostgreSQL + Redis only (default)"
        echo "  --analytics   + ClickHouse"
        echo "  --events      + Kafka + Zookeeper"
        echo "  --monitoring  + Prometheus + Grafana"
        echo "  --full        All services"
        exit 1
        ;;
esac

echo "🚀 Starting Tala Backend Development Environment (${MODE} mode)..."

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
if [ -z "$PROFILES" ]; then
    docker-compose up -d
else
    docker-compose $PROFILES up -d
fi

# Wait for services to be healthy
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check service health
echo "🔍 Checking service health..."

# Core services (always check)
core_services=("postgres" "redis")
for service in "${core_services[@]}"; do
    if docker ps | grep -q "tala-${service}-dev"; then
        echo "✅ ${service} is running"
    else
        echo "❌ ${service} failed to start"
        docker-compose logs ${service}
        exit 1
    fi
done

# Optional services (check based on mode)
if [[ "$MODE" == "analytics" || "$MODE" == "full" ]]; then
    if docker ps | grep -q "tala-clickhouse-dev"; then
        echo "✅ clickhouse is running"
    fi
fi

if [[ "$MODE" == "events" || "$MODE" == "full" ]]; then
    if docker ps | grep -q "tala-kafka-dev"; then
        echo "✅ kafka is running"
    fi
fi

if [[ "$MODE" == "monitoring" || "$MODE" == "full" ]]; then
    if docker ps | grep -q "tala-prometheus-dev"; then
        echo "✅ prometheus is running"
    fi
    if docker ps | grep -q "tala-grafana-dev"; then
        echo "✅ grafana is running"
    fi
fi

# Show service URLs
echo ""
echo "🎉 Development environment is ready (${MODE} mode)!"
echo ""
echo "📊 Running Services:"
echo "  ✅ PostgreSQL:  localhost:5432"
echo "  ✅ Redis:       localhost:6379"

if [[ "$MODE" == "analytics" || "$MODE" == "full" ]]; then
    echo "  ✅ ClickHouse:  http://localhost:8123"
fi

if [[ "$MODE" == "events" || "$MODE" == "full" ]]; then
    echo "  ✅ Kafka:       localhost:9092"
fi

if [[ "$MODE" == "monitoring" || "$MODE" == "full" ]]; then
    echo "  ✅ Prometheus:  http://localhost:9090"
    echo "  ✅ Grafana:     http://localhost:3000 (admin/admin)"
fi

echo ""
echo "💡 Switch modes:"
echo "  ./scripts/start-dev.sh --analytics   # Add ClickHouse"
echo "  ./scripts/start-dev.sh --events      # Add Kafka"
echo "  ./scripts/start-dev.sh --monitoring  # Add monitoring"
echo "  ./scripts/start-dev.sh --full        # All services"
echo ""
echo "📝 Next steps:"
echo "  1. Build the project:    mvn clean install"
echo "  2. Run a service:        cd services/origin-data-service && mvn spring-boot:run"
echo "  3. View logs:            docker-compose logs -f"
echo "  4. Stop services:        ./scripts/stop-dev.sh"
echo ""
