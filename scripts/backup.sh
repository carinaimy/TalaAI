#!/bin/bash
# Backup Production Data

set -e

BACKUP_DIR="${BACKUP_PATH:-./backups}"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_NAME="tala_backup_${DATE}"

echo "💾 Starting backup: ${BACKUP_NAME}"

# Create backup directory
mkdir -p "${BACKUP_DIR}"

# Backup PostgreSQL
echo "📦 Backing up PostgreSQL..."
docker exec tala-postgres-prod pg_dump -U ${POSTGRES_USER:-tala_prod} ${POSTGRES_DB:-tala_prod} | gzip > "${BACKUP_DIR}/${BACKUP_NAME}_postgres.sql.gz"

# Backup ClickHouse
echo "📦 Backing up ClickHouse..."
docker exec tala-clickhouse-prod clickhouse-client --query="BACKUP DATABASE tala_analytics TO File('${BACKUP_NAME}_clickhouse')"

# Backup Redis
echo "📦 Backing up Redis..."
docker exec tala-redis-prod redis-cli --rdb /data/dump.rdb
docker cp tala-redis-prod:/data/dump.rdb "${BACKUP_DIR}/${BACKUP_NAME}_redis.rdb"

# Cleanup old backups (keep last 30 days)
echo "🧹 Cleaning up old backups..."
find "${BACKUP_DIR}" -name "tala_backup_*" -mtime +30 -delete

echo "✅ Backup completed: ${BACKUP_DIR}/${BACKUP_NAME}"
