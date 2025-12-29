#!/bin/bash
# Test REST API endpoints

BASE_URL="${BASE_URL:-http://localhost:8080}"
API_BASE="${BASE_URL}/api"

echo "Testing REST API Endpoints..."
echo "=============================="
echo "Base URL: ${BASE_URL}"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

# Function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4
    local auth_token=$5
    
    echo -n "Testing: ${description}... "
    
    local headers=(-H "Content-Type: application/json")
    if [ -n "$auth_token" ]; then
        headers+=(-H "Authorization: Bearer ${auth_token}")
    fi
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "${API_BASE}${endpoint}" "${headers[@]}")
    elif [ "$method" = "POST" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X POST "${API_BASE}${endpoint}" "${headers[@]}" -d "$data")
        else
            response=$(curl -s -w "\n%{http_code}" -X POST "${API_BASE}${endpoint}" "${headers[@]}")
        fi
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "${API_BASE}${endpoint}" "${headers[@]}" -d "$data")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "${API_BASE}${endpoint}" "${headers[@]}")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASSED++))
        echo "$body" | python3 -m json.tool 2>/dev/null | head -20 || echo "$body" | head -5
        echo ""
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
        ((FAILED++))
        echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        echo ""
        return 1
    fi
}

# Test 1: Health check / Server status
echo "=== Server Status ==="
echo -n "Testing: Health check... "
health_response=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/actuator/health")
health_code=$(echo "$health_response" | tail -n1)
if [ "$health_code" -ge 200 ] && [ "$health_code" -lt 300 ]; then
    echo -e "${GREEN}✓ PASS${NC} (HTTP $health_code)"
    ((PASSED++))
else
    echo -e "${YELLOW}SKIP${NC} (HTTP $health_code - may require authentication)"
fi
echo ""

# Test 2: Register a new user
echo "=== Authentication Tests ==="
REGISTER_DATA='{"email":"testuser@example.com","password":"Test1234!","firstName":"Test","lastName":"User"}'
test_endpoint "POST" "/auth/register" "$REGISTER_DATA" "User registration"

# Extract token from registration response
TOKEN=$(curl -s -X POST "${API_BASE}/auth/register" \
    -H "Content-Type: application/json" \
    -d "$REGISTER_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))" 2>/dev/null)

if [ -z "$TOKEN" ] || [ "$TOKEN" = "None" ]; then
    echo -e "${YELLOW}Warning: Could not extract token from registration. Trying login...${NC}"
    LOGIN_DATA='{"email":"testuser@example.com","password":"Test1234!"}'
    TOKEN=$(curl -s -X POST "${API_BASE}/auth/login" \
        -H "Content-Type: application/json" \
        -d "$LOGIN_DATA" | python3 -c "import sys, json; print(json.load(sys.stdin).get('token', ''))" 2>/dev/null)
fi

if [ -n "$TOKEN" ] && [ "$TOKEN" != "None" ]; then
    echo -e "${GREEN}✓ Token obtained: ${TOKEN:0:20}...${NC}"
    echo ""
    
    # Test 3: Get current user
    test_endpoint "GET" "/users/me" "" "Get current user" "$TOKEN"
    
    # Test 4: Login
    LOGIN_DATA='{"email":"testuser@example.com","password":"Test1234!"}'
    test_endpoint "POST" "/auth/login" "$LOGIN_DATA" "User login"
    
    # Test 5: Refresh token
    REFRESH_DATA="{\"token\":\"${TOKEN}\"}"
    test_endpoint "POST" "/auth/refresh" "$REFRESH_DATA" "Token refresh"
else
    echo -e "${RED}✗ Could not obtain authentication token. Skipping authenticated tests.${NC}"
    echo ""
fi

# Test 6: Get all categories
echo "=== Category Tests ==="
test_endpoint "GET" "/categories" "" "Get all categories" ""

# Test 7: Get all tags
echo "=== Tag Tests ==="
test_endpoint "GET" "/tags" "" "Get all tags" ""

# Test 8: Get all articles (public endpoint)
echo "=== Article Tests ==="
test_endpoint "GET" "/articles?page=0&size=5" "" "Get articles (paginated)" ""

# Test 9: Get featured articles
test_endpoint "GET" "/articles/featured?limit=5" "" "Get featured articles" ""

# Test 10: Get recent articles
test_endpoint "GET" "/articles/recent?limit=5" "" "Get recent articles" ""

# Test 11: Search articles
echo "=== Search Tests ==="
test_endpoint "GET" "/search/articles?query=test&page=0&size=5" "" "Search articles" ""

# Test 12: Get article statistics
echo "=== Statistics Tests ==="
if [ -n "$TOKEN" ] && [ "$TOKEN" != "None" ]; then
    test_endpoint "GET" "/statistics/articles" "" "Get article statistics" "$TOKEN"
    test_endpoint "GET" "/statistics/publication" "" "Get publication statistics" "$TOKEN"
else
    echo -e "${YELLOW}Skipping statistics tests (requires authentication)${NC}"
    echo ""
fi

# Test 13: Get all publication issues
echo "=== Publication Issue Tests ==="
test_endpoint "GET" "/publication-issues" "" "Get all publication issues" ""

# Test 14: Media upload (if token available)
echo "=== Media Upload Test ==="
if [ -n "$TOKEN" ] && [ "$TOKEN" != "None" ]; then
    echo -n "Testing: Media upload... "
    # Create a small test image file
    TEST_IMAGE="/tmp/test_image.png"
    # Create a 1x1 PNG using ImageMagick or skip if not available
    if command -v convert &> /dev/null; then
        convert -size 1x1 xc:white "$TEST_IMAGE" 2>/dev/null
    elif command -v magick &> /dev/null; then
        magick -size 1x1 xc:white "$TEST_IMAGE" 2>/dev/null
    else
        echo -e "${YELLOW}SKIP${NC} (ImageMagick not available for test image creation)"
        echo ""
    fi
    
    if [ -f "$TEST_IMAGE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "${API_BASE}/media/upload" \
            -H "Authorization: Bearer ${TOKEN}" \
            -F "file=@${TEST_IMAGE}" \
            -F "type=IMAGE" \
            -F "altText=Test image")
        
        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')
        
        if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
            echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
            ((PASSED++))
            echo "$body" | python3 -m json.tool 2>/dev/null | head -10 || echo "$body" | head -5
        else
            echo -e "${RED}✗ FAIL${NC} (HTTP $http_code)"
            ((FAILED++))
            echo "$body" | python3 -m json.tool 2>/dev/null || echo "$body"
        fi
        rm -f "$TEST_IMAGE"
        echo ""
    fi
else
    echo -e "${YELLOW}Skipping media upload test (requires authentication)${NC}"
    echo ""
fi

# Summary
echo "=============================="
echo "Test Summary:"
echo -e "${GREEN}Passed: ${PASSED}${NC}"
echo -e "${RED}Failed: ${FAILED}${NC}"
echo "=============================="

if [ $FAILED -eq 0 ]; then
    exit 0
else
    exit 1
fi

