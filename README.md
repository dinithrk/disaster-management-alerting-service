<div align="center">
  <h1>🚨 Disaster Management Alerting Service</h1>
  <p><strong>A robust microservice for processing, scheduling, and dispatching alerts within the Disaster Management System.</strong></p>

  [![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://java.com)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15%2B-blue.svg)](https://www.postgresql.org/)
  [![Maven](https://img.shields.io/badge/Maven-Build-C71A22.svg)](https://maven.apache.org/)
</div>

<br/>

## 📖 Overview

The **Alerting Service** is a core microservice developed for the second-year project's Disaster Management System by **KernelX**. It is responsible for continuously evaluating alert conditions, scheduling notifications, and managing the lifecycle of disaster alerts.

### ✨ Key Features
- **Automated Scheduling**: Cron-based alert evaluation and dispatch mechanism.
- **Time Window Analysis**: Configurable sliding windows for tracking recent events.
- **Data Retention Policies**: Automated cleanup of outdated alerts based on retention configurations.
- **Relational Persistence**: Robust data storage using PostgreSQL and Spring Data JPA.

---

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 |
| **Framework** | Spring Boot | 4.0.3 |
| **Database** | PostgreSQL | (Driver integrated) |
| **ORM** | Spring Data JPA / Hibernate | - |
| **Utilities** | Lombok | - |
| **Build Tool** | Maven | - |

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your local machine:
- [Java 21 JDK](https://adoptium.net/) or higher
- [Maven](https://maven.apache.org/install.html)
- [PostgreSQL](https://www.postgresql.org/download/)

### ⚙️ Configuration

The service connects to a local PostgreSQL instance by default. 
Create a database named `kernelx` and a user `kernelx`.

To run the application successfully, you must provide the database password via an environment variable.

#### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_PASSWORD` | Password for the `kernelx` PostgreSQL user | `your_secure_password` |

#### Application Properties

Key configurable properties found in `src/main/resources/application.yaml`:

```yaml
server:
  port: 8093

alert:
  scheduler-cron: "0 */1 * * * *"  # Runs every minute
  time-window-in-mins: 10          # Evaluates events within the last 10 mins
  retention-in-days: 1             # Keeps alerts for 1 day
```

### 💻 Installation & Execution

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd disaster-management-alerting-service
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```
   *(Note: Ensure `DB_PASSWORD` is set if running tests that require DB connectivity)*

3. **Run the microservice**:
   ```bash
   # On Windows
   set DB_PASSWORD=your_password
   mvn spring-boot:run
   
   # On Linux/macOS
   DB_PASSWORD=your_password mvn spring-boot:run
   ```

The service will start and run on `http://localhost:8093`.

---

## 🏗️ Project Structure

```text
disaster-management-alerting-service/
├── src/
│   ├── main/
│   │   ├── java/com/kernelx/alerts/    # Application source code
│   │   └── resources/                  # Configuration files (application.yaml)
│   └── test/                           # Unit and integration tests
├── pom.xml                             # Maven build configuration
└── README.md                           # Project documentation
```

---

## 📄 License

This project is licensed under the standard MIT/Apache license format. See the `LICENSE` file for details.
