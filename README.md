# 🛡️ SafeReport — Grievance Management System

> A full-stack web application for submitting, tracking, and resolving public grievance complaints — built with Spring Boot, MySQL, and vanilla HTML/CSS/JS.

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![JWT](https://img.shields.io/badge/Auth-JWT-purple?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Default Credentials](#-default-credentials)
- [API Endpoints](#-api-endpoints)
- [Roles & Permissions](#-roles--permissions)
- [Pages & Modules](#-pages--modules)
- [Contributing](#-contributing)

---

## 🌐 Overview

**SafeReport** is a grievance management system designed to streamline the process of submitting, reviewing, assigning, investigating, and resolving public complaints. It supports three roles — **Complainant**, **Authority**, and **Admin** — each with their own dedicated dashboard and workflow.

Citizens can submit complaints with file evidence. Admins review, approve, and assign them to authority officers. Authority officers investigate and update case statuses. Everyone gets real-time notifications and a full audit trail.

---

## ✨ Features

### 👤 Complainant
- Register/login with email verification
- Submit complaints with title, description, category, location, incident date, and evidence files
- Track complaint status in real time using a unique Tracking ID
- Receive notifications on status changes
- View full complaint history and updates
- Anonymous complaint submission support

### 🏛️ Authority Officer
- Personal dashboard with assigned case statistics
- Investigation panel — add updates, view evidence, manage case progress
- Evidence Viewer — browse all evidence across assigned cases with complainant details
- Generate case reports
- Close and resolve cases with resolution notes

### 🔐 Admin
- Full complaint lifecycle management (review → approve → assign → resolve)
- Assign complaints to specific authority officers
- Reject complaints with reason
- User and authority management (create, activate, deactivate accounts)
- Audit logs for all system actions
- Analytics and reports dashboard
- Priority management and admin notes

### ⚙️ System
- JWT-based stateless authentication
- Role-based access control (RBAC)
- Email notifications via Gmail SMTP
- File evidence upload and authenticated download
- Complete audit trail for every action
- Support ticket system

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5 |
| **Security** | Spring Security, JWT (jjwt 0.11.5) |
| **Database** | MySQL 8.0, Spring Data JPA / Hibernate |
| **Email** | Spring Mail, Gmail SMTP |
| **File Storage** | Local filesystem (configurable path) |
| **Frontend** | HTML5, CSS3, Vanilla JavaScript |
| **Build Tool** | Apache Maven |
| **Utilities** | Lombok, Jackson |

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     FRONTEND (Static HTML)               │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   User   │  │   Authority  │  │      Admin       │  │
│  │ /user/*  │  │ /authority/* │  │    /admin/*      │  │
│  └────┬─────┘  └──────┬───────┘  └────────┬─────────┘  │
└───────┼───────────────┼───────────────────┼─────────────┘
        │               │                   │
        └───────────────┼───────────────────┘
                        │  REST API (JSON)
                        ▼
┌─────────────────────────────────────────────────────────┐
│              SPRING BOOT BACKEND (:8080)                 │
│                                                         │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  Controllers │  │   Services   │  │  Repositories │  │
│  │  /api/auth  │  │  Complaint   │  │  JPA/Hibernate│  │
│  │  /api/admin │  │  Auth        │  │               │  │
│  │  /api/evid. │  │  Notification│  │               │  │
│  │  /api/comp. │  │  Email       │  │               │  │
│  └─────────────┘  └──────────────┘  └───────┬───────┘  │
│                                             │            │
│  ┌──────────────────────────────────────────┤            │
│  │  Spring Security + JWT Filter            │            │
│  └──────────────────────────────────────────┘            │
└─────────────────────────────────┬───────────────────────┘
                                  │
          ┌───────────────────────┼───────────────┐
          ▼                       ▼               ▼
   ┌─────────────┐       ┌──────────────┐  ┌──────────┐
   │   MySQL DB   │       │  File System │  │  Gmail   │
   │ safereport_db│       │  /uploads/   │  │  SMTP    │
   └─────────────┘       └──────────────┘  └──────────┘
```

---

## 📁 Project Structure

```
safereport/
│
├── backend/safereport-new/
│   ├── pom.xml
│   └── src/main/java/com/safereport/
│       ├── config/
│       │   ├── SecurityConfig.java        # JWT + CORS + route permissions
│       │   └── DataInitializer.java       # Seeds default admin & authority
│       ├── controller/
│       │   ├── AuthController.java        # Login, register, forgot password
│       │   ├── ComplaintController.java   # CRUD, assign, status, evidence
│       │   ├── AdminController.java       # Admin-only operations
│       │   ├── EvidenceController.java    # Upload & download evidence files
│       │   ├── UserController.java        # Profile management
│       │   ├── NotificationController.java
│       │   ├── CategoryController.java
│       │   └── SupportTicketController.java
│       ├── dto/
│       │   ├── request/                   # LoginRequest, ComplaintRequest, etc.
│       │   └── response/                  # ApiResponse, ComplaintResponse, EvidenceResponse
│       ├── entity/
│       │   ├── User.java
│       │   ├── Complaint.java
│       │   ├── Evidence.java
│       │   ├── ComplaintUpdate.java
│       │   ├── Notification.java
│       │   ├── AuditLog.java
│       │   ├── Category.java
│       │   └── SupportTicket.java
│       ├── enums/
│       │   ├── Role.java                  # ADMIN, AUTHORITY, USER
│       │   ├── ComplaintStatus.java       # SUBMITTED → RESOLVED / CLOSED
│       │   └── Priority.java              # LOW, MEDIUM, HIGH, CRITICAL
│       ├── repository/                    # Spring Data JPA repositories
│       ├── security/
│       │   ├── JwtUtil.java
│       │   ├── JwtAuthenticationFilter.java
│       │   └── UserDetailsServiceImpl.java
│       └── service/impl/
│           ├── ComplaintService.java      # Core business logic
│           ├── AuthService.java
│           ├── EmailService.java
│           ├── NotificationService.java
│           └── UserService.java
│
└── frontend/
    ├── index.html                         # Landing page
    ├── login.html / register.html
    ├── api.js                             # Shared API base config
    ├── user/                              # Complainant pages
    ├── authority/                         # Authority officer pages
    └── admin/                             # Admin pages
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+
- A Gmail account with an [App Password](https://support.google.com/accounts/answer/185833) for email notifications

---

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/safereport.git
cd safereport
```

---

### 2. Create the Database

```sql
CREATE DATABASE safereport_db;
```

> The app will auto-create it if it doesn't exist, as long as your MySQL user has `CREATE` privileges.

---

### 3. Configure `application.properties`

Edit `backend/safereport-new/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/safereport_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JWT — use a strong random secret (min 32 characters)
app.jwt.secret=your-very-strong-secret-key-here

# Email (Gmail SMTP with App Password)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password

# File upload directory (relative to where the app runs)
app.upload.dir=uploads
```

> ⚠️ **Never commit real credentials to GitHub.** Add `application.properties` to `.gitignore` and use environment variables in production.

---

### 4. Run the Backend

```bash
cd backend/safereport-new
mvn spring-boot:run
```

The API starts at **`http://localhost:8080`**

On first run, default admin and authority accounts are automatically created (see [Default Credentials](#-default-credentials)).

---

### 5. Open the Frontend

The frontend is plain static HTML — no build step needed.

```bash
# Option A: VS Code Live Server (recommended)
# Right-click frontend/index.html → "Open with Live Server"

# Option B: Python
cd frontend
python -m http.server 5500

# Option C: Node.js
npx serve frontend
```

Open **`http://localhost:5500`** in your browser.

---

## 🔑 Default Credentials

Automatically seeded on first startup.

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@safereport.com` | `Admin@123` |
| **Authority** | `authority@safereport.com` | `Auth@123` |
| **User** | Register via `/register.html` | — |

> 🔒 Change these credentials immediately in a production deployment.

---

## 📡 API Endpoints

### Authentication — `/api/auth`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT token | Public |
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/forgot-password` | Send reset email | Public |
| POST | `/api/auth/reset-password` | Reset with token | Public |

### Complaints — `/api/complaints`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/complaints` | Submit a complaint | Public/User |
| GET | `/api/complaints` | List all complaints | Admin |
| GET | `/api/complaints/{id}` | Get complaint details | Auth |
| GET | `/api/complaints/track/{trackingId}` | Public complaint tracking | Public |
| PATCH | `/api/complaints/{id}/status` | Update status/priority | Admin/Authority |
| POST | `/api/complaints/{id}/assign` | Assign to authority | Admin |
| GET | `/api/complaints/authority/assigned` | My assigned cases | Authority |
| GET | `/api/complaints/{id}/history` | Complaint update history | Auth |

### Evidence — `/api/evidence`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/evidence/upload/{complaintId}` | Upload evidence file | Auth |
| GET | `/api/evidence/complaint/{complaintId}` | List evidence files | Auth |
| GET | `/api/evidence/download/{evidenceId}` | Download evidence file | Auth |

### Admin — `/api/admin`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/admin/users` | List all users | Admin |
| GET | `/api/admin/authorities` | List authority officers | Admin |
| POST | `/api/admin/authorities` | Create authority account | Admin |
| PATCH | `/api/admin/users/{id}/toggle` | Activate/deactivate user | Admin |
| GET | `/api/admin/audit-logs` | System audit logs | Admin |

### Notifications — `/api/notifications`
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/notifications` | Get my notifications | Auth |
| PATCH | `/api/notifications/{id}/read` | Mark as read | Auth |
| PATCH | `/api/notifications/read-all` | Mark all as read | Auth |

---

## 👥 Roles & Permissions

```
PUBLIC
  ├── Submit complaint (anonymous or named)
  ├── Track complaint by tracking ID
  └── Register / Login

USER (Complainant)
  ├── View and manage own complaints
  ├── Upload evidence to own complaints
  ├── Receive notifications
  └── Update profile

AUTHORITY
  ├── View complaints assigned to them
  ├── Add investigation updates
  ├── View and download evidence
  ├── Close / resolve cases
  └── Generate case reports

ADMIN  (full access)
  ├── View ALL complaints
  ├── Approve, reject, assign complaints
  ├── Manage users and authority accounts
  ├── View audit logs
  └── Access analytics and system settings
```

---

## 📄 Pages & Modules

### User Portal (`/user/`)
| Page | Purpose |
|---|---|
| `user-dashboard.html` | Complaint overview and stats |
| `submit-complaint.html` | Multi-step submission with evidence upload |
| `my-complaint.html` | List all submitted complaints |
| `complaint-status.html` | Detailed status and history timeline |
| `notifications.html` | All system notifications |
| `user-profile.html` | Edit profile, change password |

### Authority Portal (`/authority/`)
| Page | Purpose |
|---|---|
| `authority-dashboard.html` | Case statistics and activity feed |
| `my-cases.html` | All assigned cases |
| `investigation.html` | Full investigation panel per case |
| `evidence.html` | Evidence viewer with complainant details |
| `add-update.html` | Add progress updates to a case |
| `close-cases.html` | Resolve/close a case |
| `closed-cases.html` | Archive of completed cases |
| `generate-report.html` | Case summary report generator |

### Admin Portal (`/admin/`)
| Page | Purpose |
|---|---|
| `admin-dashboard.html` | System-wide statistics |
| `all-complaints.html` | Full complaint list with filters |
| `pending-review.html` | Complaints awaiting review |
| `assigned-cases.html` | Complaints assigned to authorities |
| `review-complaint.html` | Approve / assign / reject workflow |
| `user-management.html` | Manage user accounts |
| `authority-management.html` | Manage authority accounts |
| `audit-logs.html` | Full system audit trail |
| `reports.html` | Analytics dashboard |

---

## 🔄 Complaint Lifecycle

```
  SUBMITTED
      │
      ▼
  UNDER_REVIEW ──────────────────► REJECTED
      │
      ▼
  ASSIGNED  (to Authority Officer)
      │
      ▼
  INVESTIGATING
      │
      ▼
  RESOLVED
      │
      ▼
  CLOSED
```

---

## 🔒 Security Notes

- All non-public endpoints require a valid `Authorization: Bearer <token>` header
- Passwords are hashed with **BCrypt**
- Server-side file paths are never exposed in API responses
- Evidence downloads require authentication
- Role enforcement via `@PreAuthorize` annotations at the method level

---

## ⚙️ Configuration Reference

| Property | Default | Description |
|---|---|---|
| `server.port` | `8080` | Backend server port |
| `app.jwt.secret` | — | JWT signing secret (min 32 chars) |
| `app.jwt.expiration` | `86400000` | Token expiry in ms (24h) |
| `app.upload.dir` | `uploads` | Evidence file storage directory |
| `spring.servlet.multipart.max-file-size` | `10MB` | Max size per uploaded file |
| `spring.servlet.multipart.max-request-size` | `50MB` | Max total upload size |
| `app.mail.enabled` | `true` | Toggle email notifications |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add: description of your change"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📝 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
Built with ❤️ using Spring Boot &amp; Vanilla JavaScript
</div>
