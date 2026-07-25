# 🚀 Project Startup Guide: Employee Payroll Management System (SRMCEM)

This guide provides step-by-step instructions to set up, start, and verify both the backend and frontend components of the Employee Payroll Management System.

---

## 📋 Prerequisites

Before starting, ensure you have the following software installed on your system:
1. **Java Development Kit (JDK)**: Version 17 or higher.
2. **Node.js**: Version 18 or higher (along with `npm`).
3. **Web Browser**: Chrome, Edge, or Firefox.

---

## ⚙️ Step 1: Starting the Backend Server

The backend is built with Spring Boot and uses an in-memory H2 database by default.

1. Open a new terminal / command prompt.
2. Navigate to the project root directory:
   ```bash
   cd c:\Users\DELL\Desktop\payroll
   ```
3. Run the backend using the Maven Wrapper command:
   - **On Windows (PowerShell or Command Prompt)**:
     ```cmd
     .\mvnw.cmd spring-boot:run
     ```
   - **On macOS / Linux**:
     ```bash
     ./mvnw spring-boot:run
     ```
4. Wait until you see a console log indicating that the application has started:
   ```text
   Started PayrollApplication in X.XXX seconds (JVM running for X.XX)
   ```
5. The backend server runs at **`http://localhost:8080`**.

---

## 🎨 Step 2: Starting the Frontend Application

The frontend is built with React + Vite + Tailwind CSS.

1. Open a **second terminal / command prompt** (do not close the backend terminal).
2. Navigate to the `frontend` directory:
   ```bash
   cd c:\Users\DELL\Desktop\payroll\frontend
   ```
3. Install the required dependencies (only required the first time):
   ```bash
   npm install
   ```
4. Run the Vite development server:
   ```bash
   npm run dev
   ```
5. The frontend server runs at **`http://localhost:5173`**.

---

## 🔑 Login Credentials

Once the frontend is running:
1. Open your browser and navigate to: **`http://localhost:5173`**
2. Log in using the default Administrator credentials:
   * **Username**: `admin`
   * **Password**: `admin123`

---

## 🔍 Verification & Developer Tools

Here are useful endpoints to verify that the services are running correctly:

* **Frontend App**: [http://localhost:5173](http://localhost:5173) (Interactive UI)
* **Backend Swagger/OpenAPI Documentation**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) (Test API endpoints directly)
* **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * **JDBC URL**: `jdbc:h2:mem:payroll_db`
  * **User Name**: `sa`
  * **Password**: `password`

---

## 🛠️ Troubleshooting

### 1. Port 8080 or 5173 is already in use
If you get an address/port binding error:
- Find and stop any other applications running on ports `8080` or `5173`.
- Alternatively, you can configure a different port in [application.properties](file:///c:/Users/DELL/Desktop/payroll/src/main/resources/application.properties) using `server.port=XXXX`.

### 2. Node Modules / dependency conflicts
If `npm install` fails:
- Run `npm cache clean --force` and try again.
- Or use `npm install --legacy-peer-deps`.
