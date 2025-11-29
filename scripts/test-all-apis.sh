#!/bin/bash

# TalaAI - API Testing Script
# Tests all service endpoints systematically

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "======================================"
echo "TalaAI Backend - API Testing"
echo "======================================"
echo ""

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test function
test_endpoint() {
    local service=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_status=$5
    local description=$6
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "[$TOTAL_TESTS] Testing: $description... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" -H "Content-Type: application/json" -d "$data" "$endpoint")
    fi
    
    status_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        if [ -n "$body" ] && [ "$body" != "null" ]; then
            echo "   Response: $(echo $body | jq -c '.' 2>/dev/null || echo $body | head -c 100)"
        fi
    else
        echo -e "${RED}✗ FAIL${NC} (Expected: $expected_status, Got: $status_code)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        if [ -n "$body" ]; then
            echo "   Error: $(echo $body | head -c 200)"
        fi
    fi
    echo ""
}

# ============================================
# 1. INFRASTRUCTURE HEALTH CHECKS
# ============================================
echo -e "${BLUE}=== Infrastructure Health Checks ===${NC}"
echo ""

test_endpoint "PostgreSQL" "GET" "http://localhost:5432" "" "000" "PostgreSQL Connection"
test_endpoint "Redis" "GET" "http://localhost:6379" "" "000" "Redis Connection"

# ============================================
# 2. USER SERVICE TESTS
# ============================================
echo -e "${BLUE}=== User Service Tests ===${NC}"
echo ""

test_endpoint "user-service" "GET" "http://localhost:8084/actuator/health" "" "200" "User Service Health"

# Register a test user
test_endpoint "user-service" "POST" "http://localhost:8084/api/v1/auth/register" \
    '{"email":"test@tala.ai","password":"Test123!@#","fullName":"Test User"}' \
    "201" "Register New User"

# Try to register same user again (should fail)
test_endpoint "user-service" "POST" "http://localhost:8084/api/v1/auth/register" \
    '{"email":"test@tala.ai","password":"Test123!@#","fullName":"Test User"}' \
    "400" "Duplicate User Registration"

# Login
test_endpoint "user-service" "POST" "http://localhost:8084/api/v1/auth/login" \
    '{"email":"test@tala.ai","password":"Test123!@#"}' \
    "200" "User Login"

# Get JWT token for authenticated requests
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8084/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@tala.ai","password":"Test123!@#"}')
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.accessToken' 2>/dev/null || echo "")

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    echo -e "${GREEN}✓ JWT Token obtained${NC}"
    echo ""
    
    # Create baby profile
    test_endpoint "user-service" "POST" "http://localhost:8084/api/v1/profiles" \
        '{"babyName":"Emma","birthDate":"2024-06-15","gender":"FEMALE","timezone":"America/Los_Angeles"}' \
        "201" "Create Baby Profile" \
        "-H \"Authorization: Bearer $TOKEN\""
    
    # Get user interest scores
    test_endpoint "user-service" "GET" "http://localhost:8084/api/v1/users/interest/scores?userId=1&profileId=1" \
        "" "200" "Get Interest Scores" \
        "-H \"Authorization: Bearer $TOKEN\""
else
    echo -e "${YELLOW}⚠ Skipping authenticated tests (no token)${NC}"
    echo ""
fi

# ============================================
# 3. EVENT SERVICE TESTS
# ============================================
echo -e "${BLUE}=== Event Service Tests ===${NC}"
echo ""

test_endpoint "event-service" "GET" "http://localhost:8081/actuator/health" "" "200" "Event Service Health"

# Create event (if we have token)
if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    test_endpoint "event-service" "POST" "http://localhost:8081/api/v1/events" \
        '{"profileId":1,"userId":1,"eventType":"FEEDING","eventTime":"2024-11-29T10:30:00Z","eventData":{"type":"BOTTLE","amount":120,"unit":"ml"}}' \
        "201" "Create Feeding Event"
    
    # Get timeline
    test_endpoint "event-service" "GET" "http://localhost:8081/api/v1/events/timeline?profileId=1&startTime=2024-11-01T00:00:00Z&endTime=2024-11-30T23:59:59Z" \
        "" "200" "Get Event Timeline"
fi

# ============================================
# 4. REMINDER SERVICE TESTS
# ============================================
echo -e "${BLUE}=== Reminder Service Tests ===${NC}"
echo ""

test_endpoint "reminder-service" "GET" "http://localhost:8085/actuator/health" "" "200" "Reminder Service Health"

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    # Create reminder
    test_endpoint "reminder-service" "POST" "http://localhost:8085/api/v1/reminders" \
        '{"userId":1,"profileId":1,"category":"appointment","title":"6-month checkup","description":"Pediatrician visit","dueAt":"2024-12-15T10:00:00Z","priority":"high"}' \
        "201" "Create Reminder"
    
    # Get due reminders
    test_endpoint "reminder-service" "GET" "http://localhost:8085/api/v1/reminders/due?userId=1" \
        "" "200" "Get Due Reminders"
fi

# ============================================
# 5. MEDIA SERVICE TESTS
# ============================================
echo -e "${BLUE}=== Media Service Tests ===${NC}"
echo ""

test_endpoint "media-service" "GET" "http://localhost:8086/actuator/health" "" "200" "Media Service Health"

# ============================================
# 6. FILE SERVICE TESTS
# ============================================
echo -e "${BLUE}=== File Service Tests ===${NC}"
echo ""

test_endpoint "file-service" "GET" "http://localhost:8087/actuator/health" "" "200" "File Service Health"

# ============================================
# 7. QUERY SERVICE TESTS
# ============================================
echo -e "${BLUE}=== Query Service Tests ===${NC}"
echo ""

test_endpoint "query-service" "GET" "http://localhost:8082/actuator/health" "" "200" "Query Service Health"

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    # Get daily context
    test_endpoint "query-service" "GET" "http://localhost:8082/api/v1/analytics/daily-context?profileId=1&date=2024-11-29" \
        "" "200" "Get Daily Context"
fi

# ============================================
# 8. AI SERVICE TESTS
# ============================================
echo -e "${BLUE}=== AI Service Tests ===${NC}"
echo ""

test_endpoint "ai-service" "GET" "http://localhost:8083/actuator/health" "" "200" "AI Service Health"

# ============================================
# 9. PERSONALIZATION SERVICE TESTS
# ============================================
echo -e "${BLUE}=== Personalization Service Tests ===${NC}"
echo ""

test_endpoint "personalization-service" "GET" "http://localhost:8088/actuator/health" "" "200" "Personalization Service Health"

if [ -n "$TOKEN" ] && [ "$TOKEN" != "null" ]; then
    # Get Today page
    test_endpoint "personalization-service" "GET" "http://localhost:8088/api/v1/personalization/today?userId=1&profileId=1" \
        "" "200" "Get Today Page"
    
    # Get Insights
    test_endpoint "personalization-service" "GET" "http://localhost:8088/api/v1/personalization/insights?userId=1&profileId=1" \
        "" "200" "Get Insights Page"
    
    # Get Tala Starters
    test_endpoint "personalization-service" "GET" "http://localhost:8088/api/v1/personalization/tala-starters?userId=1&profileId=1" \
        "" "200" "Get Tala Conversation Starters"
fi

# ============================================
# SUMMARY
# ============================================
echo ""
echo "======================================"
echo -e "${BLUE}Test Summary${NC}"
echo "======================================"
echo "Total Tests:  $TOTAL_TESTS"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}✗ Some tests failed${NC}"
    exit 1
fi
