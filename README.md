# E-commerce Order Processing System

A robust, production-ready RESTful API for an E-commerce Order Processing System built with Java and Spring Boot. This project was developed as an assignment to demonstrate clean architecture, concurrent data handling, and RESTful API best practices.

---

## 🚀 Technical Highlights for Reviewers

While this project satisfies standard CRUD requirements, it was engineered with enterprise-level patterns to handle edge cases and scale gracefully:

* **Idempotency Handling:** The `POST /api/orders` endpoint accepts an `idempotencyKey` to safely handle network retries and prevent duplicate order creation.
* **Concurrency Control (Optimistic Locking):** The `Order` entity utilizes `@Version` to prevent race conditions during concurrent status updates, throwing a clean `OptimisticLockingFailureException` rather than corrupting data.
* **Efficient Database Operations:** Instead of fetching and saving in a loop, the background scheduler uses a custom `@Modifying` JPA query to bulk-promote `PENDING` orders to `PROCESSING` efficiently.
* **Strict State Machine:** Order status transitions are strictly guarded. For instance, an order cannot jump directly from `PENDING` to `DELIVERED`, and only `PENDING` orders can be cancelled.
* **Standardized Exception Handling:** A `@ControllerAdvice` global exception handler ensures all client errors (400, 404, 409) return a consistent, readable JSON error contract.

---

## 🛠️ Tech Stack

* **Language:** Java 21
* **Framework:** Spring Boot 4.0.5
* **Database Management:** Spring Data JPA / Hibernate
* **API Documentation:** OpenAPI 3.0 / Swagger UI
* **Utilities:** Lombok, Maven

---

## ⚙️ Core Features

1. **Place an Order:** Create an order with multiple items, automatically calculating subtotals and the total amount.
2. **Retrieve Orders:** Fetch details of a specific order by its ID.
3. **List & Filter Orders:** Retrieve a paginated list of all orders, with an optional filter by order status.
4. **Cancel an Order:** Customers can cancel an order (restricted to `PENDING` status only).
5. **Update Order Status:** Explicit endpoint to transition an order through its lifecycle (`PENDING` -> `PROCESSING` -> `SHIPPED` -> `DELIVERED`).
6. **Automated Order Processing:** A scheduled background job automatically promotes `PENDING` orders to `PROCESSING` every 5 minutes.

---

## 📡 API Endpoints

Once the application is running, you can access the interactive **Swagger UI** for full endpoint documentation and testing at:
`http://localhost:8080/swagger-ui/index.html` (or `http://localhost:8080/swagger-ui.html`)

### Orders
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/orders` | Create a new order (Supports Idempotency Key) |
| `GET` | `/api/orders/{id}` | Retrieve details of a specific order |
| `GET` | `/api/orders` | List all orders (Paginated, supports `?status=PENDING`) |
| `PATCH` | `/api/orders/{id}/status` | Update the status of an order |
| `POST` | `/api/orders/{id}/cancel` | Cancel an order |

---

## 💻 How to Run Locally

### Prerequisites
* Java Development Kit (JDK) 21.
* Maven installed (or use the included Maven wrapper).

### Steps
   ```bash
   git clone https://github.com/tanmaymone/order-processing-system.git
   cd order-processing-system
   mvn clean package
   java -jar target/peerislands_assignment-0.0.1-SNAPSHOT.jar

