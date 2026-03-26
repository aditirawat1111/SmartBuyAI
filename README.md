# SmartBuyAI

SmartBuyAI is a Spring Boot backend for product price comparison across e-commerce platforms.  
It provides search, product comparison, price history APIs, and a scheduled simulation job that updates platform prices daily.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA (Hibernate)
- MySQL
- Spring Validation
- Spring Retry + Spring AOP
- Maven
- Lombok

## What It Does

- Search products by name or category
- Compare a product across platforms (price, rating, delivery)
- Return historical price points for trend tracking
- Calculate a smart score to recommend the best deal
- Simulate realistic daily price changes using a scheduler
- Seed default sample data on startup (`default` profile)

## Project Structure

- `src/main/java/com/aditi/smartbuy/controller` - REST APIs
- `src/main/java/com/aditi/smartbuy/service` - business logic (search, compare, history, smart score)
- `src/main/java/com/aditi/smartbuy/repository` - JPA repositories and custom queries
- `src/main/java/com/aditi/smartbuy/entity` - database entities
- `src/main/java/com/aditi/smartbuy/scheduler` - price simulation scheduler + page processor
- `src/main/java/com/aditi/smartbuy/config` - startup data loader
- `src/main/java/com/aditi/smartbuy/dto` - API response DTOs
- `src/main/resources/application.properties` - runtime configuration

## API Endpoints

Base path: `/api/products`

- `GET /search?q={query}`  
  Search products by name/category. Returns all products when query is empty.

- `GET /{id}`  
  Get product details by product ID.

- `GET /{id}/compare`  
  Compare platform prices for a product and get best-deal + smart score output.

- `GET /{id}/price-history?limit={n}`  
  Get recent price history for a product.  
  - Default limit: `90`  
  - Max limit: `365`

## Scheduler Overview

Price simulation runs on a cron schedule (`smartbuy.scheduler.cron`) and processes mappings in pages.

- Reads product-platform mappings in batches
- Applies deterministic + random simulation factors (weekend, demand, competitor, noise)
- Clamps simulated values between min/max price factors
- Updates current price and writes a new `price_history` record
- Retries transient DB failures using configured retry/backoff

## Data Model (Core)

- `users`
- `products`
- `platforms`
- `product_platform_mapping` (unique per product/platform pair)
- `price_history`

## Setup and Run

### Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8+

### Configure Database

Update `src/main/resources/application.properties`:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Recommended: keep credentials out of source control (use environment variables or profile-specific property files).

### Run Locally

```bash
mvn spring-boot:run
```

### Build

```bash
mvn clean package
```

### Test

```bash
mvn test
```

### Run Packaged JAR

```bash
java -jar target/smartbuy-0.0.1-SNAPSHOT.jar
```

## Important Configuration

`application.properties` includes:

- Server/app: `server.port`, `spring.application.name`
- JPA + Hibernate batching and SQL settings
- Scheduler controls:
  - `smartbuy.scheduler.page-size`
  - `smartbuy.scheduler.min-price-factor`
  - `smartbuy.scheduler.max-price-factor`
  - `smartbuy.scheduler.cron`
- Simulation tuning:
  - `smartbuy.scheduler.simulation.*`
- Retry settings:
  - `smartbuy.scheduler.retry.max-attempts`
  - `smartbuy.scheduler.retry.backoff-ms`
