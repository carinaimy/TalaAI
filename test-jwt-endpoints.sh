#!/bin/bash

# JWT Authentication Test Script
# Tests all authentication endpoints and JWT validation

set -e

BASE_URL="${BASE_URL:-http://localhost:8080}"
TEST_EMAIL="test-$(date +%s)@example.com"
TEST_PASSWORD="Test123!@#"
TEST_NAME="Test User"

echo "========================================="
echo "JWT Authentication Test Suite"
echo "========================================="
echo "Base URL: $BASE_URL"
echo "Test Email: $TEST_EMAIL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ PASS${NC}: $2"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC}: $2"
        ((TESTS_FAILED++))
    fi
}

# Function to extract JSON field
extract_json_field() {
    echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | cut -d'"' -f4
}

echo "========================================="
echo "Test 1: User Registration"
echo "========================================="

REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\",
    \"fullName\": \"$TEST_NAME\"
  }")

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
    ACCESS_TOKEN=$(extract_json_field "$RESPONSE_BODY" "accessToken")
    REFRESH_TOKEN=$(extract_json_field "$RESPONSE_BODY" "refreshToken")
    USER_ID=$(echo "$RESPONSE_BODY" | grep -o '"userId":[0-9]*' | cut -d':' -f2)
    
    print_result 0 "User registration successful (HTTP $HTTP_CODE)"
    echo "  Access Token: ${ACCESS_TOKEN:0:50}..."
    echo "  Refresh Token: ${REFRESH_TOKEN:0:50}..."
    echo "  User ID: $USER_ID"
else
    print_result 1 "User registration failed (HTTP $HTTP_CODE)"
    echo "  Response: $RESPONSE_BODY"
    exit 1
fi

echo ""
echo "========================================="
echo "Test 2: Access Protected Endpoint with Valid Token"
echo "========================================="

PROFILE_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/profiles" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

HTTP_CODE=$(echo "$PROFILE_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$PROFILE_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "404" ]; then
    print_result 0 "Protected endpoint accessible with valid token (HTTP $HTTP_CODE)"
else
    print_result 1 "Protected endpoint failed with valid token (HTTP $HTTP_CODE)"
    echo "  Response: $RESPONSE_BODY"
fi

echo ""
echo "========================================="
echo "Test 3: Access Protected Endpoint without Token"
echo "========================================="

NO_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/profiles")

HTTP_CODE=$(echo "$NO_TOKEN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ]; then
    print_result 0 "Protected endpoint correctly rejected request without token (HTTP $HTTP_CODE)"
else
    print_result 1 "Protected endpoint should return 401 without token (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test 4: Access Protected Endpoint with Invalid Token"
echo "========================================="

INVALID_TOKEN_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/profiles" \
  -H "Authorization: Bearer invalid.token.here")

HTTP_CODE=$(echo "$INVALID_TOKEN_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ]; then
    print_result 0 "Protected endpoint correctly rejected invalid token (HTTP $HTTP_CODE)"
else
    print_result 1 "Protected endpoint should return 401 with invalid token (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test 5: User Login"
echo "========================================="

LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"$TEST_PASSWORD\"
  }")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$LOGIN_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    NEW_ACCESS_TOKEN=$(extract_json_field "$RESPONSE_BODY" "accessToken")
    NEW_REFRESH_TOKEN=$(extract_json_field "$RESPONSE_BODY" "refreshToken")
    
    print_result 0 "User login successful (HTTP $HTTP_CODE)"
    echo "  New Access Token: ${NEW_ACCESS_TOKEN:0:50}..."
    echo "  New Refresh Token: ${NEW_REFRESH_TOKEN:0:50}..."
else
    print_result 1 "User login failed (HTTP $HTTP_CODE)"
    echo "  Response: $RESPONSE_BODY"
fi

echo ""
echo "========================================="
echo "Test 6: Login with Wrong Password"
echo "========================================="

WRONG_PASSWORD_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$TEST_EMAIL\",
    \"password\": \"WrongPassword123\"
  }")

HTTP_CODE=$(echo "$WRONG_PASSWORD_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "400" ]; then
    print_result 0 "Login correctly rejected wrong password (HTTP $HTTP_CODE)"
else
    print_result 1 "Login should reject wrong password (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test 7: Refresh Token"
echo "========================================="

REFRESH_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

HTTP_CODE=$(echo "$REFRESH_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$REFRESH_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
    REFRESHED_ACCESS_TOKEN=$(extract_json_field "$RESPONSE_BODY" "accessToken")
    REFRESHED_REFRESH_TOKEN=$(extract_json_field "$RESPONSE_BODY" "refreshToken")
    
    print_result 0 "Token refresh successful (HTTP $HTTP_CODE)"
    echo "  Refreshed Access Token: ${REFRESHED_ACCESS_TOKEN:0:50}..."
    echo "  Refreshed Refresh Token: ${REFRESHED_REFRESH_TOKEN:0:50}..."
    
    # Update tokens for next tests
    ACCESS_TOKEN="$REFRESHED_ACCESS_TOKEN"
    REFRESH_TOKEN="$REFRESHED_REFRESH_TOKEN"
else
    print_result 1 "Token refresh failed (HTTP $HTTP_CODE)"
    echo "  Response: $RESPONSE_BODY"
fi

echo ""
echo "========================================="
echo "Test 8: Use Refresh Token as Access Token (Should Fail)"
echo "========================================="

REFRESH_AS_ACCESS_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/v1/profiles" \
  -H "Authorization: Bearer $REFRESH_TOKEN")

HTTP_CODE=$(echo "$REFRESH_AS_ACCESS_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ]; then
    print_result 0 "Refresh token correctly rejected as access token (HTTP $HTTP_CODE)"
else
    print_result 1 "Refresh token should not work as access token (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test 9: Use Access Token to Refresh (Should Fail)"
echo "========================================="

ACCESS_AS_REFRESH_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$ACCESS_TOKEN\"
  }")

HTTP_CODE=$(echo "$ACCESS_AS_REFRESH_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "401" ] || [ "$HTTP_CODE" = "400" ]; then
    print_result 0 "Access token correctly rejected for refresh (HTTP $HTTP_CODE)"
else
    print_result 1 "Access token should not work for refresh (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test 10: Health Check (Public Endpoint)"
echo "========================================="

HEALTH_RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/actuator/health")

HTTP_CODE=$(echo "$HEALTH_RESPONSE" | tail -n1)

if [ "$HTTP_CODE" = "200" ]; then
    print_result 0 "Health check accessible without authentication (HTTP $HTTP_CODE)"
else
    print_result 1 "Health check failed (HTTP $HTTP_CODE)"
fi

echo ""
echo "========================================="
echo "Test Summary"
echo "========================================="
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
echo "========================================="

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed!${NC}"
    exit 1
fi
