#!/bin/bash
# Stop Development Environment

set -e

echo "🛑 Stopping Tala Backend Development Environment..."

# Stop all services
docker-compose down

echo "✅ All services stopped"
echo ""
echo "💡 To clean up volumes (⚠️  deletes all data):"
echo "   docker-compose down -v"
echo ""
