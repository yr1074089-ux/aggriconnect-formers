Live Demo:https://aggriconnect-formers.onrender.com

# AgriConnect — Farmer-Customer E-commerce Platform (Spring Boot)

A secure, role-based, full-stack agricultural supply-chain marketplace connecting
**Farmers**, **Customers**, and **Delivery Partners** — REST API backend plus a
complete, styled web frontend, all served from a single Spring Boot application.

## Frontend

A full browser UI ships in `src/main/resources/static/`, served directly by Spring
Boot at `http://localhost:8080/` — no separate frontend server or build step needed.

| Page                        | Purpose                                                        |
|------------------------------|------------------------------------------------------------------|
| `index.html`                 | Marketing landing page (hero, role overview, AI tools, order flow) |
| `login.html` / `register.html` | Authentication, with a role picker on signup                  |
| `farmer-dashboard.html`      | Product CRUD, AI description/price tools, orders, earnings      |
| `customer-dashboard.html`    | Browse/search, cart & checkout, order history with live tracking |
| `delivery-dashboard.html`    | Accept tasks, advance delivery status, log location              |
| `admin-dashboard.html`       | View all users and all orders                                    |

It's vanilla HTML/CSS/JS (no build tooling required) that talks to the REST API
under `/api/**` using a JWT stored in `localStorage` (see `js/common.js`).

## Key Features

- **JWT authentication** with role-based access control (Farmer / Customer / Delivery Partner / Admin)
- **Farmer portal**
  - Add/update/delete/enable-disable products
  - **AI-powered tools**: auto-generate product descriptions, AI price suggestions based on
    category/quality/quantity, and automatic product categorization
  - View orders containing their products
  - Track earnings via full transaction history
- **Customer portal**
  - Browse/search products (public storefront + authenticated views)
  - Place multi-item orders (stock is validated and decremented atomically)
  - View order history
- **Delivery partner portal**
  - View unassigned delivery tasks and self-assign, or be assigned by an admin
  - Real-time delivery lifecycle updates: `ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED`
  - Update current location at each step
  - On delivery, farmer earnings are automatically recorded as transactions
- **Admin**: view all users and orders

## Tech Stack

- Java 17, Spring Boot 3.2.5
- Spring Web, Spring Data JPA, Spring Security (JWT via `jjwt`)
- MySQL database (default, seeded with demo data) — H2 in-memory connector still included for quick local demos
- Lombok, Bean Validation

## Project Structure

```
src/main/java/com/agriconnect/
  config/        SecurityConfig, DataSeeder (demo data)
  security/      JwtUtil, JwtAuthFilter, CustomUserDetails(Service)
  entity/        User, Product, Order, OrderItem, Delivery, Transaction (+ enums)
  repository/    Spring Data JPA repositories
  service/       AuthService, ProductService, OrderService, DeliveryService,
                 TransactionService, UserService, AIService (AI-powered tools)
  controller/    AuthController, FarmerController, CustomerController,
                 DeliveryController, AdminController, PublicProductController
  dto/           Request/response payloads
  exception/     Custom exceptions + GlobalExceptionHandler
```

## Running the project

```bash
mvn spring-boot:run
```

Before starting, make sure MySQL is running and update the credentials in
`src/main/resources/application.properties` if needed (default: `root` / `yourpassword`,
database `agriconnect_db`, created automatically on first run via
`createDatabaseIfNotExist=true`).

Then open **`http://localhost:8080`** in a browser — that's the landing page, with
login/register and all three role dashboards reachable from there. The app connects
to MySQL and seeds demo accounts automatically (see console output on startup).

### Demo accounts (password for all: `Passw0rd!`)

| Role             | Email              |
|------------------|--------------------|
| Farmer           | farmer@demo.com    |
| Customer         | customer@demo.com  |
| Delivery Partner | delivery@demo.com  |
| Admin            | admin@demo.com     |

### Switching back to H2 (quick local demo, no DB setup needed)

Edit `src/main/resources/application.properties`: comment out the MySQL block and
uncomment the H2 block.

## API Overview

All protected endpoints require header: `Authorization: Bearer <token>`

### Auth
| Method | Endpoint             | Description                  |
|--------|-----------------------|-------------------------------|
| POST   | `/api/auth/register`  | Register (role: FARMER/CUSTOMER/DELIVERY_PARTNER/ADMIN) |
| POST   | `/api/auth/login`     | Login, returns JWT           |

### Farmer (`ROLE_FARMER`)
| Method | Endpoint                                   | Description                       |
|--------|---------------------------------------------|------------------------------------|
| POST   | `/api/farmer/products`                      | Add product (optionally AI-assisted) |
| PUT    | `/api/farmer/products/{id}`                 | Update product                    |
| DELETE | `/api/farmer/products/{id}`                 | Delete product                    |
| PATCH  | `/api/farmer/products/{id}/availability`    | Toggle availability               |
| GET    | `/api/farmer/products`                      | List own products                 |
| POST   | `/api/farmer/ai/generate-description`       | AI: generate product description  |
| POST   | `/api/farmer/ai/suggest-price`               | AI: suggest a fair price          |
| GET    | `/api/farmer/ai/auto-categorize`            | AI: auto-detect category          |
| GET    | `/api/farmer/orders`                        | Orders containing this farmer's products |
| GET    | `/api/farmer/earnings`                      | Full transaction/earnings history |
| GET    | `/api/farmer/earnings/total`                | Total earnings                    |

### Customer (`ROLE_CUSTOMER`)
| Method | Endpoint                        | Description               |
|--------|-----------------------------------|----------------------------|
| GET    | `/api/customer/products`          | Browse/search products     |
| GET    | `/api/customer/products/{id}`     | Product detail              |
| POST   | `/api/customer/orders`            | Place an order              |
| GET    | `/api/customer/orders`            | My orders                   |
| GET    | `/api/customer/orders/{id}`       | Order detail                |

### Public storefront (no auth)
| Method | Endpoint                    | Description        |
|--------|------------------------------|---------------------|
| GET    | `/api/products/browse`       | Browse available products |

### Delivery Partner (`ROLE_DELIVERY_PARTNER`)
| Method | Endpoint                                   | Description                |
|--------|---------------------------------------------|------------------------------|
| GET    | `/api/delivery/tasks/available`             | Unassigned delivery tasks    |
| GET    | `/api/delivery/tasks/mine`                  | My assigned tasks            |
| POST   | `/api/delivery/tasks/{id}/accept`           | Self-assign a task           |
| PATCH  | `/api/delivery/tasks/{id}/status`           | Update status/location       |
| GET    | `/api/delivery/order/{orderId}/track`       | Track a delivery by order    |

### Admin (`ROLE_ADMIN`)
| Method | Endpoint                  | Description        |
|--------|-----------------------------|---------------------|
| GET    | `/api/admin/users`          | All users            |
| GET    | `/api/admin/users/role/{role}` | Users by role      |
| GET    | `/api/admin/orders`         | All orders            |

## Notes on the AI-powered tools

`AIService` currently ships as a deterministic, rule/template-based engine so the
project compiles and runs fully offline with no API keys required. It is written so
that the description-generation and price-suggestion methods can be swapped for real
calls to an LLM provider (e.g. the Anthropic Messages API) without touching any
controller or caller code — only the method bodies in `AIService` need to change.

## Suggested next steps

- Add Swagger/OpenAPI (springdoc-openapi) for interactive API docs
- Add pagination/filtering to product & order listings
- Add refresh tokens and password-reset flow
- Add payment gateway integration (Razorpay/Stripe) before order confirmation
- Add a review/rating system for products and delivery partners
- Containerize with Docker + docker-compose (app + MySQL)
