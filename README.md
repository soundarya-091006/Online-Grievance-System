# 🛡️ SafeReport — Women's Grievance Reporting System

<div align="center">

![SafeReport Banner](https://img.shields.io/badge/SafeReport-v1.0.0-6366f1?style=for-the-badge&logo=shield&logoColor=white)
![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL_8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**A secure, full-stack grievance management platform for women to report, track, and resolve complaints confidentially.**

[Features](#-features) · [Tech Stack](#-tech-stack) · [Getting Started](#-getting-started) · [API Reference](#-api-reference) · [Screenshots](#-project-structure)

</div>

---

## 📋 Overview

SafeReport is a web-based grievance reporting system that allows women to securely submit complaints, track their status in real time, and receive updates from assigned authorities. The platform supports anonymous submissions, evidence uploads, and end-to-end case management by administrators and authority officers.

---

## ✨ Features

### 👤 User Portal
- Submit complaints — anonymous or identified
- Upload supporting evidence (images, documents, videos)
- Track complaint status in real time with a timeline
- Receive email & in-app notifications on every update
- Manage profile and change password securely

### 🏛️ Authority Portal
- View all assigned cases with complainant contact details
- Add investigation updates and notes
- Upload and manage evidence files (authenticated download)
- Close cases with formal resolution summaries
- Access investigation timeline and history

### 🔐 Admin Portal
- Full complaint management — review, approve, reject, assign
- User & authority management (enable/disable/delete)
- Real-time analytics dashboard with charts
- Audit logs tracking every system action
- Send custom notifications and request more details from complainants
- Export complaints as CSV

### 🔒 Security
- JWT-based stateless authentication
- Role-based access control (USER / AUTHORITY / ADMIN)
- BCrypt password hashing
- Anonymous complaint support (identity never stored)
- Password reset via email token

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Backend** | Java 17, Spring Boot 3.2.5 |
| **Security** | Spring Security, JWT (jjwt) |
| **Database** | MySQL 8, Spring Data JPA / Hibernate |
| **Email** | Spring Mail (Gmail SMTP) |
| **Frontend** | Vanilla HTML, CSS, JavaScript |
| **Build** | Maven |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- Maven 3.8+
- Gmail account with App Password (for email features)
- Any static file server (VS Code Live Server, Python http.server, etc.)

---

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/safereport.git
cd safereport
```

---

### 2. Set Up the Database

```sql
CREATE DATABASE safereport_db;
```

---

### 3. Configure the Backend

Edit `backend/safereport-new/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/safereport_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JWT Secret (change this in production)
app.jwt.secret=your-secret-key-minimum-32-characters-long
app.jwt.expiration=86400000

# Email (Gmail SMTP with App Password)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

> **Gmail App Password**: Go to [myaccount.google.com](https://myaccount.google.com) → Security → 2-Step Verification → App passwords → Generate.

---

### 4. Run the Backend

```bash
cd backend/safereport-new
mvn spring-boot:run
```

The server starts at `http://localhost:8080`

On first startup, the system automatically seeds:
- **Admin account**: `admin@safereport.com` / `Admin@123`
- **8 complaint categories**

---

### 5. Run the Frontend

```bash
cd frontend/frontend
python -m http.server 3000
```

Then open `http://localhost:3000` in your browser.

> Alternatively use **VS Code Live Server** extension — right-click `index.html` → Open with Live Server.

---

### Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@safereport.com` | `Admin@123` |
| Authority | *(Create via Admin panel)* | — |
| User | *(Register via `/register.html`)* | — |

> **Authority Registration**: Use verification code `AUTH-CODE-001` during registration.

---

## 📁 Project Structure

```
grievance system/
├── safereport-new/
│       |
│       ├── src/main/java/com/safereport/
│       │   ├── config/          # Security, CORS, data seeding
│       │   ├── controller/      # REST API controllers
│       │   ├── dto/             # Request/Response DTOs
│       │   ├── entity/          # JPA entities
│       │   ├── enums/           # Status, Role, Priority enums
│       │   ├── exception/       # Global exception handler
│       │   ├── repository/      # Spring Data JPA repositories
│       │   ├── security/        # JWT filter & utilities
│       │   └── service/         # Business logic
│       └── src/main/resources/
│           └── application.properties
│
└── frontend/
        ├── index.html           # Landing page
        ├── login.html           # Login
        ├── register.html        # Registration
        ├── forgot-password.html # Password reset
        ├── api.js               # Shared API helper
        ├── admin/               # Admin portal pages
        ├── authority/           # Authority portal pages
        └── user/                # User portal pages
```

---

## 🔌 API Reference

### Base URL
```
http://localhost:8080/api
```

### Authentication
All protected endpoints require:
```
Authorization: Bearer <jwt-token>
```

---

### Auth  `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/register` | No | Register user/authority |
| POST | `/login` | No | Login → returns JWT |
| POST | `/forgot-password` | No | Send password reset token to email |
| POST | `/reset-password` | No | Reset password with token |

---

### Complaints  `/api/complaints`

| Method | Endpoint | Roles | Description |
|--------|----------|-------|-------------|
| POST | `/` | All | Submit complaint (multipart) |
| GET | `/track/{trackingId}` | Public | Track by ID |
| GET | `/my` | USER | My complaints |
| GET | `/{id}` | All | Get complaint |
| GET | `/{id}/history` | All | Update history |
| GET | `/` | ADMIN, AUTHORITY | All complaints |
| POST | `/{id}/assign` | ADMIN | Assign to authority |
| PATCH | `/{id}/status` | ADMIN, AUTHORITY | Update status/priority |
| GET | `/authority/assigned` | AUTHORITY | My assigned cases |
| POST | `/{id}/investigation-update` | AUTHORITY | Add investigation note |

**Complaint Status Flow:**
```
SUBMITTED → UNDER_REVIEW → ASSIGNED → INVESTIGATING → RESOLVED → CLOSED
                                                              ↘ REJECTED
```

---

### Evidence  `/api/evidence`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/upload/{complaintId}` | Yes | Upload file |
| GET | `/complaint/{complaintId}` | Yes | List evidence |
| GET | `/download/{evidenceId}` | Yes | Download file (authenticated) |

---

### User  `/api/user`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get profile |
| PUT | `/profile` | Update profile |
| PUT | `/change-password` | Change password |

---

### Admin  `/api/admin`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Analytics stats |
| GET | `/users` | All users (`?role=USER`) |
| GET | `/authorities` | All authorities |
| PATCH | `/users/{id}/toggle-status` | Enable/disable user |
| DELETE | `/users/{id}` | Delete user |
| POST | `/authorities` | Create authority account |
| GET | `/audit-logs` | System audit logs |

---

### Public Stats  `/api/stats`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/public` | No | Platform statistics for landing page |

---

## 🗄️ Database Schema (Key Entities)

```
users          → id, fullName, email, password, phone, role, active
complaints     → id, trackingId, title, description, status, priority, anonymous, complainantId
categories     → id, name, description, icon, active
evidences      → id, complaintId, fileName, filePath, fileType, fileSize, uploadedById
notifications  → id, userId, title, message, read, type
complaint_updates → id, complaintId, updatedById, updateType, previousStatus, newStatus
audit_logs     → id, userId, action, entityType, entityId, details
```

---

## 🔧 Environment Variables

For production, use environment variables instead of hardcoding in `application.properties`:

```properties
spring.datasource.password=${DB_PASSWORD}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m 'Add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Open a Pull Request

---

<div align="center">
Made with ❤️ for women's safety and justice.
</div>
