# WashQ - Online Laundry Reservation System

A full-stack web application for online laundry slot reservations, built with Spring Boot and React.

## Project Description

WashQ is an online laundry reservation system designed for university students and everyday residents. The platform allows users to register, log in, and eventually book laundry time slots through a clean, modern interface.

Phase 1 implements the core authentication layer: secure user registration and login with JWT-based stateless authentication. Passwords are hashed using BCrypt before storage. The frontend is a responsive React SPA that communicates with the Spring Boot REST API, with protected routes that redirect unauthenticated users to the login page.

## Technologies Used

### Backend
- **Spring Boot 3** - Java-based REST API framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database interaction and ORM
- **PostgreSQL (Supabase)** - Cloud-hosted relational database
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **BCrypt** - Password hashing (12 salt rounds)
- **Maven** - Dependency management and build tool

### Frontend
- **React 19** - JavaScript library for building user interfaces
- **Vite 7** - Build tool and development server
- **Tailwind CSS 3** - Utility-first CSS framework
- **Axios** - HTTP client for API requests
- **React Router DOM 7** - Client-side routing

### Development Tools
- **Visual Studio Code** - Code editor
- **Postman** - API testing
- **Git / GitHub** - Version control

## Prerequisites

Before running this application, ensure you have the following installed:

- **Java Development Kit (JDK) 17 or higher**
- **Maven 3.6 or higher**
- **Node.js 18 or higher**
- **npm 9 or higher**
- **Git**

## Project Structure

```
IT342-Abel-WashQ/
├── backend/              # Spring Boot application
│   └── src/main/java/edu/cit/abel/washq/
│       ├── controller/   # REST controllers (AuthController)
│       ├── dto/          # Data transfer objects
│       ├── entity/       # JPA entities (User)
│       ├── exception/    # Custom exceptions and global handler
│       ├── repository/   # Spring Data repositories
│       ├── security/     # JWT, filters, SecurityConfig
│       └── service/      # Business logic (AuthService)
├── web/                  # React frontend application
│   └── src/
│       ├── api/          # Axios instance and API service functions
│       ├── components/   # Reusable components (ProtectedRoute)
│       ├── pages/        # Page components (Login, Register, Dashboard)
│       └── routes/       # Centralized route definitions
├── docs/                 # Documentation
└── README.md             # This file
```

## Steps to Run Backend

### 1. Configure Environment Variables

Create a `.env` file in the `backend/` directory with the following variables:

```
DB_URL=jdbc:postgresql://<your-supabase-host>:5432/postgres
DB_USERNAME=<your-db-username>
DB_PASSWORD=<your-db-password>
JWT_SECRET=<your-jwt-secret-key>
```

### 2. Run the Spring Boot Application

From the backend directory:

```bash
cd backend
./mvnw spring-boot:run
```

The backend server will start on `http://localhost:8080`.

### 3. Verify Backend is Running

You should see console output ending with:

```
Started WashqApplication in X.XXX seconds
```

## Steps to Run Web App

### 1. Install Dependencies

Navigate to the web directory and install npm packages:

```bash
cd web
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

The web application will start on `http://localhost:5173`.

### 3. Access the Application

Open your browser and navigate to `http://localhost:5173`. You will see the login page.

### Additional Frontend Commands

- **Build for production:** `npm run build`
- **Preview production build:** `npm run preview`
- **Run linter:** `npm run lint`

## API Endpoints

### Register User

```
POST /auth/register
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123",
  "address": "123 Main St",
  "contactNumber": "+639123456789"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "CUSTOMER"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  },
  "timestamp": "2026-03-07T12:00:00"
}
```

### Login User

```
POST /auth/login
```

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "role": "CUSTOMER"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiJ9..."
  },
  "timestamp": "2026-03-07T12:00:00"
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "error": {
    "code": "AUTH-001",
    "message": "Invalid email or password."
  },
  "timestamp": "2026-03-07T12:00:00"
}
```

### Get Current User (Protected)

```
GET /auth/me
```

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  },
  "timestamp": "2026-03-07T12:00:00"
}
```

## Configuration

### Backend

Configuration is loaded from `backend/src/main/resources/application.properties` and a `.env` file:

- **Database:** PostgreSQL via Supabase (session pooler)
- **JWT Expiration:** 24 hours (86400000 ms)
- **Password Hashing:** BCrypt with 12 salt rounds

### Frontend

The frontend connects to the backend at `http://localhost:8080`, configured in `src/api/axios.js`.

### CORS

The backend allows cross-origin requests from `http://localhost:5173` (Vite dev server) with support for GET, POST, PUT, DELETE, and OPTIONS methods.

## Security Features

- **Password Encryption** -- All passwords are hashed using BCrypt before storage
- **JWT Authentication** -- Stateless authentication using signed JSON Web Tokens
- **Protected Routes** -- Frontend routes guarded by token presence check with redirect to login
- **CORS Protection** -- Configured to only allow requests from the frontend origin
- **Input Validation** -- Server-side validation via Jakarta Bean Validation; client-side validation on blur
- **Stateless Sessions** -- No server-side session; all state carried in the JWT

## Testing Checklist

- User Registration
  - Valid registration with all six fields
  - Duplicate email returns error message
  - Missing or invalid fields show inline validation errors
  - Successful registration redirects to login page

- User Login
  - Valid credentials redirect to dashboard
  - Invalid credentials show AUTH-001 error banner
  - JWT token and user data stored in localStorage

- Protected Routes
  - Dashboard accessible with valid token
  - Dashboard redirects to login without token
  - Sign-out clears token and redirects to login

## Future Enhancements

- Google OAuth integration
- Laundry slot booking and queue management
- Staff and admin dashboards
- Real-time order status tracking
- Mobile application
- Email verification and password reset
- Refresh token mechanism

## License

This project is developed for educational purposes as part of IT342 coursework.