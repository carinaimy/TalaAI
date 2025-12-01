# AI Service

AI processing service for Tala baby tracking application with 3-stage pipeline for data extraction.

## Features

### 3-Stage AI Processing Pipeline

1. **Attachment Parser** (Stage 1)
   - Analyzes images, PDFs, and documents
   - Extracts text content via OCR
   - Classifies document types (daycare reports, medical records, photos, etc.)
   - Identifies key findings

2. **Chat Classifier** (Stage 2)
   - Determines user intent from message
   - Classifies interaction types:
     - `DATA_RECORDING`: User wants to log baby data
     - `QUESTION_ANSWERING`: User is asking questions
     - `GENERAL_CHAT`: Casual conversation
     - `OUT_OF_SCOPE`: Unrelated topics

3. **Event Extraction** (Stage 3)
   - Extracts structured event data from user input
   - Supports multiple event categories:
     - **JOURNAL**: Feeding, Sleep, Diaper, Pumping, Milestone, Growth
     - **HEALTH**: Sickness, Medicine, Medical Visit, Vaccination
   - Outputs JSON/TOON format for origin-data-service
   - Handles timestamp parsing and context understanding

## Technology Stack

- **AI Model**: Google Gemini 2.0 Flash
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Dependencies**: 
  - OkHttp for HTTP client
  - Jackson for JSON processing
  - PostgreSQL for data storage
  - Redis for caching
  - Kafka for event streaming
  - ClickHouse for analytics

## Setup

### Prerequisites

- Java 21+
- Maven 3.8+
- Docker & Docker Compose
- Gemini API Key (get from [Google AI Studio](https://makersuite.google.com/app/apikey))

### Configuration

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```

2. Add your Gemini API key to `.env`:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```

3. Update `application.yml` if needed (default configuration uses environment variables)

### Build

```bash
# Build the service
mvn clean package -DskipTests

# Or build with tests
mvn clean package
```

### Run Locally

```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR
java -jar target/ai-service-1.0.0-SNAPSHOT.jar
```

### Run with Docker

```bash
# Build and start all services (PostgreSQL, Redis, Kafka, ClickHouse, AI Service)
docker-compose -f docker-compose.test.yml up --build

# Stop services
docker-compose -f docker-compose.test.yml down

# View logs
docker-compose -f docker-compose.test.yml logs -f ai-service
```

## API Endpoints

### Process User Input

**POST** `/api/v1/ai/processing/analyze`

Process user input through the complete 3-stage AI pipeline.

**Request Body:**
```json
{
  "userMessage": "Baby drank 120ml formula at 2pm",
  "attachmentUrls": ["https://example.com/daycare-report.pdf"],
  "babyProfileContext": "Baby: Aria, 6 months old, Female",
  "chatHistory": "Recent conversation context...",
  "userLocalTime": "2025-11-30T14:30:00"
}
```

**Response:**
```json
{
  "success": true,
  "attachmentParserResult": {
    "overallSummary": "Daycare daily report for Aria",
    "attachmentType": "DAYCARE_REPORT",
    "attachments": [...],
    "confidence": 0.95
  },
  "chatClassificationResult": {
    "interactionType": "DATA_RECORDING",
    "classificationReason": "User is reporting baby feeding event",
    "confidence": 0.95
  },
  "eventExtractionResult": {
    "aiMessage": "We've recorded Aria's feeding! 💙",
    "intentUnderstanding": "Parent logged 120ml formula feeding at 2pm",
    "confidence": 0.95,
    "events": [
      {
        "eventCategory": "JOURNAL",
        "eventType": "FEEDING",
        "timestamp": "2025-11-30T14:00:00",
        "summary": "Fed 120ml formula",
        "eventData": {
          "amount": 120,
          "unit": "ML",
          "feeding_type": "FORMULA"
        },
        "confidence": 0.95
      }
    ],
    "clarificationNeeded": []
  }
}
```

### Health Check

**GET** `/api/v1/ai/processing/health`

Returns service health status.

## Integration with Origin-Data-Service

The AI service is designed to work with `origin-data-service` for data ingestion:

```
User Input (text/voice/attachment)
    ↓
AI Service (3-stage processing)
    ↓
Structured JSON/TOON output
    ↓
Origin-Data-Service (data storage)
    ↓
Database (original_events → timeline_entries)
```

### Integration Flow

1. **Frontend/Mobile** sends user input to AI Service
2. **AI Service** processes through 3 stages
3. **AI Service** returns structured event data
4. **Origin-Data-Service** receives structured data and stores it
5. **Timeline** displays the recorded events

## Prompt Engineering

The service uses carefully crafted prompts for each stage:

- **Attachment Parser**: Extracts comprehensive text and classifies document types
- **Chat Classifier**: Semantic understanding of user intent (not keyword-based)
- **Event Extractor**: Structured data extraction with timestamp handling and context awareness

All prompts are optimized for:
- Token efficiency
- Accuracy and reliability
- Warm, empathetic tone (for user-facing messages)
- Structured JSON output

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run integration tests
mvn verify

# Stop test environment
docker-compose -f docker-compose.test.yml down
```

### Manual Testing with cURL

```bash
# Test the processing endpoint
curl -X POST http://localhost:8085/api/v1/ai/processing/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "userMessage": "Baby drank 120ml formula just now",
    "babyProfileContext": "Baby: Aria, 6 months old",
    "userLocalTime": "2025-11-30T14:30:00"
  }'
```

## Monitoring

- **Health**: `http://localhost:8085/actuator/health`
- **Metrics**: `http://localhost:8085/actuator/metrics`
- **Prometheus**: `http://localhost:8085/actuator/prometheus`

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                  AI Service                         │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────────────────────────────────────┐  │
│  │   AIProcessingController                     │  │
│  └──────────────┬───────────────────────────────┘  │
│                 │                                   │
│  ┌──────────────▼───────────────────────────────┐  │
│  │   AIProcessingOrchestrator                   │  │
│  └──┬────────────┬────────────┬─────────────────┘  │
│     │            │            │                     │
│  ┌──▼──────┐ ┌──▼──────┐  ┌──▼──────────────┐     │
│  │Attachment│ │  Chat   │  │     Event       │     │
│  │  Parser  │ │Classifier│  │   Extraction    │     │
│  │ Service  │ │ Service │  │    Service      │     │
│  └──┬───────┘ └──┬──────┘  └──┬──────────────┘     │
│     │            │            │                     │
│  ┌──▼────────────▼────────────▼─────────────────┐  │
│  │          GeminiService                        │  │
│  │      (Gemini 2.0 Flash API)                   │  │
│  └───────────────────────────────────────────────┘  │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Gemini API key (required) | - |
| `GEMINI_MODEL` | Gemini model to use | `gemini-2.0-flash-exp` |
| `AI_SERVICE_PORT` | Service port | `8085` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection | `jdbc:postgresql://localhost:5432/tala_db` |
| `SPRING_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |

## License

Copyright © 2025 Tala Team
