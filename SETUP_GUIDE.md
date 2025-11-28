# Tala Backend Setup Guide

Complete setup guide for local development and production deployment on Apple Silicon.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Production Setup (Mac Mini)](#production-setup-mac-mini)
4. [GitHub Actions CI/CD](#github-actions-cicd)
5. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Development Machine (MacBook Air)

- **macOS** Ventura or later
- **Java 21** (ARM64)
  ```bash
  brew install openjdk@21
  ```
- **Maven 3.9+**
  ```bash
  brew install maven
  ```
- **Docker Desktop** for Mac
  - Download from: https://www.docker.com/products/docker-desktop
  - Enable Apple Silicon support
- **Git**
  ```bash
  brew install git
  ```

### Production Server (Mac Mini)

- **macOS** Ventura or later
- **Docker Desktop** or **Docker CE**
- **SSH access** configured
- **Git**

---

## Local Development Setup

### Step 1: Clone Repository

```bash
cd /Users/robert/Documents/Projects/TalaAI/backend
```

### Step 2: Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env file with your configuration
nano .env
```

Key configurations:
```bash
POSTGRES_PASSWORD=tala_dev_2025
REDIS_PASSWORD=          # Leave empty for dev
JWT_SECRET=dev-secret-key-change-in-production
OPENAI_API_KEY=your-key  # If using AI features
```

### Step 3: Start Infrastructure

```bash
# Make scripts executable (already done)
chmod +x scripts/*.sh

# Start all infrastructure services
./scripts/start-dev.sh
```

This will start:
- ✅ PostgreSQL (port 5432)
- ✅ ClickHouse (port 8123)
- ✅ Kafka (port 9092)
- ✅ Redis (port 6379)
- ✅ Prometheus (port 9090)
- ✅ Grafana (port 3000)

### Step 4: Verify Infrastructure

```bash
# Check all containers are running
docker ps

# Test PostgreSQL
docker exec -it tala-postgres-dev psql -U tala -d tala_db -c "SELECT version();"

# Test ClickHouse
curl http://localhost:8123

# Test Redis
docker exec -it tala-redis-dev redis-cli ping

# Test Kafka
docker exec -it tala-kafka-dev kafka-topics --bootstrap-server localhost:9092 --list
```

### Step 5: Build Project

```bash
# Build all modules
mvn clean install

# Verify build
ls -la shared/common-core/target/*.jar
```

### Step 6: Run Services

**Option A: Run with Maven (Development)**
```bash
cd services/event-service
mvn spring-boot:run
```

**Option B: Run with Docker (Closer to Production)**
```bash
# Build service image
docker build -t tala/event-service:latest \
  --build-arg SERVICE_NAME=event-service \
  -f Dockerfile.template .

# Run service
docker run -p 8081:8081 \
  --network tala-network \
  -e SPRING_PROFILES_ACTIVE=dev \
  tala/event-service:latest
```

### Step 7: Test API

```bash
# Health check
curl http://localhost:8081/actuator/health

# Create test event
curl -X POST http://localhost:8081/api/v1/events \
  -H "Content-Type: application/json" \
  -d '{
    "profileId": 1,
    "userId": 1,
    "eventType": "FEEDING",
    "eventTime": "2024-01-01T10:30:00Z",
    "eventData": {
      "amount": 120,
      "unit": "ml"
    }
  }'
```

---

## Production Setup (Mac Mini)

### Step 1: Prepare Mac Mini

```bash
# SSH into Mac Mini
ssh user@your-mac-mini-ip

# Install Docker (if not installed)
# Option 1: Docker Desktop
# Download from https://www.docker.com/products/docker-desktop

# Option 2: Docker CE (command line)
brew install docker docker-compose

# Create project directory
mkdir -p ~/tala-backend
cd ~/tala-backend
```

### Step 2: Configure Production Environment

```bash
# Copy environment template
cp .env.production.example .env.production

# Edit with SECURE credentials
nano .env.production
```

**IMPORTANT**: Use strong passwords!
```bash
POSTGRES_PASSWORD=VERY-SECURE-PASSWORD-HERE
REDIS_PASSWORD=ANOTHER-SECURE-PASSWORD
JWT_SECRET=LONG-RANDOM-STRING-64-CHARS-MINIMUM
GRAFANA_PASSWORD=SECURE-GRAFANA-PASSWORD
```

### Step 3: Deploy Infrastructure

```bash
# Start infrastructure services
docker-compose -f docker-compose.prod.yml up -d postgres clickhouse kafka redis

# Wait for services to be ready
sleep 15

# Verify databases
docker exec -it tala-postgres-prod psql -U tala_prod -d tala_prod -c "\dt events.*"
```

### Step 4: Deploy Application Services

```bash
# Pull or build application images
docker-compose -f docker-compose.prod.yml build

# Start application services
docker-compose -f docker-compose.prod.yml up -d

# Check status
docker-compose -f docker-compose.prod.yml ps
```

### Step 5: Setup Monitoring

```bash
# Access Grafana
# URL: http://your-mac-mini-ip:3000
# Username: admin
# Password: (from GRAFANA_PASSWORD in .env.production)

# Add Prometheus data source
# URL: http://prometheus:9090
```

### Step 6: Setup Backups

```bash
# Test backup script
./scripts/backup.sh

# Setup cron job for daily backups
crontab -e

# Add this line (runs at 2 AM daily)
0 2 * * * cd ~/tala-backend && ./scripts/backup.sh >> ~/tala-backend/logs/backup.log 2>&1
```

---

## GitHub Actions CI/CD

### Step 1: Configure GitHub Secrets

Go to: `https://github.com/YOUR-USERNAME/TalaAI/settings/secrets/actions`

Add these secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `SSH_PRIVATE_KEY` | Your SSH private key | For deploying to Mac Mini |
| `SERVER_HOST` | Mac Mini IP or domain | Production server address |
| `SERVER_USER` | Your username | SSH username |
| `SLACK_WEBHOOK` | Webhook URL (optional) | For notifications |

### Step 2: Generate SSH Key (if needed)

```bash
# On your development machine
ssh-keygen -t ed25519 -C "github-actions"

# Copy public key to Mac Mini
ssh-copy-id -i ~/.ssh/id_ed25519.pub user@your-mac-mini-ip

# Copy private key content to GitHub secret
cat ~/.ssh/id_ed25519
```

### Step 3: Test CI/CD

```bash
# Make a small change and push
git add .
git commit -m "test: trigger CI/CD pipeline"
git push origin main

# Watch GitHub Actions
# Go to: https://github.com/YOUR-USERNAME/TalaAI/actions
```

### Workflow Stages

1. **Build & Test** - Runs on every PR and push
2. **Build Images** - Builds Docker images for main branch
3. **Deploy Production** - Deploys to Mac Mini automatically
4. **Security Scan** - Scans for vulnerabilities

---

## Resource Allocation

### Development (MacBook Air)

Recommended Docker Desktop settings:
- **CPUs**: 4-6 cores
- **Memory**: 6-8 GB
- **Swap**: 2 GB
- **Disk**: 50 GB

Per-service limits (configured in docker-compose.yml):
- CPU: 0.5-1.0 cores
- Memory: 256-512 MB

### Production (Mac Mini)

Recommended allocation:
- **Total CPUs**: 8 cores
- **Total Memory**: 16 GB
- **Disk**: 250 GB SSD

Per-service limits (configured in docker-compose.prod.yml):
- Infrastructure: 1-2 cores, 512MB-1GB
- Application: 1 core, 512MB each

---

## Troubleshooting

### Issue: Docker containers won't start

**Solution:**
```bash
# Check Docker is running
docker info

# Clean up and restart
docker-compose down -v
docker system prune -a
./scripts/start-dev.sh
```

### Issue: Maven build fails

**Solution:**
```bash
# Check Java version
java -version  # Should be 21

# Clean Maven cache
rm -rf ~/.m2/repository/com/tala
mvn clean install -U
```

### Issue: Database connection errors

**Solution:**
```bash
# Check if PostgreSQL is ready
docker logs tala-postgres-dev

# Manually test connection
docker exec -it tala-postgres-dev psql -U tala -d tala_db

# Reset database
docker-compose down -v
docker-compose up -d postgres
```

### Issue: Port already in use

**Solution:**
```bash
# Find process using port
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different ports in .env
```

### Issue: Out of memory errors

**Solution:**
```bash
# Increase Docker Desktop memory
# Docker Desktop > Settings > Resources > Memory

# Or reduce resource limits in docker-compose.yml
# Edit: deploy.resources.limits.memory
```

### Issue: Apple Silicon compatibility

**Solution:**
```bash
# Ensure using ARM64 images
docker pull --platform linux/arm64 postgres:16-alpine

# Check image architecture
docker inspect postgres:16-alpine | grep Architecture
```

---

## Next Steps

### For Development

1. ✅ Complete common-core module
2. ✅ Create event-service
3. ✅ Create query-service
4. ✅ Create ai-service
5. ✅ Create user-service
6. ✅ Create gateway-service

Follow: [BACKEND-V2-IMPLEMENTATION-PLAN.md](../docs/BACKEND-V2-IMPLEMENTATION-PLAN.md)

### For Production

1. ✅ Setup SSL certificates
2. ✅ Configure domain name
3. ✅ Setup firewall rules
4. ✅ Configure monitoring alerts
5. ✅ Setup automated backups
6. ✅ Load testing

---

## Support

- 📖 Documentation: [README.md](./README.md)
- 🐛 Issues: Create GitHub issue
- 💬 Questions: Contact team

---

**Environment Comparison**

| Feature | Development | Production |
|---------|------------|------------|
| Hardware | MacBook Air | Mac Mini |
| CPU | 4-6 cores | 8 cores |
| Memory | 6-8 GB | 16 GB |
| Docker | Desktop | Desktop/CE |
| SSL | No | Yes |
| Backups | No | Daily |
| Monitoring | Optional | Required |
| Logging | Console | File + Remote |

---

**Ready to develop! 🚀**
