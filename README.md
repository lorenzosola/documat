# Documat - Document Management System

A comprehensive DMS (Document Management System) with client/server architecture developed in Java and using MySQL as the database management system.

## Features

- **User Authentication & Authorization**: Secure login system with JWT tokens and role-based access control (Admin, Manager, User)
- **Document Management**: Upload, download, view, and delete documents
- **Search Functionality**: Search documents by title, description, or tags
- **Category Organization**: Organize documents by categories
- **Version Tracking**: Track document versions and metadata
- **RESTful API**: Complete REST API for all document operations
- **JavaFX Client**: Desktop client application for easy interaction

## Architecture

The system consists of two main modules:

### Server Module (`documat-server`)
- **Framework**: Spring Boot 3.2.0
- **Database**: MySQL 8.0+
- **Security**: Spring Security with JWT authentication
- **ORM**: JPA/Hibernate
- **REST API**: Spring MVC controllers

### Client Module (`documat-client`)
- **UI Framework**: JavaFX 21
- **HTTP Client**: OkHttp
- **JSON Processing**: Gson

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.6 or higher
- **MySQL**: 8.0 or higher
- **Operating System**: Windows, Linux, or macOS

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/lorenzosola/documat.git
cd documat
```

### 2. Setup MySQL Database

Create a MySQL database and user:

```sql
CREATE DATABASE documat_db;
CREATE USER 'documat_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON documat_db.* TO 'documat_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Database Connection

Edit `documat-server/src/main/resources/application.properties` or set environment variables:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/documat_db
spring.datasource.username=documat_user
spring.datasource.password=your_password
```

**For production, use environment variables instead of hardcoding credentials:**

```bash
export DB_URL=jdbc:mysql://localhost:3306/documat_db
export DB_USERNAME=documat_user
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_long_random_secret_key_base64_encoded
export CORS_ALLOWED_ORIGINS=https://your-domain.com
```

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Server

```bash
cd documat-server
mvn spring-boot:run
```

The server will start on `http://localhost:8080`

### 6. Initialize Database

The application will automatically create the necessary tables. You can run the initialization script to create default roles and admin user:

```bash
mysql -u documat_user -p documat_db < documat-server/src/main/resources/db/migration/init.sql
```

Default admin credentials:
- Username: `admin`
- Password: `admin123`

### 7. Run the Client

```bash
cd documat-client
mvn javafx:run
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration

### Documents
- `GET /api/documents` - Get all documents
- `GET /api/documents/my` - Get current user's documents
- `GET /api/documents/{id}` - Get document by ID
- `POST /api/documents/upload` - Upload a new document
- `GET /api/documents/download/{id}` - Download a document
- `DELETE /api/documents/{id}` - Delete a document (Manager/Admin only)
- `GET /api/documents/search?keyword={keyword}` - Search documents

## Usage

### Using the Client Application

1. Launch the client application
2. Login with your credentials (use admin/admin123 for first login)
3. The main window will display available documents
4. Use the interface to upload, view, search, and manage documents

### Using the REST API

Example using curl:

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Upload Document
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer {your_jwt_token}" \
  -F "file=@/path/to/document.pdf" \
  -F "title=My Document" \
  -F "category=Reports" \
  -F "description=Sample document"

# Search Documents
curl -X GET "http://localhost:8080/api/documents/search?keyword=report" \
  -H "Authorization: Bearer {your_jwt_token}"
```

## Security

- **Passwords**: Encrypted using BCrypt
- **Authentication**: JWT-based token authentication
- **Authorization**: Role-based access control for sensitive operations
- **File Upload**: Size limits and MIME type validation (PDF, Word, Excel, images)
- **CORS**: Configurable allowed origins (default: localhost)
- **Configuration**: Support for environment variables to avoid hardcoding secrets
- **CSRF Protection**: Disabled for stateless JWT API (CSRF is not applicable to stateless REST APIs using bearer tokens)

### Security Best Practices

For production deployments:

1. **Never commit secrets**: Use environment variables for database credentials and JWT secrets
2. **Configure CORS properly**: Set `CORS_ALLOWED_ORIGINS` to your specific domain(s)
3. **Use HTTPS**: Always use SSL/TLS in production
4. **Strong JWT secret**: Use a long, randomly generated secret key
5. **Logging levels**: Set logging to INFO or WARN in production (not DEBUG)
6. **File upload restrictions**: Review and adjust allowed MIME types based on your needs
7. **Token Storage**: Clients should store JWT tokens securely (not in localStorage for web apps; use httpOnly cookies or secure storage)

Example production environment variables:
```bash
export DB_URL=jdbc:mysql://prod-db-server:3306/documat_db?useSSL=true
export DB_USERNAME=documat_prod_user
export DB_PASSWORD=your_very_secure_password
export JWT_SECRET=$(openssl rand -base64 64)
export CORS_ALLOWED_ORIGINS=https://your-domain.com
export LOG_LEVEL=INFO
export SECURITY_LOG_LEVEL=WARN
```

### Why CSRF Protection is Disabled

This application disables CSRF protection because it uses stateless JWT authentication. CSRF attacks are not applicable to:
- Stateless REST APIs
- APIs that use bearer token authentication (Authorization header)
- APIs with no session cookies

If you plan to add session-based authentication or use cookies for authentication, you should re-enable CSRF protection.

## Development

### Project Structure

```
documat/
├── pom.xml                          # Parent POM
├── documat-server/                  # Server module
│   ├── src/main/java/
│   │   └── com/documat/server/
│   │       ├── controller/          # REST controllers
│   │       ├── service/             # Business logic
│   │       ├── repository/          # Data access
│   │       ├── entity/              # JPA entities
│   │       ├── dto/                 # Data transfer objects
│   │       ├── config/              # Configuration
│   │       └── exception/           # Exception handlers
│   └── src/main/resources/
│       ├── application.properties
│       └── db/migration/            # Database scripts
└── documat-client/                  # Client module
    ├── src/main/java/
    │   └── com/documat/client/
    │       ├── controller/          # JavaFX controllers
    │       ├── service/             # API service
    │       └── model/               # Data models
    └── src/main/resources/
        └── fxml/                    # JavaFX layouts
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
cd documat-server
mvn test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and questions, please open an issue on the GitHub repository.