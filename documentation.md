# FX Deals Processing Application

## Overview

This application is designed to process and store foreign exchange (FX) deals. It includes features for validating deal data, storing it in a database, and ensuring that each deal is processed only once. The application supports batch processing of deals via CSV input.

## Technologies Used

- **Java 17**: The core programming language used for developing the application.
- **Spring Boot 3.3.2**: A framework used for building and deploying the application, providing features like dependency injection, web services, and more.
- **Hibernate/JPA**: Used for Object-Relational Mapping (ORM) to interact with the PostgreSQL database.
- **PostgreSQL**: The relational database used to store FX deals and currency information.
- **JUnit**: For unit testing the application to ensure functionality works as expected.
- **Docker**: Used to containerize the application and database for easy deployment and testing.

## Database Structure

The database consists of two main tables:

### 1. `currency`
- **currency_id (SERIAL PRIMARY KEY)**: A unique identifier for each currency.
- **currency_code (VARCHAR(3) UNIQUE NOT NULL)**: The ISO 4217 currency code (e.g., USD, EUR).
- **currency_name (VARCHAR(255) NOT NULL)**: The full name of the currency (e.g., United States Dollar).
- **currency_symbol (VARCHAR(3))**: Optional symbol for the currency (e.g., $).

### 2. `deal`
- **deal_id (SERIAL PRIMARY KEY)**: A unique identifier for each deal.
- **deal_unique_id (VARCHAR(255) UNIQUE NOT NULL)**: A provided unique ID for each deal.
- **from_currency_code (CHAR(3) NOT NULL)**: The currency code for the currency being sold (foreign key to `currency_id`).
- **to_currency_code (CHAR(3) NOT NULL)**: The currency code for the currency being bought (foreign key to `currency_id`).
- **deal_timestamp (TIMESTAMP NOT NULL)**: The timestamp of when the deal was made.
- **deal_amount (NUMERIC(18, 2) NOT NULL)**: The amount of the deal in the "from" currency.

### Why Two Tables?

The decision to use two tables (`currency` and `deal`) was made to ensure data normalization and maintain integrity:

- **Normalization**: The `currency` table allows for a centralized reference for currency data, avoiding redundancy.
- **Data Integrity**: By referencing currencies in the `deal` table using foreign keys, we ensure that each deal only refers to valid currencies.
- **Indexing**: an index was created on `deal` table using `deal_unique_id` to let the retrieving faster.

### Initial data:

In the init.sql script, there are some currencies added for testing purposes, if a deal is added with a currency that does not exist in the table, it is added after being validated.

## APIs Available

### 1. **POST /fxdeals/api/deals/addBatch**
   - **Description**: Processes a batch of FX deals from a CSV file.
   - **Input**: A CSV file with the following columns:
     - `dealUniqueId`
     - `fromCurrencyCode`
     - `toCurrencyCode`
     - `dealTimestamp` (format: `yyyy-MM-dd hh:mm:ss`)
     - `dealAmount`
   - **Assumption**: Based on my understanding of the task, I assumed the input would be a CSV file.

### 2. **POST /fxdeals/api/deals/addDeal**
   - **Description**: Processes and stores a single FX deal.
   - **Input**: JSON body with the deal details.

## Unit Tests Available

### 1. `validateAndParseCsvRow_validRow_shouldReturnDeal`
   - **Description**: Tests that a valid CSV row is correctly parsed into a `Deal` object.
   - **Validation**: Ensures that the parsed deal matches the expected values.

### 2. `validateAndParseCsvRow_missingFields_shouldReturnNull`
   - **Description**: Tests that a CSV row with missing fields returns `null`.
   - **Validation**: Ensures the deal is not created when required fields are missing.

### 3. `validateAndParseCsvRow_invalidCurrencyCode_shouldReturnNull`
   - **Description**: Tests that a CSV row with an invalid currency code returns `null`.
   - **Validation**: Ensures that deals with invalid currency codes are rejected.

### 4. `validateAndParseCsvRow_sameFromAndToCurrency_shouldReturnNull`
   - **Description**: Tests that a CSV row with the same currency for both `fromCurrency` and `toCurrency` returns `null`.
   - **Validation**: Ensures that deals where the same currency is used for both buying and selling are rejected.

### 5. `validateAndParseCsvRow_invalidTimestamp_shouldReturnNull`
   - **Description**: Tests that a CSV row with an invalid timestamp returns `null`.
   - **Validation**: Ensures that deals with incorrect timestamp formats are rejected.

### 6. `validateAndParseCsvRow_invalidNumericalAmount_shouldReturnNull`
   - **Description**: Tests that a CSV row with a non-numeric amount returns `null`.
   - **Validation**: Ensures that deals with non-numeric amounts are rejected.

### 7. `validateAndParseCsvRow_nonPositiveAmount_shouldReturnNull`
   - **Description**: Tests that a CSV row with a non-positive amount returns `null`.
   - **Validation**: Ensures that deals with zero or negative amounts are rejected.

### 8. `saveDealsFromCsv_validDeals_shouldSaveAllDeals`
   - **Description**: Tests that a valid batch of deals from a CSV file is correctly saved.
   - **Validation**: Ensures that all valid deals are saved and no errors occur.

### 9. `testValidateCurrencyIsCalled`
   - **Description**: Tests that the `validateCurrency` method in `DealService` correctly calls `CurrencyService` for validation.
   - **Validation**: Ensures that currency validation logic is triggered as expected.

### 10. `saveDealsFromCsv_invalidAndValidDeals_shouldSaveValidDealsOnly`
   - **Description**: Tests that a batch with both valid and invalid deals only saves the valid deals.
   - **Validation**: Ensures that invalid deals are skipped and valid deals are saved.

### 11. `saveDealsFromCsv_dealAlreadyExists_shouldNotSaveDuplicate`
   - **Description**: Tests that a duplicate deal is not saved.
   - **Validation**: Ensures that the system correctly identifies and rejects duplicate deals.

### 12. `saveSingleDeal_invalidDeal_shouldThrowException`
   - **Description**: Tests that an invalid single deal throws an exception and is not saved.
   - **Validation**: Ensures that invalid deals are not saved and the correct exception is thrown.


## How to Run the Code



 **Clone the Repository**:
   ```bash
   git clone https://github.com/alaamigdady/Fxdeals.git
   cd Fxdeals
   mvn clean package
   docker-compose up --build
   
   
## How to Run the APIs
### 1. **POST /fxdeals/api/deals/addBatch**

curl -X POST -F "file=@sample-deals.csv" http://localhost:8080/fxdeals/api/deals/addBatch
- **sample-deals.csv**: 
existed in the root directory


- **Expected Response**: 
{
  "successfulDeals": 2,
  "totalDeals": 2,
  "errors": []
}

### 2. **POST /fxdeals/api/deals/addDeal**
curl -X POST -H "Content-Type: application/json" -d '{
  "dealUniqueId": "deal8",
  "fromCurrency": {"currencyCode": "USD"},
  "toCurrency": {"currencyCode": "EUR"},
  "dealTimestamp": "2024-08-20T12:30:00",
  "dealAmount": 1000.00
}' http://localhost:8080/fxdeals/api/deals/addDeal


## Running the Application Using Makefile

A `Makefile` is included in the project to automate common tasks such as building the project, running Docker Compose, and cleaning up resources.

### Common Makefile Commands

- **Build the Project**:
  ```bash
  make build
 
   
