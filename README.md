# Employee Payroll Management System (SRMCEM)

A comprehensive, full-stack Employee Payroll Management System built for SRMCEM. This project features a robust Spring Boot RESTful API backend paired with a modern React + Vite + Tailwind CSS frontend interface.

---

## 📋 Table of Contents
1. [Project Overview & Verification](#project-overview--verification)
2. [Installation Guide](#installation-guide)
3. [Project Structure](#project-structure)
4. [API Documentation Summary](#api-documentation-summary)
5. [Database Schema Summary](#database-schema-summary)
6. [Suggested Improvements (College Project Scope)](#suggested-improvements-college-project-scope)

---

## ✅ Project Overview & Verification

The Employee Payroll Management System has been thoroughly built, configured, and verified.

- **Backend Build & Compilation**: Verified with Maven Wrapper (`./mvnw.cmd test`). All **170 unit & integration tests** pass cleanly with 0 errors or failures.
- **Database Configuration**: Configured with Spring Data JPA & H2 In-Memory Database (with MySQL compatibility flags enabled for easy production migration). H2 Console available at `http://localhost:8080/h2-console`.
- **Security & Authentication**: Spring Security enabled with Session-based authentication and BCrypt password hashing. Protected REST endpoints require an active authenticated session.
- **CRUD Operations**: Complete CRUD workflows implemented and verified for Admin, Employees, Departments, Designations, Attendance, Leaves, Payroll, Documents, and Company Settings.
- **Reports Generation**: Dynamic report engine supports PDF, Excel (.xlsx), and CSV formats across 5 major modules (Employees, Attendance, Leaves, Payroll, Departments).
- **Email System**: Automated email notifications triggered via JavaMailSender upon Leave Approval/Rejection and Monthly Payroll Generation.
- **Swagger OpenAPI Documentation**: Fully documented interactive Swagger UI accessible at `http://localhost:8080/swagger-ui/index.html`.
- **Frontend-Backend Integration**: React + Vite + Tailwind CSS Single Page Application (SPA) consuming backend REST APIs via pre-configured Axios client with CORS and credentials support.

---

## 🚀 Installation Guide

### Prerequisites
- **Java Development Kit (JDK)**: Java 17 or higher installed.
- **Node.js & npm**: Node.js 18+ and npm installed.
- **Maven**: (Optional) Standard Maven 3.8+ or use the included `./mvnw.cmd` wrapper.

### Step 1: Clone & Navigate to Project
```bash
cd c:\Users\DELL\Desktop\payroll
```

### Step 2: Backend Setup & Execution
1. Open a terminal in the root directory.
2. Build and run the Spring Boot application:
   ```bash
   ./mvnw.cmd spring-boot:run
   ```
3. The backend server will start on port `8080` (`http://localhost:8080`).
4. **H2 Database Console**: `http://localhost:8080/h2-console`
   - **JDBC URL**: `jdbc:h2:mem:payroll_db`
   - **Username**: `sa`
   - **Password**: `password`
5. **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

### Step 3: Frontend Setup & Execution
1. Open a second terminal and navigate to the `frontend` folder:
   ```bash
   cd frontend
   ```
2. Install frontend dependencies (if not already installed):
   ```bash
   npm install
   ```
3. Launch the Vite development server:
   ```bash
   npm run dev
   ```
4. Access the frontend application in your browser at `http://localhost:5173`.

### Default Admin Credentials
- **Username**: `admin`
- **Password**: `admin123`

---

## 📂 Project Structure

```
payroll/
├── src/
│   ├── main/
│   │   ├── java/com/srmcem/payroll/
│   │   │   ├── config/             # Security, OpenAPI Swagger, Web CORS configurations
│   │   │   ├── controller/         # REST Controllers (Admin, Employee, Dept, Leave, Payroll, etc.)
│   │   │   ├── dto/                # Data Transfer Objects (Requests & Responses)
│   │   │   ├── entity/             # JPA Entities (Employee, Department, Leave, AuditLog, etc.)
│   │   │   ├── enums/              # Enum types (EmployeeStatus, LeaveStatus, DocumentType, etc.)
│   │   │   ├── exception/          # Global Exception Handler & Custom Exception Classes
│   │   │   ├── repository/         # Spring Data JPA Repositories
│   │   │   ├── response/           # Standardized ApiResponse wrapper format
│   │   │   ├── service/            # Service Interfaces & Implementations
│   │   │   └── util/               # Utility classes (PDF/Excel exporters, Date formatting)
│   │   └── resources/
│   │       ├── application.properties # Spring configuration (DB, Mail, File upload)
│   │       ├── schema.sql          # Optional DB schema DDL
│   │       └── data.sql            # Initial seed data for testing
│   └── test/                       # 170+ JUnit 5 & Mockito test suites
├── frontend/
│   ├── src/
│   │   ├── components/         # Global Layout, Sidebar, Navigation
│   │   ├── context/            # AuthContext session state manager
│   │   ├── pages/              # 11 Modular UI Page Components
│   │   │   ├── Login.jsx
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Employees.jsx
│   │   │   ├── Departments.jsx
│   │   │   ├── Designations.jsx
│   │   │   ├── Attendance.jsx
│   │   │   ├── Leaves.jsx
│   │   │   ├── Payroll.jsx
│   │   │   ├── Documents.jsx
│   │   │   ├── Reports.jsx
│   │   │   ├── Settings.jsx
│   │   │   ├── Profile.jsx
│   │   │   └── AuditLogs.jsx
│   │   ├── services/           # Axios instance configuration (api.js)
│   │   ├── App.jsx             # React Router DOM mapping
│   │   └── index.css           # Tailwind CSS directives
│   ├── package.json
│   ├── vite.config.js
│   └── postcss.config.js
├── mvnw.cmd
└── pom.xml
```

---

## 📡 API Documentation Summary

The backend exposes a full suite of RESTful endpoints categorized by functional modules.

| Module | Base Path | Key Endpoints | Description |
|--------|-----------|---------------|-------------|
| **Auth** | `/api/auth` | `POST /login`, `POST /logout`, `POST /change-password` | Session authentication & credential management |
| **Dashboard** | `/api/dashboard` | `GET /summary` | Aggregated system metrics & counts |
| **Employee** | `/api/employees` | `GET /`, `POST /`, `PUT /{id}`, `DELETE /{id}`, `GET /paginated`, `GET /search` | Complete employee profile management with pagination |
| **Department** | `/api/departments` | `GET /`, `POST /`, `PUT /{id}`, `DELETE /{id}` | Department management |
| **Designation** | `/api/designations` | `GET /`, `POST /`, `PUT /{id}`, `DELETE /{id}` | Official employee job titles |
| **Attendance** | `/api/attendance` | `GET /date`, `POST /` | Daily attendance log tracking |
| **Leave** | `/api/leaves` | `GET /`, `GET /pending`, `POST /`, `PATCH /{id}/status` | Leave application & approval workflows |
| **Payroll** | `/api/payroll` | `GET /paginated`, `POST /generate`, `GET /{id}/payslip` | Monthly payroll calculation & PDF payslip generation |
| **Document** | `/api/documents` | `POST /upload`, `GET /employee/{id}`, `GET /download/{id}`, `DELETE /{id}` | Multipart file storage (Aadhaar, PAN, Resume) |
| **Reports** | `/api/reports` | `GET /{module}/{format}` | Download PDF, Excel, CSV reports for all modules |
| **Company Settings**| `/api/settings` | `GET /`, `POST /`, `PUT /` | Singleton company settings configuration |
| **Audit Logs** | `/api/audit-logs` | `GET /` | Activity tracker with search, module & date filters |

*Detailed request/response schemas and sample payloads can be explored interactively via Swagger UI at `/swagger-ui/index.html`.*

---

## 🗄️ Database Schema Summary

The application uses an RDBMS relational schema mapped with Spring Data JPA.

### Key Entities & Relations
1. **`Admin`**: Stores administrator credentials (`id`, `username`, `password`, `email`, `role`).
2. **`Department`**: Organizational units (`departmentId`, `departmentName`, `description`).
3. **`Designation`**: Job titles (`designationId`, `designationName`).
4. **`Employee`**: Core entity (`employeeId`, `firstName`, `lastName`, `email`, `phone`, `gender`, `dateOfBirth`, `joiningDate`, `salary`, `status`).
   - Many-to-One with `Department`
   - Many-to-One with `Designation`
5. **`Attendance`**: Attendance logs (`attendanceId`, `employee`, `date`, `checkIn`, `checkOut`, `attendanceStatus`).
6. **`LeaveRequest`**: Leave applications (`leaveId`, `employee`, `leaveType`, `startDate`, `endDate`, `totalDays`, `reason`, `status`, `adminRemarks`).
7. **`Payroll`**: Calculated monthly salaries (`payrollId`, `employee`, `payrollMonth`, `basicSalary`, `allowances`, `deductions`, `grossSalary`, `netSalary`, `paymentDate`).
8. **`Document`**: Uploaded employee IDs (`documentId`, `employee`, `documentType`, `fileName`, `filePath`, `uploadDate`).
9. **`CompanySettings`**: Singleton organization details (`id`, `companyName`, `address`, `email`, `phone`, `website`, `logoPath`, `financialYear`).
10. **`AuditLog`**: System audit trail (`id`, `username`, `action`, `module`, `timestamp`).

---

## 💡 Suggested Improvements (College Project Scope)

These enhancements are specifically tailored to add academic value and demonstration appeal for a college final project presentation:

1. **Employee Self-Service Portal**:
   - Add a separate login role (`ROLE_EMPLOYEE`) allowing non-admin employees to view their own payslips, mark their own daily check-in/check-out, and submit leave requests directly.
2. **Visual Analytics & Dashboard Charts**:
   - Integrate Chart.js or Recharts in the React frontend to display visual bar charts for monthly payroll expenses, pie charts for department employee distribution, and leave trend graphs.
3. **Tax & PF Deduction Calculator**:
   - Enhance the Payroll Service logic to automatically calculate Professional Tax (PT), Provident Fund (PF), and Income Tax (TDS) based on Indian Tax Slabs rather than flat deduction estimates.
4. **Export to Excel Charts / Dynamic Filtering**:
   - Add date-range parameter inputs in the Reports page frontend to allow users to generate custom date-filtered attendance/leave reports before downloading.
5. **Bulk Attendance Upload**:
   - Provide a CSV/Excel upload feature for admins to import daily attendance logs in bulk for all employees at once.

---
*Created for College Final Project Submission - SRMCEM Payroll System.*