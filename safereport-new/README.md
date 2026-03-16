# SafeReport Backend API

Spring Boot REST API for the SafeReport anonymous complaint platform.

---

## Tech Stack
- Java 17 + Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA + MySQL 8
- Lombok, Jackson

---

## Quick Start

### 1. Create MySQL Database
```sql
CREATE DATABASE safereport_db;
```

### 2. Configure application.properties
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/safereport_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Run
```bash
mvn spring-boot:run
```

Server starts at: `http://localhost:8080`

On first startup, the system automatically seeds:
- **Admin account**: `admin@safereport.com` / `Admin@123`
- **8 complaint categories**

---

## Authentication

All protected endpoints require:
```
Authorization: Bearer <token>
```

JWT tokens are returned on login/register.

---

## API Reference

### Auth  `/api/auth`

| Method | Endpoint | Body | Auth | Description |
|--------|----------|------|------|-------------|
| POST | `/register` | `{fullName, email, password, phone, role?, verificationCode?}` | No | Register user or authority |
| POST | `/login` | `{email, password}` | No | Login |
| POST | `/verify-email` | `{email, code}` | No | Verify email |
| POST | `/forgot-password` | `{email}` | No | Request password reset |
| POST | `/reset-password` | `{token, newPassword, confirmPassword}` | No | Reset password |

**Role values**: `USER` (default), `AUTHORITY` (needs `verificationCode: "AUTH-CODE-001"`)

**Register example:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "secret123",
  "phone": "9876543210",
  "role": "USER"
}
```

**Login response:**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "fullName": "John Doe",
    "email": "john@example.com",
    "role": "USER",
    "accessToken": "eyJ...",
    "tokenType": "Bearer"
  }
}
```

---

### Complaints  `/api/complaints`

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/` | Optional | All | Submit complaint (multipart/form-data) |
| GET | `/track/{trackingId}` | No | Public | Track complaint by ID |
| GET | `/my` | Yes | USER | My complaints |
| GET | `/{id}` | Yes | All | Get complaint by ID |
| GET | `/{id}/evidence` | Yes | All | List evidence |
| GET | `/{id}/history` | Yes | All | Update history |
| GET | `/` | Yes | ADMIN, AUTHORITY | All complaints (with filters) |
| POST | `/{id}/assign` | Yes | ADMIN | Assign to authority |
| PATCH | `/{id}/status` | Yes | ADMIN, AUTHORITY | Update status/priority |
| GET | `/authority/assigned` | Yes | AUTHORITY | My assigned complaints |
| POST | `/{id}/investigation-update` | Yes | AUTHORITY | Add investigation note |

**Submit complaint (multipart/form-data):**
```
POST /api/complaints
Content-Type: multipart/form-data

complaint (part): {
  "title": "Road pothole on Main St",
  "description": "Large pothole causing accidents",
  "categoryId": 6,
  "priority": "HIGH",
  "location": "Main Street, Block 4",
  "incidentDate": "2025-03-10",
  "anonymous": false
}
files (part, optional): [file1, file2]
```

**Complaint status flow:**
```
SUBMITTED → UNDER_REVIEW → ASSIGNED → INVESTIGATING → RESOLVED → CLOSED
                                                              ↘ REJECTED
```

**Filter complaints (Admin):**
```
GET /api/complaints?status=SUBMITTED&priority=HIGH&categoryId=1&keyword=road&page=0&size=10
```

**Assign complaint:**
```json
POST /api/complaints/5/assign
{
  "authorityId": 3,
  "notes": "Please investigate urgently"
}
```

**Update status:**
```json
PATCH /api/complaints/5/status
{
  "status": "INVESTIGATING",
  "priority": "URGENT",
  "notes": "Investigation has started",
  "notifyComplainant": true
}
```

---

### Evidence  `/api/evidence`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/upload/{complaintId}` | Yes | Upload evidence file |
| GET | `/complaint/{complaintId}` | Yes | List evidence |
| GET | `/download/{evidenceId}` | Yes | Download evidence file |

**Upload:**
```
POST /api/evidence/upload/5
Content-Type: multipart/form-data
file: <file>
description: "Screenshot of the incident"
```

---

### User  `/api/user`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/profile` | Yes | Get my profile |
| PUT | `/profile` | Yes | Update profile |
| PUT | `/change-password` | Yes | Change password |

**Change password:**
```json
{
  "currentPassword": "old123",
  "newPassword": "new456",
  "confirmPassword": "new456"
}
```

---

### Notifications  `/api/notifications`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Yes | Get notifications (paginated) |
| GET | `/unread-count` | Yes | Get unread count |
| PATCH | `/{id}/read` | Yes | Mark as read |
| PATCH | `/read-all` | Yes | Mark all as read |

---

### Categories  `/api/categories`

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| GET | `/` | No | Public | List active categories |
| GET | `/all` | Yes | ADMIN | List all categories |
| POST | `/` | Yes | ADMIN | Create category |
| PUT | `/{id}` | Yes | ADMIN | Update category |
| PATCH | `/{id}/toggle` | Yes | ADMIN | Enable/disable category |

---

### Admin  `/api/admin`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/dashboard` | Yes (ADMIN) | Analytics dashboard stats |
| GET | `/users` | Yes (ADMIN) | All users (filter by `?role=USER`) |
| GET | `/users/{userId}` | Yes (ADMIN) | Get specific user |
| GET | `/authorities` | Yes (ADMIN) | List all authorities |
| PATCH | `/users/{userId}/toggle-status` | Yes (ADMIN) | Enable/disable user |
| DELETE | `/users/{userId}` | Yes (ADMIN) | Delete user |
| GET | `/audit-logs` | Yes (ADMIN) | Audit logs (filter by `?userId=`) |

**Dashboard response:**
```json
{
  "totalComplaints": 120,
  "submitted": 30,
  "underReview": 15,
  "assigned": 20,
  "investigating": 10,
  "resolved": 40,
  "closed": 5,
  "rejected": 0,
  "totalUsers": 85,
  "totalAuthorities": 8,
  "activeUsers": 90,
  "byCategory": {
    "Corruption": 25,
    "Fraud": 18,
    "Infrastructure": 30
  }
}
```

---

### Support Tickets  `/api/support/tickets`

| Method | Endpoint | Auth | Roles | Description |
|--------|----------|------|-------|-------------|
| POST | `/` | Yes | All | Create ticket |
| GET | `/my` | Yes | All | My tickets |
| GET | `/{id}` | Yes | All | Get ticket |
| GET | `/` | Yes | ADMIN | All tickets |
| POST | `/{id}/respond` | Yes | ADMIN | Respond to ticket |
| PATCH | `/{id}/status` | Yes | ADMIN | Update ticket status |

---

## Standard Response Format

All API responses follow this structure:
```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "timestamp": "2025-03-14T10:30:00"
}
```

Error response:
```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2025-03-14T10:30:00"
}
```

---

## Roles & Permissions

| Role | Can Do |
|------|--------|
| `USER` | Submit complaints, view own complaints, manage profile |
| `AUTHORITY` | View assigned complaints, update status, add investigation notes |
| `ADMIN` | Everything — manage users, assign complaints, view analytics |

---

## Default Credentials (seeded on startup)

| Email | Password | Role |
|-------|----------|------|
| `admin@safereport.com` | `Admin@123` | ADMIN |

---

## File Upload Notes
- Max file size: **10MB** per file
- Max request size: **50MB**
- Uploaded files stored in: `uploads/<complaint_id>/`
- Allowed file types: any (validate on frontend)
