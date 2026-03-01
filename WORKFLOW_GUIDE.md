# SafeReport - Online Grievance System - Complete Workflow Guide

## 📋 System Overview

SafeReport is a three-tier role-based grievance management system with the following roles:
- **User**: Submit and track complaints
- **Admin**: Review, approve, and assign complaints to authorities
- **Authority**: Investigate and resolve assigned complaints

## 🔄 Complete Workflow

### 1. **User Submits Complaint**
   - User registers or logs in
   - Navigates to "Submit Complaint" 
   - Fills complaint details (category, subject, description, location, priority)
   - Complaint is created with status **`NEW`**
   - User can track complaint in "My Complaints" or redirect to "Complaint Status"

### 2. **Admin Reviews Complaint**
   - Admin logs in to "Admin Dashboard"
   - Views complaints in "Pending Review" section
   - Reviews complaint details
   - **Approves** complaint → status changes to **`UNDER REVIEW`**
   - Complaint moves to "Assigned Cases" queue for assignment

### 3. **Admin Assigns to Authority**
   - Admin navigates to "Assigned Cases"
   - Views unassigned complaints (status = `UNDER REVIEW`)
   - Selects an authority from dropdown
   - Clicks "Assign" → status changes to **`ASSIGNED`**
   - Authority email is stored in `complaint.assignedTo`

### 4. **Authority Accepts Assignment**
   - Authority logs in to "Authority Dashboard"
   - Navigates to "New Assignments"
   - Reviews assignment details
   - Clicks "Accept Case" → status changes to **`IN PROGRESS`**
   - Case now appears in "Active Investigations"

### 5. **Authority Investigates**
   - Authority views case in "Active Investigations"
   - Updates investigation progress
   - Uploads evidence and timeline updates
   - Can close case when resolved → status = **`RESOLVED`**

### 6. **User Tracks Resolution**
   - User logs in and views complaint in "My Complaints" or "Complaint Status Page"
   - Sees real-time status updates as authority progresses
   - Receives confirmation when case is resolved

---

## 🧪 Testing the Complete Workflow

### **Step 0: Initialize Test Data**
1. Open `frontend/test-data.html` in your browser
2. Click **"✅ Initialize All Sample Data"**
3. This creates:
   - 2 test users (Amit Kumar, Priya Singh)
   - 2 test authorities (Rajan Arora, Mukesh Rao)
   - 4 sample complaints with different statuses

### **Step 1: Test as User**
**Login Credentials:**
- Email: `amit@test.com`
- Password: `User@123`

**Actions:**
1. Login → Dashboard
2. Click "Submit Complaint"
3. Fill form and submit (creates NEW complaint)
4. Go to "My Complaints" to see your submissions
5. Click "Track" on a complaint to view details
6. Return later to see status updates from admin/authority

### **Step 2: Test as Admin**
**Login Credentials:**
- Email: `admin@test.com`
- Password: `Admin@123`

**Actions:**
1. Login → Admin Dashboard
2. Click "Pending Review" → See NEW complaints from users
3. Click complaint details to review
4. Click "Approve" → complaint moves to `UNDER REVIEW` status
5. Navigate to "Assigned Cases"
6. In the "Unassigned Queue" section:
   - Select authority from dropdown
   - Click "Assign" → sends to that authority with status `ASSIGNED`
7. View workload in "Assigned Cases" grid showing counts

### **Step 3: Test as Authority**
**Login Credentials:**
- Email: `rajan@authority.com`
- Password: `Auth@123`

**Actions:**
1. Login → Authority Dashboard
2. Check badge counts update dynamically
3. Navigate to "New Assignments"
   - See all cases with status `ASSIGNED`
   - Click "Accept" on a case → status becomes `IN PROGRESS`
4. Navigate to "Active Investigations"
   - See all accepted cases (status = `IN PROGRESS`)
   - View progress percentage and metadata
5. Click "Investigate" or "Quick Update" to add case updates
6. When done, "Close Case" to mark as `RESOLVED`

### **Step 4: Verify User Sees Updates**
1. Login as user (amit@test.com)
2. View "Complaint Status" page
3. Confirm real-time status reflects authority's progress
4. See timeline of all status changes

---

## 📊 Data Structure

### **Users Table** (localStorage: `users`)
```javascript
{
  fullname: "Amit Kumar",
  email: "amit@test.com",
  phone: "9876543210",
  password: "User@123",          // Demo password (hash in production)
  role: "user|admin|authority"
}
```

### **Authorities Table** (localStorage: `authorities`)
```javascript
{
  id: "1",
  name: "Rajan Arora",
  email: "rajan@authority.com",  // Unique identifier for assignment
  department: "Labour Department",
  phone: "9876543212",
  address: "Labour Office, Chennai",
  createdAt: ISO_DATE_STRING
}
```

### **Complaints Table** (localStorage: `complaints`)
```javascript
{
  id: "C001",
  userEmail: "amit@test.com",     // User who submitted
  category: "Workplace Harassment",
  subject: "Repeated verbal abuse",
  description: "...",
  location: "Office Building A",
  priority: "High|Medium|Low|Critical",
  status: "NEW|UNDER REVIEW|ASSIGNED|IN PROGRESS|RESOLVED|CLOSED",
  assignedTo: "rajan@authority.com", // Authority's email (null until assigned)
  createdAt: ISO_DATE_STRING,
  updatedAt: ISO_DATE_STRING,
  progress: 0-100,                // Progress percentage
  timeline: [                     // Status change history
    { status: "In Progress", date: ISO_DATE, description: "..." }
  ]
}
```

---

## 🔑 Key Features Implemented

### ✅ **Authentication & Authorization**
- Role-based login (User/Admin/Authority)
- Registration with role selection
- Session management via localStorage
- Automatic redirect based on role

### ✅ **User Portal**
- Submit and manage complaints
- Real-time status tracking
- Download complaint details
- View personal complaint history

### ✅ **Admin Portal**
- View all complaints by status
- Approve complaints (NEW → UNDER REVIEW)
- Assign to authorities with dropdown selection
- Monitor workload distribution
- View authority performance metrics

### ✅ **Authority Portal**
- Accept/decline assignments
- Track active investigations
- Update case progress
- Generate investigation reports
- Close resolved cases

### ✅ **Navigation & UI**
- Responsive hamburger menu
- Dynamic badge counts  
- Status-based color coding
- Toast notifications
- Consistent styling across roles

---

## 🚀 Deployment Notes

### **For Development/Testing:**
1. Use `test-data.html` to initialize sample data
2. Open pages in browser - no backend needed
3. Data persists in browser's localStorage

### **For Production:**
1. **Backend Required:**
   - Replace localStorage with real database
   - Implement API endpoints for CRUD
   - Add proper authentication & JWT

2. **Security Considerations:**
   - Hash passwords (use bcrypt)
   - Implement role-based access control (RBAC)
   - Add audit logging
   - Use HTTPS only
   - Implement rate limiting

3. **Database Schema:**
   - Migrate localStorage structure to PostgreSQL/MongoDB
   - Add indexes on email, status, assignedTo
   - Implement timestamps (createdAt, updatedAt)
   - Add soft deletes for complaints

---

## 📁 File Structure

```
frontend/
├── index.html                           # Landing page
├── login.html                           # Login (all roles)
├── register.html                        # Registration (all roles)
├── test-data.html                       # 🆕 Test data initialization
│
├── user/
│   ├── user-dashboard.html             # User home
│   ├── submit-complaint.html           # Submit new complaint
│   ├── my-complaints.html              # View user's complaints
│   ├── complaint-status.html           # Track specific complaint
│   └── user-profile.html               # User settings
│
├── admin/
│   ├── admin-dashboard.html            # Admin home
│   ├── pending-review.html             # NEW → UNDER REVIEW
│   ├── assigned-cases.html             # Assign UNDER REVIEW → ASSIGNED
│   ├── all-complaints.html             # View all complaints
│   ├── authority-management.html       # Manage authorities
│   ├── add-authority.html              # Add new authority
│   └── user-management.html            # Manage users
│
├── authority/
│   ├── authority-dashboard.html        # Authority home
│   ├── new-assignments.html            # ASSIGNED cases → Accept/Decline
│   ├── active-cases.html               # IN PROGRESS cases
│   ├── my-cases.html                   # All authority cases
│   ├── add-update.html                 # Add case update
│   ├── investigate.html                # Investigation details
│   ├── close-cases.html                # Mark as RESOLVED
│   ├── closed-cases.html               # View closed cases
│   ├── generate-report.html            # Generate reports
│   ├── evidence.html                   # Evidence manager
│   ├── schedule.html                   # Schedule hearings
│   ├── profile.html                    # Authority profile
│   ├── authority-shared.css            # Shared styles
│   └── authority-shared.js             # 🆕 Shared JS (auth, nav, utils)
│
└── [other files: about, contact, privacy, terms, analytics, etc.]
```

---

## 🐛 Common Issues & Fixes

### **Issue: Badge counts not updating**
- **Fix:** Ensure `DOMContentLoaded` event calls `updateNavCounts()` and `updateDashboardCounts()`
- **Check:** Browser console for JS errors

### **Issue: Authority can't see assigned complaints**
- **Fix:** Verify `complaint.assignedTo` matches `currentUser.email` exactly (case-sensitive)
- **Check:** Use test-data.html to verify authority email

### **Issue: Status not changing after action**
- **Fix:** Ensure complaint object is saved back to localStorage with `JSON.stringify()`
- **Check:** View data in test-data.html "Show Data Summary"

### **Issue: Page redirect loop**
- **Fix:** Check that `currentUser.role` is set correctly during login
- **Check:** Verify login page stores role as lowercase

---

## 📝 Future Enhancements

- [ ] Real-time notifications via WebSockets
- [ ] Email notifications
- [ ] Advanced search and filtering
- [ ] Bulk complaint actions
- [ ] Analytics dashboard
- [ ] Document uploads and management
- [ ] Case scheduling and calendar integration
- [ ] Multi-language support
- [ ] Mobile app version
- [ ] Integration with external APIs

---

## 👥 Support

For issues or questions, refer to the system documentation or contact the development team.

**Last Updated:** January 2025
