# SocietEase — Multi-Tenant Society Management Platform

SocietEase is a modern, secure, multi-tenant SaaS platform for residential society management. It allows Responsible Persons (RPs) to register their societies, automatically generates apartment structures, and provides a comprehensive dashboard for managing residents, helpers, committees, complaints, notices, and maintenance collections.

## ✨ Features

- **Multi-Tenant SaaS Architecture:** Any number of societies can register and manage their operations independently on a single deployment.
- **Automated Apartment Generation:** Set up towers, floors, and units per floor during registration; the system auto-generates apartment labels (e.g., A-101, B-304).
- **Role-Based Access Control:** Distinct roles and dashboards for SuperAdmin (if applicable), Responsible Persons (RPs), and Residents.
- **Robust Security & Privacy:**
  - **AES-256-GCM Encryption:** Resident Personally Identifiable Information (PII) like names, phone numbers, emails, and Aadhar numbers are encrypted in the database.
  - **BCrypt Password Hashing:** Secure password storage.
  - **CSRF Protection:** Token-based validation on all state-changing POST requests.
  - **Security Headers:** Strict Content Security Policy (CSP), X-Frame-Options, X-XSS-Protection.
  - **XSS Prevention:** Output escaping via JSTL `<c:out>`.
- **Payment Gateway Integration:** Razorpay integration for seamless monthly maintenance fee collection (supports UPI, Cards, NetBanking).
- **Automated Email Notifications:** Asynchronous email alerts using JavaMail for notices, complaint status updates, payment receipts, and welcome emails.
- **Auto-Initializing Database:** The application automatically provisions the required 13 database tables upon startup if they don't exist.

## 🛠️ Technology Stack

- **Backend:** Java 8+, Servlets, JSP
- **Frontend:** HTML5, Vanilla CSS (Custom Premium Design System), Vanilla JavaScript
- **Database:** MySQL 8.0+
- **Server:** Apache Tomcat / GlassFish
- **Dependencies:** JSTL 1.2, Gson 2.10.1, JavaMail 1.6.2, MySQL Connector/J 9.2.0

---

## 🚀 How to Run Locally

### Prerequisites

1. **Java Development Kit (JDK):** Version 8 or higher.
2. **Database:** MySQL 8.0+ running locally (default port `3306`).
3. **IDE:** NetBeans, IntelliJ IDEA, or Eclipse configured with a Java EE server (like GlassFish or Tomcat).

### Step 1: Database Setup

Ensure your local MySQL server is running. Create an empty database:
```sql
CREATE DATABASE societease;
```
*Note: You do not need to run any SQL scripts to create tables. The `AppStartupListener` will automatically create all necessary tables when the application starts.*

### Step 2: Project Setup (NetBeans)

1. Clone or download the repository.
2. Open the project in NetBeans (`File -> Open Project`).
3. **Dependencies:** The project requires a few JAR files. If they are not already in `web/WEB-INF/lib`, run the provided `download-deps.bat` script from the project root to fetch them automatically.
4. **Environment Variables:** By default, the application attempts to connect to `jdbc:mysql://localhost:3306/societease` with the user `root` and an empty password. 

To use your specific database credentials or configure other services (like Email and Payments), you need to set environment variables. In NetBeans, you can pass these as JVM arguments:
- Right-click the project -> **Properties** -> **Run**.
- In the **VM Options** field, add:
  ```
  -DDB_URL=jdbc:mysql://localhost:3306/societease?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  -DDB_USER=your_db_username
  -DDB_PASSWORD=your_db_password
  -DSOCIETEASE_ENCRYPT_KEY=your_secure_32_byte_base64_encoded_key
  ```
  *(To generate a new encryption key, you can temporarily run the `main` method in `util.EncryptionUtil.java`)*

### Step 3: Run the Application

1. **Clean and Build** the project (Shift + F11).
2. **Run** the project (F6). It will deploy to your configured server (e.g., GlassFish).
3. Open your browser and navigate to the application URL (usually `http://localhost:8080/SocietEaseWeb/`).

### Step 4: First Use

1. Click **"Register Your Society"** on the landing page.
2. Complete the 3-step wizard to create a society, configure building towers/apartments, and create the Responsible Person (RP) account.
3. Log in with the RP credentials to access the Admin Dashboard.

---

## ⚙️ Configuration (Environment Variables)

For full functionality, configure the following environment variables (or JVM properties `-Dkey=value`). In a production environment (like Railway.app), these are set in the deployment dashboard.

| Variable Name | Description | Default (Local) |
|---------------|-------------|-----------------|
| `DB_URL` | JDBC connection string | `jdbc:mysql://localhost:3306/societease` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | *(empty)* |
| `SOCIETEASE_ENCRYPT_KEY` | 256-bit AES Key (Base64) for PII | `DefaultFallbackSecretKeyForDev12` |
| `SMTP_USER` | Email address for sending notifications | *(empty)* |
| `SMTP_PASSWORD` | Email App Password | *(empty)* |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID | `rzp_test_PLACEHOLDER` |
| `RAZORPAY_KEY_SECRET` | Razorpay API Secret | *(empty)* |

---

## 🌐 Deployment Guide (e.g., Railway.app)

To make the application live on the internet, you can deploy it to PaaS providers like Railway, Heroku, or Render using a Tomcat Docker container.

### Preparing for Deployment

1. **Database Provisioning:** Add a MySQL database plugin in your hosting provider's dashboard.
2. **Set Environment Variables:** Copy the credentials provided by the MySQL plugin and set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in your application service's variables.
3. **Set Production Keys:** Generate a strong `SOCIETEASE_ENCRYPT_KEY`. Set up your live `SMTP_USER`/`SMTP_PASSWORD` and `RAZORPAY_KEY_ID`/`RAZORPAY_KEY_SECRET`.

### Deployment using Docker (Tomcat)

Create a `Dockerfile` in the root of your project:

```dockerfile
FROM tomcat:9.0-jdk8-openjdk

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file to Tomcat webapps directory as ROOT.war
# Assuming the build process outputs SocietEaseWeb.war to the dist/ directory
COPY dist/SocietEaseWeb.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
```

If your platform supports building Java applications directly (via Maven/Gradle/Ant), configure the build command (e.g., `ant clean dist`) and the start command appropriately.

---

## 🛡️ Security Notes

- **Never** commit `.env` files, actual passwords, or encryption keys to version control.
- Ensure the production database is not publicly accessible unless strictly necessary (use VPC/private networks if possible).
- The encryption key (`SOCIETEASE_ENCRYPT_KEY`) must remain constant once data is stored. If lost, all encrypted PII will be unrecoverable.

## 📄 License
*Specify your license here (e.g., MIT, GPL)*
