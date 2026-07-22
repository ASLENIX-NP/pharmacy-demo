# Pharmacy Demo

Pharmacy Demo is a Spring Boot web application for managing pharmacy operations such as user access, inventory, sales, billing, purchase workflows, dashboards, and supporting reports. The project uses Thymeleaf for server-rendered views, MySQL for persistence, and includes email support and PDF export features.

## Installation

### Prerequisites

- Java 21
- Maven 3.9+ or the included Maven Wrapper
- MySQL 8+
- A Gmail account or SMTP credentials if you plan to use password reset/email features

### Setup steps

1. Clone the repository.
2. Create a MySQL database for the application.
3. Set the required environment variables used in `src/main/resources/application.properties`:

```powershell
$env:DB_PORT="3306"
$env:DB_NAME="DB_NAME"
$env:DB_USERNAME="DB_USERNAME"
$env:DB_PASSWORD="DB_PASSWORD"
$env:DOMAIN="DOMAIN"
$env:MAIL_PASSWORD="MAIL_PASSWORD"
$env:MAIL_USERNAME="MAIL_USERNAME"
```

4. Start the application from the project root:

```powershell

.\mvnw.cmd spring-boot:run
```

### Run with one command (recommended for local development)

If you want one command every time, create a local script named `run_local.ps1` in the project root:

```powershell
$env:DB_PORT="3306"
$env:DB_NAME="DB_NAME"
$env:DB_USERNAME="DB_USERNAME"
$env:DB_PASSWORD="DB_PASSWORD"
$env:DOMAIN="DOMAIN"
$env:MAIL_PASSWORD="MAIL_PASSWORD"
$env:MAIL_USERNAME="MAIL_USERNAME"

.\mvnw.cmd spring-boot:run
```

Then run:

```powershell
.\run_local.ps1
```

This helps avoid the common issue where `spring-boot:run` fails in a terminal because environment variables are not set in that shell session.

5. Open the app in your browser at:

```text
http://localhost:8080
```

## Usage

After the application starts, use the browser to access the login page and role-based dashboards.

### Common pages

- Login: `http://localhost:8080/login`
- Dashboard redirect: `http://localhost:8080/Dashboard`
- Forgot password: `http://localhost:8080/forgot-password`

### Example browser flow

1. Sign in through the login page.
2. You will be redirected to the appropriate dashboard based on your role.
3. Manage pharmacy data such as products, inventory, sales, and billing from the available dashboard pages.

## Contribution Guidelines

Contributions are welcome. If you want to improve the project:

1. Create a feature branch.
2. Make focused, well-documented changes.
3. Test your changes locally before opening a pull request.
4. Keep code style and template structure consistent with the existing Spring Boot application.
5. Include a clear description of the problem solved and any setup changes.

Suggested contribution workflow:

```powershell
git checkout -b feature/your-change
git status
git add .
git commit -m "Describe your change"
git push origin feature/your-change
```

## License

No license file is currently included in this repository. If you plan to share or publish the project, add a `LICENSE` file and specify the terms clearly before distributing it.
