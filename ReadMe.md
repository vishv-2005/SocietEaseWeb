# SocietEase — Multi-Tenant Society Management Platform

SocietEase is a modern, secure, multi-tenant SaaS platform for residential society management. It allows Responsible Persons (RPs) to register their societies, automatically generates unit structures, and provides a comprehensive dashboard for managing residents, helpers, committees, complaints, notices, and maintenance collections.

Recently enhanced with **state-of-the-art 3D UI animations** and a **flexible multi-type society registration system**, SocietEase delivers a premium, engaging user experience tailored to any residential structure.

---

## ✨ Key Features

### 🏢 Flexible Multi-Type Society Registration
- **Support for Any Structure:** Seamlessly register Apartments, Villas, Bungalows, Duplexes, Row Houses, Plots, or custom unit types.
- **Dynamic Configuration:** Tell the system how your society works. Support for Blocks/Wings/Sectors/Phases or flat structures.
- **Customizable Labeling:** Generate unit labels your way — plain sequential (`1, 2, 3`), block-unit (`A-1, B-3`), prefix style (`Villa-1`), or full labels.
- **Live Preview:** Instantly see how your unit labels will be generated during the registration process.

### 🎨 Stunning 3D UI & Animations (Premium UX)
- **Three.js Landing Page:** Interactive 3D scene featuring floating particles, window glow pulsing, elastic camera easing, and color-shifting orbiting rings.
- **Holographic Shimmer:** Premium rainbow gradient sweeps across cards on hover.
- **Magnetic Interactions:** Buttons subtly pull toward the user's cursor for a physical, tactile feel, paired with expanding click ripples.
- **Spring-Physics Entrances:** UI elements cascade and bounce into view using custom cubic-bezier timing functions.
- **3D Wizard Transitions:** Registration steps flip into view with depth perspective.
- **Neon Focus Glows:** Input fields pulse with a cyan glow ring on focus, elevating standard forms.
- **Parallax Ambient Shell:** Floating background orbs shift at different depths based on user scroll.

### 🛡️ Core Platform Capabilities
- **Multi-Tenant Architecture:** Multiple independent societies operate securely on a single deployment.
- **Role-Based Access Control:** Distinct dashboards for Responsible Persons (RPs) and Residents.
- **Robust Security & Privacy:**
  - **AES-256-GCM Encryption:** Encrypts PII (names, phone numbers, emails, Aadhar).
  - **BCrypt Hashing:** Secure password storage.
  - **CSRF & XSS Protection:** Token validation and JSTL output escaping.
- **Payment Gateway Integration:** Razorpay integration for seamless maintenance fee collection (UPI, Cards, NetBanking).
- **Automated Notifications:** Asynchronous JavaMail alerts for notices, complaints, receipts, and registration.
- **Auto-Initializing Database:** Startup listeners automatically provision all required schemas and perform zero-downtime migrations.

---

## 🛠️ Technology Stack

- **Backend:** Java 8+, Servlets, JSP
- **Frontend:** HTML5, Vanilla CSS (Premium 3D Design System), Vanilla JS, Three.js
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
*Note: You do not need to run any SQL scripts to create tables. The `AppStartupListener` will automatically create and migrate all necessary tables when the application starts.*

### Step 2: Project Setup (NetBeans)

1. Clone or download the repository.
2. Open the project in NetBeans (`File -> Open Project`).
3. **Dependencies:** Ensure JAR files are in `web/WEB-INF/lib`. Run the `download-deps.bat` script from the project root if needed.
4. **Environment Variables:** By default, the app attempts to connect to `jdbc:mysql://localhost:3306/societease` with user `root` and an empty password.

To use custom credentials, add VM Options in NetBeans (Right-click project -> **Properties** -> **Run** -> **VM Options**):
```
-DDB_URL=jdbc:mysql://localhost:3306/societease?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
-DDB_USER=your_db_username
-DDB_PASSWORD=your_db_password
-DSOCIETEASE_ENCRYPT_KEY=your_secure_32_byte_base64_encoded_key
```

### Step 3: Run the Application

1. **Clean and Build** the project (Shift + F11).
2. **Run** the project (F6).
3. Open your browser to the application URL (usually `http://localhost:8080/SocietEaseWeb/`).

### Step 4: First Use

1. Click **"Register Your Society"** on the landing page to experience the new 3D wizard.
2. Choose between **Apartment/Tower** or **Individual Units** and configure your layout.
3. Log in with the newly created RP credentials to access the Admin Dashboard.

---

## ⚙️ Configuration (Environment Variables)

For full functionality, configure the following environment variables (or JVM properties `-Dkey=value`):

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

## 🌐 Deployment Guide

To deploy to PaaS providers like Railway, Heroku, or Render using a Tomcat Docker container:

### Preparing for Deployment
1. **Database Provisioning:** Add a MySQL database plugin in your hosting provider's dashboard.
2. **Set Environment Variables:** Add `DB_URL`, `DB_USER`, and `DB_PASSWORD`.
3. **Set Production Keys:** Generate a strong `SOCIETEASE_ENCRYPT_KEY`. Set up live `SMTP` and `RAZORPAY` keys.

### Deployment using Docker (Tomcat)
Create a `Dockerfile` in the root of your project:
```dockerfile
FROM tomcat:9.0-jdk8-openjdk

# Remove default Tomcat apps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file to Tomcat webapps directory as ROOT.war
COPY dist/SocietEaseWeb.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
```

---

## 🛡️ Security Notes

- **Never** commit `.env` files, actual passwords, or encryption keys to version control.
- Ensure the production database is not publicly accessible.
- The encryption key (`SOCIETEASE_ENCRYPT_KEY`) must remain constant once data is stored. If lost, encrypted PII becomes unrecoverable.
