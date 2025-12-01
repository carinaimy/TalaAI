#!/bin/bash

# Test script for AI Service API
# Usage: ./test-api.sh [test_name]

BASE_URL="http://localhost:8085"
API_URL="${BASE_URL}/api/v1/ai/processing"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== AI Service API Test Script ===${NC}\n"

# Test 1: Health Check
test_health() {
    echo -e "${YELLOW}Test 1: Health Check${NC}"
    curl -s "${API_URL}/health" | jq .
    echo -e "\n"
}

# Test 2: Simple Feeding Event
test_feeding() {
    echo -e "${YELLOW}Test 2: Simple Feeding Event${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "Baby drank 120ml formula at 2pm",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T14:30:00"
        }' | jq .
    echo -e "\n"
}

# Test 3: Sleep Event
test_sleep() {
    echo -e "${YELLOW}Test 3: Sleep Event${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "Baby started napping at 3pm",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T15:00:00"
        }' | jq .
    echo -e "\n"
}

# Test 4: Question (not recording)
test_question() {
    echo -e "${YELLOW}Test 4: Question (Not Recording)${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "How much did baby eat today?",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T18:00:00"
        }' | jq .
    echo -e "\n"
}

# Test 5: Multiple Events
test_multiple() {
    echo -e "${YELLOW}Test 5: Multiple Events${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "Baby had 100ml milk at 10am, then slept from 11am to 1pm, and had a wet diaper at 1:30pm",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T14:00:00"
        }' | jq .
    echo -e "\n"
}

# Test 6: With Chat History
test_with_history() {
    echo -e "${YELLOW}Test 6: With Chat History${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "She had another 80ml just now",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "chatHistory": "Parent: Baby drank 120ml formula at 2pm\nTala: We'\''ve recorded Aria'\''s feeding! 💙",
            "userLocalTime": "2025-11-30T16:30:00"
        }' | jq .
    echo -e "\n"
}

# Test 7: Milestone Event
test_milestone() {
    echo -e "${YELLOW}Test 7: Milestone Event${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "Aria smiled for the first time today!",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T10:00:00"
        }' | jq .
    echo -e "\n"
}

# Test 8: Medicine Event
test_medicine() {
    echo -e "${YELLOW}Test 8: Medicine Event${NC}"
    curl -s -X POST "${API_URL}/analyze" \
        -H "Content-Type: application/json" \
        -d '{
            "userMessage": "Gave baby 2.5ml Tylenol for fever at 8am",
            "babyProfileContext": "Baby: Aria, 6 months old, Female",
            "userLocalTime": "2025-11-30T08:30:00"
        }' | jq .
    echo -e "\n"
}

# Run all tests or specific test
if [ -z "$1" ]; then
    echo -e "${GREEN}Running all tests...${NC}\n"
    test_health
    test_feeding
    test_sleep
    test_question
    test_multiple
    test_with_history
    test_milestone
    test_medicine
else
    case "$1" in
        health)
            test_health
            ;;
        feeding)
            test_feeding
            ;;
        sleep)
            test_sleep
            ;;
        question)
            test_question
            ;;
        multiple)
            test_multiple
            ;;
        history)
            test_with_history
            ;;
        milestone)
            test_milestone
            ;;
        medicine)
            test_medicine
            ;;
        *)
            echo -e "${RED}Unknown test: $1${NC}"
            echo "Available tests: health, feeding, sleep, question, multiple, history, milestone, medicine"
            exit 1
            ;;
    esac
fi

echo -e "${GREEN}=== Tests Complete ===${NC}"
