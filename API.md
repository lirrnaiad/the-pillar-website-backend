# The Pillar REST API Documentation

Complete API documentation for The Pillar E-Publication Website backend.

## Base URL

```
http://localhost:8080/api
```

Production: `https://yourdomain.com/api`

## Authentication

All authenticated endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <your-jwt-token>
```

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "roleId": 1
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  },
  "expiresIn": 86400,
  "tokenType": "Bearer"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```

**Response:** Same as register

#### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response:** New token with extended expiration

---

## Articles

### List Articles
```http
GET /api/articles?page=0&size=10&status=PUBLISHED&categoryId=1&sortField=publishedAt&sortDirection=DESC
```

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 10, max: 100) - Items per page
- `status` - Filter by status (DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED, REJECTED)
- `categoryId` - Filter by category
- `tagIds` - Filter by tags (comma-separated)
- `issueId` - Filter by publication issue
- `authorId` - Filter by author
- `featured` - Filter featured articles (true/false)
- `search` - Full-text search query
- `sortField` - Field to sort by (title, createdAt, updatedAt, publishedAt, viewCount)
- `sortDirection` - ASC or DESC

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Article Title",
      "slug": "article-title",
      "content": "Article content...",
      "excerpt": "Article excerpt...",
      "status": "PUBLISHED",
      "featured": true,
      "viewCount": 150,
      "author": {...},
      "category": {...},
      "tags": [...],
      "publishedAt": "2025-12-30T00:00:00"
    }
  ],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### Get Article by ID
```http
GET /api/articles/{id}
```

### Get Article by Slug
```http
GET /api/articles/slug/{slug}
```

### Create Article
```http
POST /api/articles
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "New Article",
  "content": "Article content...",
  "excerpt": "Article excerpt...",
  "status": "DRAFT",
  "categoryId": "1",
  "tagIds": ["1", "2"],
  "issueId": "1",
  "slug": "new-article",
  "coverId": 1,
  "metaTitle": "SEO Title",
  "metaDescription": "SEO Description",
  "ogImage": "https://example.com/image.jpg"
}
```

**Response:** `201 Created` with article object

### Update Article
```http
PUT /api/articles/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content...",
  ...
}
```

### Delete Article
```http
DELETE /api/articles/{id}
Authorization: Bearer <token>
```

**Response:** `204 No Content`

### Publish Article
```http
POST /api/articles/{id}/publish
Authorization: Bearer <token>
```

### Archive Article
```http
POST /api/articles/{id}/archive
Authorization: Bearer <token>
```

### Reject Article
```http
POST /api/articles/{id}/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "reason": "Does not meet editorial standards"
}
```

### Restore Article
```http
POST /api/articles/{id}/restore
Authorization: Bearer <token>
```

### Increment View Count
```http
POST /api/articles/{id}/views
```

**Response:** Updated article with incremented view count

### Toggle Featured Status
```http
PUT /api/articles/{id}/featured
Authorization: Bearer <token>
Content-Type: application/json

{
  "featured": true
}
```

### Get Featured Articles
```http
GET /api/articles/featured?limit=5
```

### Get Recent Articles
```http
GET /api/articles/recent?limit=5
```

### Get Articles by Category
```http
GET /api/articles/category/{categorySlug}?page=0&size=10
```

---

## Users

### List Users
```http
GET /api/users
Authorization: Bearer <token>
```

### Get User by ID
```http
GET /api/users/{id}
Authorization: Bearer <token>
```

### Get Current User
```http
GET /api/users/me
Authorization: Bearer <token>
```

### Create User
```http
POST /api/users
Authorization: Bearer <token>
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "Password123!",
  "firstName": "Jane",
  "lastName": "Smith",
  "roleId": 2
}
```

### Update User
```http
PUT /api/users/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Updated Name",
  "lastName": "Updated Last",
  "bio": "User bio..."
}
```

### Delete User
```http
DELETE /api/users/{id}
Authorization: Bearer <token>
```

### Change Password
```http
POST /api/users/{id}/change-password
Authorization: Bearer <token>
Content-Type: application/json

{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

---

## Categories

### List Categories
```http
GET /api/categories
```

### Get Category by ID
```http
GET /api/categories/{id}
```

### Get Category by Slug
```http
GET /api/categories/slug/{slug}
```

### Create Category
```http
POST /api/categories
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "News",
  "slug": "news",
  "description": "News category",
  "color": "#E53935"
}
```

### Update Category
```http
PUT /api/categories/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Name",
  "description": "Updated description"
}
```

### Delete Category
```http
DELETE /api/categories/{id}
Authorization: Bearer <token>
```

---

## Tags

### List Tags
```http
GET /api/tags
```

### Get Tag by ID
```http
GET /api/tags/{id}
```

### Get Tag by Slug
```http
GET /api/tags/slug/{slug}
```

### Create Tag
```http
POST /api/tags
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Technology",
  "slug": "technology"
}
```

### Update Tag
```http
PUT /api/tags/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Name"
}
```

### Delete Tag
```http
DELETE /api/tags/{id}
Authorization: Bearer <token>
```

---

## Media

### List Media
```http
GET /api/media
```

### Get Media by ID
```http
GET /api/media/{id}
```

### Upload Media
```http
POST /api/media/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: <binary>
type: IMAGE
altText: "Image description"
folder: "articles"
```

**Response:**
```json
{
  "id": 1,
  "url": "https://res.cloudinary.com/...",
  "publicId": "pillar-uploads/...",
  "type": "IMAGE",
  "altText": "Image description",
  "width": 1920,
  "height": 1080,
  "sizeBytes": 245760,
  "uploadedBy": {...},
  "createdAt": "2025-12-30T00:00:00"
}
```

### Delete Media
```http
DELETE /api/media/{id}
Authorization: Bearer <token>
```

---

## Publication Issues

### List Issues
```http
GET /api/publication-issues
```

### Get Issue by ID
```http
GET /api/publication-issues/{id}
```

### Get Issue by Slug
```http
GET /api/publication-issues/slug/{slug}
```

### Create Issue
```http
POST /api/publication-issues
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Volume 1, Issue 1",
  "slug": "volume-1-issue-1",
  "description": "First issue description",
  "publishedAt": "2025-12-30T00:00:00",
  "coverImageId": 1
}
```

### Update Issue
```http
PUT /api/publication-issues/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Updated Title",
  "description": "Updated description"
}
```

### Delete Issue
```http
DELETE /api/publication-issues/{id}
Authorization: Bearer <token>
```

---

## Search

### Search Articles
```http
GET /api/search/articles?query=technology&page=0&size=10
```

**Response:** Paginated list of articles matching the search query

### Search Highlights
```http
GET /api/search/highlights?query=technology&limit=10
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Article Title",
    "snippet": "Article content with <mark>highlighted</mark> search terms..."
  }
]
```

---

## Statistics

### Get Article Statistics
```http
GET /api/statistics/articles
Authorization: Bearer <token>
```

**Response:**
```json
{
  "totalArticles": 100,
  "publishedArticles": 75,
  "draftArticles": 20,
  "totalViews": 50000,
  "avgViewsPerArticle": 500.0
}
```

### Get Publication Statistics
```http
GET /api/statistics/publication
Authorization: Bearer <token>
```

**Response:**
```json
{
  "totalIssues": 10,
  "totalArticles": 100,
  "totalUsers": 25,
  "totalMedia": 500
}
```

---

## Article Revisions

### Get Revision History
```http
GET /api/articles/{articleId}/revisions
Authorization: Bearer <token>
```

### Restore from Revision
```http
POST /api/articles/revisions/{revisionId}/restore
Authorization: Bearer <token>
```

---

## Audit Logs

### Get Audit Logs
```http
GET /api/audit-logs?entityType=Article&entityId=1&action=UPDATE&page=0&size=20
Authorization: Bearer <token>
```

**Query Parameters:**
- `entityType` - Filter by entity type (Article, User, etc.)
- `entityId` - Filter by entity ID
- `action` - Filter by action (CREATE, UPDATE, DELETE, etc.)
- `userId` - Filter by user ID
- `page` - Page number
- `size` - Items per page

---

## Error Responses

All errors follow a consistent format:

```json
{
  "error": "Error Type",
  "message": "Detailed error message",
  "timestamp": "2025-12-30T01:00:00",
  "status": 400
}
```

### Common HTTP Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `204 No Content` - Success with no body
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate slug)
- `500 Internal Server Error` - Server error

---

## Frontend Integration Examples

### JavaScript/TypeScript (Fetch API)

```typescript
// Login
const login = async (email: string, password: string) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });
  
  const data = await response.json();
  localStorage.setItem('token', data.token);
  return data;
};

// Authenticated Request
const getArticles = async (token: string) => {
  const response = await fetch('http://localhost:8080/api/articles', {
    headers: {
      'Authorization': `Bearer ${token}`,
    },
  });
  
  return response.json();
};

// Upload Media
const uploadMedia = async (file: File, token: string) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', 'IMAGE');
  formData.append('altText', 'Image description');
  
  const response = await fetch('http://localhost:8080/api/media/upload', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
    },
    body: formData,
  });
  
  return response.json();
};
```

### Axios Example

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Add token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Login
const login = async (email: string, password: string) => {
  const response = await api.post('/auth/login', { email, password });
  localStorage.setItem('token', response.data.token);
  return response.data;
};

// Get Articles
const getArticles = async () => {
  const response = await api.get('/articles', {
    params: { page: 0, size: 10, status: 'PUBLISHED' },
  });
  return response.data;
};
```

---

## Rate Limiting

Currently not implemented. Consider implementing rate limiting for production.

## CORS

CORS is configured to allow requests from configured origins. See README.md for configuration details.

## Pagination

All list endpoints support pagination using Spring Data's `Page` format:
- `page` - Zero-based page index
- `size` - Number of items per page (max 100)
- Response includes `totalElements`, `totalPages`, `number`, `size`

