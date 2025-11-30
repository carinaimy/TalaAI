# JWT Authentication Implementation

## Overview

Complete JWT authentication system with access tokens, refresh tokens, and comprehensive validation across all services.

## Architecture

```
┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌─────────────┐
│ Client  │─────▶│   Gateway    │─────▶│ User Service │─────▶│  Database   │
└─────────┘      │  Port 8080   │      │  Port 8084   │      └─────────────┘
                 └──────────────┘      └──────────────┘
                        │
                        ├─ JwtAuthenticationGatewayFilter
                        │  • Validates JWT signature (HMAC-SHA256)
                        │  • Verifies token type (access only)
                        │  • Extracts userId & email
                        │  • Adds X-User-Id & X-User-Email headers
                        │  • Returns 401 if invalid
                        │
                        └─ Routes to services
                                │
                                └─ JwtAuthenticationFilter
                                   • Validates JWT again (defense in depth)
                                   • Sets Spring SecurityContext
                                   • Protects all endpoints
```

## Token Types

### Access Token
- **Purpose**: API authentication
- **Expiration**: 1 hour (3600 seconds)
- **Usage**: Bearer token in Authorization header
- **Claims**: userId, email, tokenType=access, iat, exp

### Refresh Token
- **Purpose**: Obtain new access tokens
- **Expiration**: 7 days (604800 seconds)
- **Usage**: POST to /api/v1/auth/refresh
- **Claims**: userId, email, tokenType=refresh, iat, exp

## API Endpoints

### 1. Register
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "John Doe"
}

Response (201):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe"
}
```

### 2. Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe"
}
```

### 3. Refresh Token
```bash
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response (200):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "userId": 1,
  "email": "user@example.com",
  "fullName": "John Doe"
}
```

### 4. Protected Endpoints
```bash
GET /api/v1/profiles
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response (200): [profile data]
Response (401): Unauthorized
```

## Security Features

### ✅ Implemented

1. **JWT Signature Verification**
   - HMAC-SHA256 algorithm
   - Secret key validation
   - Expiration checking

2. **Token Type Validation**
   - Access tokens for API calls
   - Refresh tokens only for /auth/refresh
   - Prevents token misuse

3. **Password Security**
   - BCrypt hashing with salt
   - No plaintext storage
   - Secure password verification

4. **Gateway Protection**
   - All requests validated at gateway
   - Public paths whitelisted
   - 401 for missing/invalid tokens

5. **Service Protection**
   - Spring Security filter chain
   - All endpoints protected except auth
   - User context in SecurityContext

6. **Defense in Depth**
   - Gateway validates tokens
   - Services re-validate tokens
   - Multiple layers of security

## Configuration

### Environment Variables

```bash
# JWT Secret (MUST be 64+ characters in production)
JWT_SECRET=your-super-secret-key-minimum-64-characters-long-for-production

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/tala_db
SPRING_DATASOURCE_USERNAME=tala
SPRING_DATASOURCE_PASSWORD=tala_dev_2025

# Service Ports
GATEWAY_SERVICE_PORT=8080
USER_SERVICE_PORT=8084
```

### application.yml

```yaml
jwt:
  secret: ${JWT_SECRET:dev-secret-key-change-in-production-minimum-64-characters-long}
  expiration: 3600000  # 1 hour for access token
```

## Testing

### Run Test Suite
```bash
cd /Users/robert/Documents/Projects/TalaAI/backend
./test-jwt-endpoints.sh
```

### Manual Testing

#### 1. Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#",
    "fullName": "Test User"
  }'
```

#### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!@#"
  }'
```

#### 3. Access Protected Endpoint
```bash
# Save access token from login response
ACCESS_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl http://localhost:8080/api/v1/profiles \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

#### 4. Refresh Token
```bash
# Save refresh token from login response
REFRESH_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}"
```

## Common Errors

### 401 Unauthorized
- **Cause**: Missing, invalid, or expired token
- **Solution**: Login again to get new tokens

### 400 Bad Request
- **Cause**: Invalid request body or validation error
- **Solution**: Check request format and required fields

### 403 Forbidden
- **Cause**: Valid token but insufficient permissions
- **Solution**: Check user roles and permissions

## Token Validation Flow

### Gateway Layer
1. Extract Authorization header
2. Validate Bearer prefix
3. Extract JWT token
4. Verify signature with secret
5. Check expiration
6. Verify token type is "access"
7. Extract userId and email
8. Add X-User-Id and X-User-Email headers
9. Forward to service

### Service Layer
1. Check X-User-Id header (from gateway)
2. If present, trust gateway validation
3. If not present, validate JWT directly
4. Verify token type is "access"
5. Set Spring SecurityContext
6. Allow request to proceed

## Best Practices

### ✅ DO
- Store JWT secret in environment variables
- Use HTTPS in production
- Implement token refresh before expiration
- Validate token type (access vs refresh)
- Log authentication failures
- Use strong passwords (BCrypt)

### ❌ DON'T
- Store tokens in localStorage (use httpOnly cookies or secure storage)
- Use short secrets (minimum 64 characters)
- Share refresh tokens between clients
- Use refresh tokens as access tokens
- Hardcode secrets in code
- Store passwords in plaintext

## Production Checklist

- [ ] Set strong JWT_SECRET (64+ characters)
- [ ] Enable HTTPS
- [ ] Configure CORS properly
- [ ] Set up rate limiting
- [ ] Enable audit logging
- [ ] Configure token expiration times
- [ ] Set up monitoring and alerts
- [ ] Implement token revocation (if needed)
- [ ] Add refresh token rotation
- [ ] Configure secure cookie settings

## Files Modified/Created

### Common-Core
- `JwtConstants.java` - JWT constants and configuration
- `JwtUtils.java` - JWT generation and validation utilities
- `JwtAuthenticationFilter.java` - Reusable JWT filter for services
- `JwtSecurityConfig.java` - Base security configuration
- `SecurityContextHolder.java` - User context access utility
- `pom.xml` - Added jjwt and Spring Security dependencies

### User-Service
- `JwtService.java` - JWT service with access/refresh token generation
- `AuthService.java` - Authentication logic with BCrypt
- `AuthController.java` - Auth endpoints (register, login, refresh)
- `AuthResponse.java` - Updated with accessToken, refreshToken, expiresIn
- `RefreshTokenRequest.java` - Refresh token request DTO
- `JwtAuthenticationFilter.java` - JWT validation filter
- `SecurityConfig.java` - Security configuration with JWT filter

### Gateway-Service
- `JwtAuthenticationGatewayFilter.java` - Global JWT validation filter
- `application.yml` - Added JWT configuration

## Support

For issues or questions:
1. Check logs in gateway-service and user-service
2. Verify JWT secret is consistent across services
3. Test with curl commands above
4. Run test suite: `./test-jwt-endpoints.sh`
