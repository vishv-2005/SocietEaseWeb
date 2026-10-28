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
- **Database:** MySQL 8.0+ (hosted on Aiven, SSL connection)
- **Server:** Apache Tomcat 9 / GlassFish
- **DevOps:** Docker (multi-stage build), GitHub Actions, Oracle Cloud VM
- **Dependencies:** JSTL 1.2, Gson 2.10.1, JavaMail 1.6.2, MySQL Connector/J 9.2.0

---

## 🚀 How to Run Locally

### Prerequisites

1. **Java Development Kit (JDK):** Version 8 or higher.
2. **Database:** MySQL 8.0+ (local, or a managed instance such as Aiven). The app always connects with SSL (`useSSL=true&requireSSL=true`). A stock MySQL 8 server has SSL turned on by default, so this works locally too.
3. **IDE (optional):** NetBeans, IntelliJ IDEA, or Eclipse configured with Tomcat or GlassFish. You can also skip the IDE and use Docker (see below).

### Option A: Run with Docker (recommended)

The `Dockerfile` compiles the source directly (multi-stage build), so you don't need a pre-built WAR or a local JDK.

```bash
docker build -t societease .

docker run -d --name societease -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_PORT=3306 \
  -e DB_NAME=societease -e DB_USER=root -e DB_PASSWORD=your_password \
  -e SOCIETEASE_ENCRYPT_KEY=your_base64_32_byte_key \
  societease
```

Or put the variables in a `.env` file (it's git-ignored) and pass `--env-file .env`.

The app is served at the root context: `http://localhost:8080/`.

### Option B: Run from NetBeans

1. Clone the repository and open it in NetBeans (`File -> Open Project`).
2. **Dependencies:** the required JARs are committed in `web/WEB-INF/lib`. If any are missing, run `download-deps.bat`.
3. **Database credentials:** set them as environment variables, or as VM Options (Right-click project -> **Properties** -> **Run** -> **VM Options**):
   ```
   -DDB_HOST=localhost -DDB_PORT=3306 -DDB_NAME=societease -DDB_USER=root -DDB_PASSWORD=your_password
   ```
   > `SOCIETEASE_ENCRYPT_KEY`, `SMTP_*` are read **only from environment variables**, not from `-D` flags. Set them in your server's environment.
4. **Clean and Build** (Shift + F11), then **Run** (F6).
5. Open `http://localhost:8080/SocietEaseWeb/`.

### Database Setup

You don't need to run any SQL scripts. On startup, `AppStartupListener` runs `CREATE DATABASE IF NOT EXISTS` for `DB_NAME`, then creates and migrates all tables automatically.

### First Use

1. Click **"Register Your Society"** on the landing page to open the 3D registration wizard.
2. Choose between **Apartment/Tower** or **Individual Units** and configure your layout.
3. Log in with the newly created RP credentials to access the Admin Dashboard.

---

## ⚙️ Configuration (Environment Variables)

| Variable | Description | Default | Read from |
|----------|-------------|---------|-----------|
| `DB_HOST` | MySQL host | `societease-db-societeaseweb.c.aivencloud.com` | env, `-D` |
| `DB_PORT` | MySQL port | `24410` | env, `-D` |
| `DB_NAME` | Database name (auto-created) | `defaultdb` | env, `-D` |
| `DB_USER` | Database username | `avnadmin` | env, `-D` |
| `DB_PASSWORD` | Database password | **none, required** | env, `-D` |
| `SOCIETEASE_ENCRYPT_KEY` | Base64-encoded 256-bit AES key for PII | insecure dev fallback (logs a warning) | env |
| `SMTP_HOST` / `SMTP_PORT` | Mail server | `smtp.gmail.com` / `587` | env |
| `SMTP_USER` / `SMTP_PASSWORD` | Sender address / app password | *(empty, emails disabled)* | env |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | Razorpay API credentials | *(empty)* | `config.properties`, `-D`, env |

The defaults point at the hosted Aiven database. For local development, set `DB_HOST`, `DB_PORT`, `DB_NAME` and `DB_USER`.

To generate an encryption key:
```bash
openssl rand -base64 32
```

---

## 🔄 CI/CD Pipeline

GitHub Actions ([`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml)) runs on every push and pull request to `main`:

1. **Build & smoke test:** builds the Docker image from source, starts it against a throwaway MySQL 8.0 service container, and waits for `/robots.txt` to return HTTP 200.
2. **Deploy to Oracle Cloud** (pushes to `main` only, `production` environment): connects to the VM over SSH, runs `git reset --hard origin/main`, rebuilds the image, restarts the `societease` container on port 8080 (loading the VM's `.env` if one exists), runs a health check, and prunes old images.

### Required GitHub secrets (`production` environment)

| Secret | Purpose | Required |
|--------|---------|----------|
| `OCI_HOST` | Public IP / hostname of the Oracle Cloud VM | Yes* |
| `OCI_SSH_KEY` | Private SSH key for the VM | Yes* |
| `OCI_USERNAME` | SSH user | No (default `ubuntu`) |
| `OCI_APP_DIR` | Path of the repo clone on the VM | No (default `~/SocietEaseWeb`) |

\* If `OCI_HOST` or `OCI_SSH_KEY` is missing, the deploy step is skipped with a warning instead of failing.

### One-time VM setup

1. Install Docker and Git on the VM, and add the SSH user to the `docker` group.
2. Clone this repository into `OCI_APP_DIR`.
3. Create a `.env` file in that directory with the production values from the configuration table above.
4. Open port `8080` in the VM firewall and in the OCI security list (or put a reverse proxy in front of it).

---

## 🛡️ Security Notes

- **Never** commit `.env` files, actual passwords, or encryption keys to version control.
- Ensure the production database is not publicly accessible.
- `DB_PASSWORD` has no default and must always be supplied through the environment. Keep production values in the VM's git-ignored `.env` and in GitHub secrets.
- The DB connection encrypts traffic but does not verify the server certificate (`verifyServerCertificate=false`). For hardened production use, import Aiven's CA certificate and turn verification on.
- The encryption key (`SOCIETEASE_ENCRYPT_KEY`) must remain constant once data is stored. If lost, encrypted PII becomes unrecoverable.
