# Origin Data Service

**Event Sourcing Architecture for TalaAI Baby Care Data**

## Overview

The Origin Data Service is the foundational data storage service for TalaAI, implementing an event sourcing pattern to store all original data from external sources alongside AI-processed data.

### Key Features

- **Event Sourcing**: All external data enters through `OriginalEvent` for complete audit trail
- **Data Separation**: Original data and AI-processed data stored separately
- **Replay Capability**: Can reprocess events with updated AI models
- **Idempotency**: Prevents duplicate data ingestion via `source_event_id`
- **Multi-Source Support**: DayCare, HomeEvent, HealthReport, IncidentReport

## Architecture

### Core Principles

1. **Original Event (Top Level)**: All external data enters as an `OriginalEvent`
2. **Structured Storage**: Data parsed into domain-specific tables (DayCare, Health, etc.)
3. **AI Timeline**: AI generates `TimelineEntry` records for app display
4. **Immutability**: Original events are never modified, only marked as processed

### Data Flow

```
External Source → OriginalEvent (raw JSON) → Domain Tables → AI Processing → Timeline
```

## Database Schema

### Schema: `origin_data`

#### 1. Original Events (Event Sourcing)
- `original_events` - Top-level event table with raw JSON payload

#### 2. Day Care Reports
- `daycare_reports` - Daily reports header
- `daycare_report_items` - Individual activities (feeding, sleeping, etc.)

#### 3. Home Events
- `home_events` - Parent-recorded events

#### 4. Incident Reports
- `incident_reports` - Incident records with severity tracking

#### 5. Health Reports
- `health_reports` - Medical visit header
- `health_measurements` - Height, weight, etc.
- `health_medications` - Prescribed medications
- `health_vaccinations` - Vaccination records

#### 6. Timeline (AI-Generated)
- `timeline_entries` - AI-processed display data

## API Endpoints

### Timeline API
```
GET /api/v1/timeline/profile/{profileId}       - Get timeline for profile (paginated)
GET /api/v1/timeline/{id}                      - Get specific timeline entry
GET /api/v1/timeline/profile/{profileId}/count - Count timeline entries
```

### Health Check
```
GET /api/v1/health - Service health status
```

## Configuration

### Environment Variables

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/tala_db
SPRING_DATASOURCE_USERNAME: tala
SPRING_DATASOURCE_PASSWORD: tala_dev_2025
ORIGIN_DATA_SERVICE_PORT: 8085
JWT_SECRET: <your-jwt-secret>
```

### Database Setup

The service uses Flyway for database migrations. Schema `origin_data` is automatically created and migrated on startup.

## Development

### Build

```bash
cd backend
mvn clean install -pl services/origin-data-service -am
```

### Run Locally

```bash
cd backend/services/origin-data-service
mvn spring-boot:run
```

### Docker Build

```bash
cd backend
docker build -f services/origin-data-service/Dockerfile -t tala-origin-data-service .
```

## Data Models

### Enums

- `DataSourceType`: DAY_CARE_REPORT, INCIDENT_REPORT, HEALTH_REPORT, HOME_EVENT
- `TimelineEventType`: FEEDING, SLEEPING, ACTIVITY, LEARNING, MILESTONE, etc.
- `DayCareReportType`: Activity types within daycare reports
- `HealthReportType`: PHYSICAL_EXAM, SICK_VISIT, VACCINATION, MEDICATION
- `HomeEventType`: Home event categories
- `IncidentSeverity`: LOW, MEDIUM, HIGH, CRITICAL

### Key Entities

All entities extend `BaseEntity` which provides:
- `id` (Snowflake ID)
- `createdAt`, `updatedAt`
- `deletedAt` (soft delete support)

## Event Sourcing Pattern

### Creating New Events

1. **Receive External Data**: API receives data from external source
2. **Create OriginalEvent**: Store raw JSON in `original_events` table
3. **Parse to Domain**: Create domain-specific records (DayCare, Health, etc.)
4. **Link to Original**: All domain records reference `original_event_id`
5. **AI Processing**: Background job processes event and creates timeline entries
6. **Mark Processed**: Update `ai_processed = true` on original event

### Replay Capability

To reprocess events with updated AI:
1. Query unprocessed or specific events
2. Run AI processing pipeline
3. Update or create new timeline entries
4. Update `ai_processed_at` timestamp

## Integration Points

### Upstream Services
- DayCare webhook integrations
- Parent mobile app (HomeEvent creation)
- Health provider integrations
- Incident reporting systems

### Downstream Services
- AI Service (for timeline generation)
- Query Service (for analytics)
- Mobile App (timeline display)

## Security

- JWT-based authentication
- All endpoints (except actuator) require authentication
- Profile-based data isolation

## Monitoring

### Actuator Endpoints
```
/actuator/health
/actuator/metrics
/actuator/prometheus
```

## Future Enhancements

1. **Kafka Integration**: Stream events to other services
2. **Webhook Support**: Receive data from external systems
3. **Batch Import**: Bulk data import capabilities
4. **Data Export**: Export original data for compliance
5. **Version Tracking**: Track AI model versions for timeline entries

## License

Proprietary - TalaAI
