# ConfigManager Backend

Spring Boot backend service for the ConfigManager application.

## Features

- RESTful API for configuration management
- Support for multiple environments (dev, staging, prod)
- Configuration validation and type checking
- Export configurations in various formats (.env, JSON, YAML)
- Audit trail for configuration changes
- API key authentication for external applications
- H2 database for development, PostgreSQL for production

## API Endpoints

### Configuration Management

- `GET /api/config` - Get all configurations
- `GET /api/config/environments` - Get all available environments
- `GET /api/config/{environment}` - Get configurations for specific environment
- `GET /api/config/{environment}/map` - Get configurations as key-value map
- `GET /api/config/{environment}/{key}` - Get specific configuration
- `POST /api/config` - Create new configuration
- `PUT /api/config/{environment}/{key}` - Update configuration
- `DELETE /api/config/{environment}/{key}` - Delete configuration

### Export/Import

- `GET /api/config/{environment}/export/env` - Export as .env file
- `POST /api/config/import` - Import configurations from file

### Search

- `GET /api/config/{environment}/search?q={searchTerm}` - Search configurations

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+

### Running the Application

```bash
# Clone the repository
git clone <repository-url>
cd backend

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/config-manager-backend-1.0.0.jar
```

The application will start on `http://localhost:8080`

### Database Access

For development, you can access the H2 console at:
`http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:configdb`
- Username: `sa`
- Password: `password`

## Configuration

### Application Properties

Key configuration properties in `application.properties`:

```properties
# Server port
server.port=8080

# Database configuration
spring.datasource.url=jdbc:h2:mem:configdb
spring.datasource.username=sa
spring.datasource.password=password

# JWT configuration
app.jwt.secret=mySecretKey
app.jwt.expiration=86400000

# CORS configuration
app.cors.allowed-origins=http://localhost:3000
```

### Environment Variables

You can override properties using environment variables:

- `DATABASE_URL` - Database connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `JWT_SECRET` - JWT secret key
- `CORS_ORIGINS` - Allowed CORS origins

## Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/configmanager/
│   │       ├── ConfigManagerApplication.java
│   │       ├── entity/           # JPA entities
│   │       ├── repository/       # Data access layer
│   │       ├── service/          # Business logic
│   │       ├── controller/       # REST controllers
│   │       └── config/           # Configuration classes
│   └── resources/
│       ├── application.properties
│       └── data.sql             # Sample data
└── test/                        # Unit and integration tests
```

### Adding New Features

1. Create entity classes in `entity/` package
2. Add repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Add tests in `test/` directory

### Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=ConfigurationServiceTest

# Run integration tests
./mvnw test -Dtest=**/*IntegrationTest
```

## Deployment

### Docker

```bash
# Build Docker image
docker build -t config-manager-backend .

# Run container
docker run -p 8080:8080 config-manager-backend
```

### Production Configuration

For production deployment:

1. Use PostgreSQL database instead of H2
2. Set strong JWT secret
3. Configure proper CORS origins
4. Enable HTTPS
5. Set up proper logging configuration

Example production `application-prod.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/configmanager
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# Security
app.jwt.secret=${JWT_SECRET}
app.cors.allowed-origins=${CORS_ORIGINS}

# Logging
logging.level.com.configmanager=INFO
logging.file.name=/var/log/configmanager/application.log
```

## API Usage Examples

### Get all configurations for production environment

```bash
curl -X GET http://localhost:8080/api/config/production
```

### Create a new configuration

```bash
curl -X POST http://localhost:8080/api/config \
  -H "Content-Type: application/json" \
  -d '{
    "key": "NEW_CONFIG",
    "value": "config_value",
    "environment": "development",
    "description": "New configuration",
    "isSensitive": false
  }'
```

### Update a configuration

```bash
curl -X PUT http://localhost:8080/api/config/development/NEW_CONFIG \
  -H "Content-Type: application/json" \
  -d '{
    "value": "updated_value",
    "description": "Updated configuration"
  }'
```

### Export configurations as .env file

```bash
curl -X GET http://localhost:8080/api/config/production/export/env \
  -o production.env
```

## Troubleshooting

### Common Issues

1. **Port already in use**: Change server port in `application.properties`
2. **Database connection errors**: Check database configuration
3. **CORS errors**: Verify allowed origins configuration

### Logs

Application logs are available in the console output. For production, configure file logging:

```properties
logging.file.name=/var/log/configmanager/application.log
logging.level.com.configmanager=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request
