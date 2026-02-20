# SmartBuyAI

E-commerce project built with Spring Boot.

## Project Information

- **Name**: SmartBuyAI
- **Language**: Java
- **Build Tool**: Maven
- **Group**: com.aditi
- **Artifact**: smartbuy
- **Package**: com.aditi.smartbuy
- **Java Version**: 17

## Dependencies

### Required
- Spring Web
- Spring Data JPA
- Lombok
- Validation
- MySQL Driver

### Helpful
- Spring Boot DevTools

## Getting Started

1. Make sure you have Java 17 and Maven installed
2. Configure your MySQL database connection in `src/main/resources/application.properties`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Database Configuration

Update the database credentials in `application.properties`:
- `spring.datasource.url`: MySQL connection URL
- `spring.datasource.username`: Database username
- `spring.datasource.password`: Database password

The application will create the database automatically if it doesn't exist (when `createDatabaseIfNotExist=true` is set).
