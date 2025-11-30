#!/bin/bash
# Deploy to Production (Mac Mini)

set -e

# Enable Docker BuildKit for better caching and performance
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

echo "🚀 Deploying Tala Backend to Production..."

# Check environment
if [ ! -f .env.production ]; then
    echo "❌ .env.production file not found!"
    echo "   Please create it from .env.production.example"
    exit 1
fi

# Load production environment
export $(cat .env.production | grep -v '^#' | xargs)

# Confirm deployment
echo "⚠️  You are about to deploy to PRODUCTION"
echo "   Server: ${SERVER_HOST:-localhost}"
read -p "   Continue? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
    echo "❌ Deployment cancelled"
    exit 1
fi

# Build Docker images
echo "🔨 Building Docker images..."
docker-compose -f docker-compose.prod.yml build --parallel

# Push images to registry (if using remote registry)
if [ -n "$DOCKER_REGISTRY" ]; then
    echo "📤 Pushing images to registry..."
    docker-compose -f docker-compose.prod.yml push
fi

# Deploy
echo "🚢 Deploying services..."
docker-compose -f docker-compose.prod.yml up -d

# Wait for health check
echo "⏳ Waiting for services to be ready..."
sleep 15

# Health check
echo "🔍 Running health checks..."
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Gateway service is healthy"
else
    echo "❌ Gateway service health check failed"
    docker-compose -f docker-compose.prod.yml logs gateway-service
    exit 1
fi

# Show status
echo ""
echo "🎉 Deployment completed successfully!"
echo ""
echo "📊 Service Status:"
docker-compose -f docker-compose.prod.yml ps
echo ""
echo "📝 View logs: docker-compose -f docker-compose.prod.yml logs -f"
echo ""
