# Tala Backend Quick Reference

## 🚀 Quick Commands

### Start Development
```bash
./scripts/start-dev.sh      # Start all infrastructure
mvn clean install           # Build project
cd services/event-service && mvn spring-boot:run  # Run service
```

### Stop Development
```bash
./scripts/stop-dev.sh       # Stop all infrastructure
```

### Deploy Production
```bash
./scripts/deploy-prod.sh    # Deploy to Mac Mini
./scripts/backup.sh         # Backup databases
```

## 📊 Service Ports

| Service | Dev Port | Description |
|---------|----------|-------------|
| Gateway | 8080 | API Gateway |
| Event Service | 8081 | Event CRUD |
| Query Service | 8082 | Analytics |
| AI Service | 8083 | AI Features |
| User Service | 8084 | Users |
| PostgreSQL | 5432 | Main DB |
| ClickHouse | 8123 | Analytics DB |
| Kafka | 9092 | Streaming |
| Redis | 6379 | Cache |
| Prometheus | 9090 | Metrics |
| Grafana | 3000 | Dashboards |

## 🐳 Docker Commands

```bash
# View running containers
docker ps

# View logs
docker-compose logs -f [service]

# Restart a service
docker-compose restart [service]

# Clean up everything
docker-compose down -v
docker system prune -a

# Check resource usage
docker stats
```

## 📦 Maven Commands

```bash
# Build all
mvn clean install

# Build without tests
mvn clean install -DskipTests

# Run tests
mvn test

# Run specific test
mvn test -Dtest=EventServiceTest

# Coverage report
mvn jacoco:report

# Update dependencies
mvn clean install -U
```

## 🔍 Database Access

### PostgreSQL
```bash
# Connect
docker exec -it tala-postgres-dev psql -U tala -d tala_db

# List tables
\dt events.*

# Query
SELECT * FROM events.events LIMIT 10;
```

### ClickHouse
```bash
# Connect
docker exec -it tala-clickhouse-dev clickhouse-client

# Show databases
SHOW DATABASES;

# Query
SELECT * FROM tala_analytics.events_analytics LIMIT 10;
```

### Redis
```bash
# Connect
docker exec -it tala-redis-dev redis-cli

# Commands
PING
KEYS *
GET key
```

## 🧪 API Testing

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Create Event
```bash
curl -X POST http://localhost:8081/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "userId": 1,
    "eventType": "FEEDING",
    "eventTime": "2024-01-01T10:30:00Z",
    "eventData": {"amount": 120, "unit": "ml"}
  }'
```

### Get Event
```bash
curl http://localhost:8081/api/v1/events/123
```

## 🔧 Troubleshooting

### Reset Everything
```bash
./scripts/stop-dev.sh
docker-compose down -v
docker system prune -a
./scripts/start-dev.sh
mvn clean install
```

### Check Service Health
```bash
# PostgreSQL
docker exec -it tala-postgres-dev pg_isready

# ClickHouse
curl http://localhost:8123/ping

# Redis
docker exec -it tala-redis-dev redis-cli ping

# Kafka
docker exec -it tala-kafka-dev kafka-topics --bootstrap-server localhost:9092 --list
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres

# Application logs
tail -f services/event-service/logs/app.log
```

## 📝 Git Workflow

```bash
# Create feature branch
git checkout -b feature/your-feature

# Make changes and commit
git add .
git commit -m "feat(service): description"

# Push and create PR
git push origin feature/your-feature
```

## 🔐 Environment Files

- `.env` - Development (git-ignored)
- `.env.production` - Production (git-ignored)
- `.env.example` - Development template (committed)
- `.env.production.example` - Production template (committed)

## 📊 Monitoring URLs

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)
- **Event Service**: http://localhost:8081/actuator
- **Gateway**: http://localhost:8080/actuator

## 🚨 Emergency Commands

### Service Down
```bash
docker-compose restart [service-name]
```

### Database Corrupted
```bash
docker-compose down -v
docker volume prune
./scripts/start-dev.sh
```

### Out of Memory
```bash
docker system prune -a
# Increase Docker Desktop memory
```

### Port Conflict
```bash
lsof -i :8080
kill -9 <PID>
```

## 📚 Documentation Links

- [README](./README.md) - Main documentation
- [SETUP_GUIDE](./SETUP_GUIDE.md) - Detailed setup
- [Implementation Plan](../docs/BACKEND-V2-IMPLEMENTATION-PLAN.md) - Full plan
- [Week 2-10 Plan](../docs/BACKEND-V2-WEEK-2-10-PLAN.md) - Detailed tasks

## 🎯 Common Tasks

### Add New Service
1. Create directory: `services/new-service/`
2. Add POM: `services/new-service/pom.xml`
3. Add to parent POM: `<module>services/new-service</module>`
4. Create Dockerfile
5. Add to docker-compose files

### Update Dependencies
```bash
# Check for updates
mvn versions:display-dependency-updates

# Update specific dependency
# Edit pom.xml versions
mvn clean install -U
```

### Run Integration Tests
```bash
mvn verify
```

## 💡 Tips

- Use IntelliJ IDEA for best Java/Maven support
- Enable Lombok plugin in IDE
- Use Docker Desktop dashboard for easy management
- Keep .env files secure and never commit them
- Monitor resource usage with `docker stats`
- Use `mvn -T 4 clean install` for parallel builds
