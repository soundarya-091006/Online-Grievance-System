# 🚀 SafeReport - Quick Start Guide

Welcome to the SafeReport Online Grievance System! This guide will help you get started in just a few minutes.

---

## 📲 Getting Started

### Step 1: Initialize Test Data (One-Time Setup)
1. Open your browser and go to: `http://localhost:8000/frontend/test-data.html`
2. Click the blue **"✅ Initialize All Sample Data"** button
3. You'll see a success message confirming the setup

> **Note:** the registration page only supports creating **user** accounts. Admin and authority login credentials are fixed for this demo (shown later in this guide and in test-data.html) and should be seeded in the backend database during implementation.

This creates sample users, authorities, and complaints for testing.

---

## 👤 **USER WORKFLOW** - File a Complaint & Track It

### Login as User
1. Go to: `http://localhost:8000/frontend/login.html`
2. Select **"User"** tab (default)
3. Enter credentials:
   - **Email:** `amit@test.com`
   - **Password:** `User@123`
4. Click **LOGIN**

### Submit a Complaint
1. From User Dashboard, click **"Submit Complaint"**
2. Fill in the form:
   - **Category:** Select from dropdown (e.g., Workplace Harassment)
   - **Subject:** Brief title
   - **Description:** Detailed explanation
   - **Location:** Where incident occurred
   - **Priority:** Select severity level
3. Click **SUBMIT COMPLAINT**
4. You'll see a success message and your complaint ID

### Track Your Complaint
1. From User Dashboard, click **"My Complaints"**
2. You'll see all your submitted complaints with status badges
3. Click **"Track"** on any complaint to see:
   - Full details
   - Current status
   - Timeline of all updates
   - When it will be reviewed/resolved

### Status Updates
As admin and authorities work on your complaint, the status will update automatically:
- 🆕 **NEW** → Just submitted
- 📋 **UNDER REVIEW** → Admin reviewing
- ✅ **ASSIGNED** → Assigned to an authority
- 🔍 **IN PROGRESS** → Authority investigating
- ✔️ **RESOLVED** → Case closed

---

## 👨‍💼 **ADMIN WORKFLOW** - Review & Assign Cases

### Login as Admin
1. Go to: `http://localhost:8000/frontend/login.html`
2. Select **"Admin"** tab
3. Enter credentials:
   - **Email:** `admin@test.com`
   - **Password:** `Admin@123`
4. Click **LOGIN**

### Review New Complaints
1. From Admin Dashboard, click **"Pending Review"**
2. You'll see all NEW complaints from users
3. Click any complaint to see full details
4. Click **APPROVE** to move to next stage
5. Complaint status changes: NEW → UNDER REVIEW

### Assign Cases to Authorities
1. From Admin Dashboard, click **"Assigned Cases"**
2. Scroll down to see **"Unassigned Queue"**
3. You'll see all UNDER REVIEW complaints
4. For each complaint:
   - Select an authority from the dropdown menu
   - Click **ASSIGN**
5. Complaint is now assigned (status: ASSIGNED)

### Monitor Workload
1. In **"Assigned Cases"** page, see authority cards showing:
   - **Assigned:** How many active cases
   - **Resolved:** How many completed cases
   - **Workload:** High/Medium/Low indicator
2. Use **"View Cases"** to see each authority's assignments

---

## 👮 **AUTHORITY WORKFLOW** - Investigate & Resolve

### Login as Authority
1. Go to: `http://localhost:8000/frontend/login.html`
2. Select **"Authority"** tab
3. Enter credentials:
   - **Email:** `rajan@authority.com`
   - **Password:** `Auth@123`
4. Click **LOGIN**

### Accept New Assignments
1. From Authority Dashboard, click **"New Assignments"**
2. You'll see cases assigned to you (status: ASSIGNED)
3. Each case shows:
   - Complaint ID and summary
   - Priority and category
   - Description
4. Click **"Accept"** to accept the case
   - Status changes to: IN PROGRESS
   - Case now appears in "Active Investigations"

### Investigate Cases
1. From Authority Dashboard, click **"Active Investigations"**
2. You'll see all your IN PROGRESS investigations
3. For each case, you can:
   - Click **"Investigate"** → Add detailed investigation notes
   - Click **"Quick Update"** → Add status updates
   - Click **"Generate Report"** → Create investigation report
4. Update the progress percentage as you investigate

### Close/Resolve Cases
1. From Authority Dashboard, click **"Closed Cases"**
2. Click **"Close Case"** on an investigation you've completed
3. Add final notes and:
   - Set progress to 100%
   - Confirm case resolution
4. Status changes to: RESOLVED
5. User will see the case marked as complete

### View Your Cases
- **"My Cases"** → See all your cases (active, closed, resolved)
- **"Active Investigations"** → See only IN PROGRESS cases
- **"New Assignments"** → See ASSIGNED cases waiting for acceptance
- **"Closed Cases"** → See RESOLVED cases

---

## 📊 Additional Features

### Check System Health
1. Go to: `http://localhost:8000/frontend/system-validation.html`
2. Click **"🚀 Run Full Validation"**
3. See a detailed system health report
4. Aim for 100% success rate!

### View All Data
1. Go to: `http://localhost:8000/frontend/test-data.html`
2. Click **"📊 Show Data Summary"**
3. See complete inventory of:
   - All users and their roles
   - All authorities
   - All complaints and their statuses

### Clear Data (Start Fresh)
1. Go to: `http://localhost:8000/frontend/test-data.html`
2. Click **"🗑️ Clear All Data"**
3. Confirm the action
4. All test data will be cleared
5. Run "Initialize All Sample Data" again to reset

---

## 🔑 Complete Test Credentials

| Role | Email | Password |
|------|-------|----------|
| **User 1** | amit@test.com | User@123 |
| **User 2** | priya@test.com | User@123 |
| **Admin** | admin@test.com | Admin@123 |
| **Authority 1** | rajan@authority.com | Auth@123 |
| **Authority 2** | mukesh@authority.com | Auth@123 |

---

## 🧪 Complete Workflow Test (5 Minutes)

### Scenario: User submits complaint → Admin approves → Authority investigates

**Time: ~5 minutes total**

1. **Login as User (2 min)**
   - Open login page
   - Select User, login as amit@test.com / User@123
   - Click "Submit Complaint"
   - Fill form and submit
   - Save the complaint ID

2. **Approve as Admin (2 min)**
   - Logout (click your profile)
   - Login again as admin@test.com / Admin@123
   - Go to "Pending Review"
   - Find your complaint and click "Approve"
   - Go to "Assigned Cases" → "Unassigned Queue"
   - Select authority "Rajan Arora" and click "Assign"

3. **Investigate as Authority (1 min)**
   - Logout and login as rajan@authority.com / Auth@123
   - Go to "New Assignments"
   - Click "Accept" on your complaint
   - Go to "Active Investigations"
   - See your case in progress

4. **Track as User (Optional)**
   - Logout and login back as user
   - Go to "Complaint Status"
   - See real-time status updates

---

## 🆘 Troubleshooting

### "Login not working"
- ✓ Check that test data is initialized (visit test-data.html)
- ✓ Make sure you selected the correct role (User/Admin/Authority)
- ✓ Verify spelling of email address exactly

### "Can't see assignments"
- ✓ Make sure status is correct:
  - Users: See NEW complaints assigned to them
  - Admins: See UNDER REVIEW in "Assigned Cases"
  - Authority: See ASSIGNED in "New Assignments"
- ✓ Clear browser cache and refresh page

### "Data not saving"
- ✓ Enable localStorage in browser settings
- ✓ Check browser isn't in private/incognito mode (disables localStorage)
- ✓ Open browser DevTools → Application → LocalStorage → check values

### "Badge counts not updating"
- ✓ Refresh the page to see latest counts
- ✓ Check page shows correct role (User/Admin/Authority)
- ✓ Visit test-data.html and run "Show Data Summary" to verify data

---

## 📚 Learn More

- **Full Workflow Guide:** Read `WORKFLOW_GUIDE.md` for complete documentation
- **Implementation Details:** See `IMPLEMENTATION_SUMMARY.md` for technical overview
- **System Architecture:** Check database structure in WORKFLOW_GUIDE.md

---

## ✅ You're All Set!

You're ready to test the complete SafeReport system. Start with the **"Complete Workflow Test (5 Minutes)"** section above to see everything in action.

**Questions?** Refer to troubleshooting section or check the full documentation.

**Happy Testing!** 🎉

---

*Last Updated: January 2025*
