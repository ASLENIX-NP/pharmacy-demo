# Configure these variables with your database and mail credentials
$env:DB_PORT="3306"
$env:DB_NAME="your_database_name"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_db_password"
$env:DOMAIN="your_domain"
$env:MAIL_PASSWORD="your_mail_password"
$env:MAIL_USERNAME="your_mail_username"

.\mvnw.cmd spring-boot:run