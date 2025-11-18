# Request Flow Architecture - Movie Recommendation System

## Overview
This document describes the request flow between the Angular frontend and backend microservices with API Gateway and Load Balancing support.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Angular)                       │
│                      http://localhost:4200                       │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │   Welcome    │  │ Movie List   │  │   Booking    │         │
│  │  Component   │  │  Component   │  │  Component   │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│         │                 │                  │                 │
│         └─────────────────┼──────────────────┘                 │
│                           │                                    │
│                  ┌────────▼────────┐                           │
│                  │  HttpClient     │                            │
│                  │  (Angular)      │                            │
│                  └────────┬────────┘                           │
└───────────────────────────┼────────────────────────────────────┘
                            │
                            │ HTTP Requests (All through Gateway)
                            │
                  ┌─────────▼──────────┐
                  │  Movie Service      │
                  │  (API Gateway)      │
                  │     :8081           │
                  │                     │
                  │  ┌────────────────┐│
                  │  │ Gateway Routes ││
                  │  │ - /movies/**   ││
                  │  │ - /api/recommendations/**││
                  │  │ - /api/ticket-booking/**││
                  │  └────────────────┘│
                  │                     │
                  │  ┌────────────────┐│
                  │  │ Load Balancer   ││
                  │  │ (Round-Robin)   ││
                  │  └────────────────┘│
                  └─────────┬──────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        │ Direct            │ Gateway           │ Gateway
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│ Movie Service │  │Recommendation │  │ Ticket Booking│
│  (Direct)     │  │   Service     │  │   Service     │
│               │  │    :8083      │  │    :8085      │
│               │  │ [Instance 1]  │  │ [Instance 1]  │
└───────┬───────┘  └───────┬───────┘  └───────┬───────┘
        │                  │                  │
        │                  │                  │
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                           │ Internal HTTP Calls
                           │
                  ┌────────▼────────┐
                  │  Movie Service  │
                  │   (Backend)     │
                  └────────┬────────┘
                           │
                           ▼
                  ┌───────────────┐
                  │ MySQL Database│
                  │   :3306       │
                  └───────────────┘
```

## Request Flow Details

### 1. **Welcome Page (Movie Discovery)**

**Component**: `welcome.component.ts`

#### Flow 1.1: Load Featured Movie
```
Frontend (Welcome Component)
    ↓ GET http://localhost:8081/movies
Movie Service (:8081)
    ↓ Query Database
MySQL Database
    ↓ Return Movies
Movie Service
    ↓ JSON Response
Frontend (Displays Featured Movie)
```

#### Flow 1.2: Load Recommendations (Logged-in Users)
```
Frontend (Welcome Component)
    ↓ GET http://localhost:8081/api/recommendations/user/{userId}/hybrid?limit=10
Movie Service (API Gateway) (:8081)
    ↓ RecommendationGatewayController receives request
    ↓ LoadBalancer selects instance (currently single instance)
    ↓ Routes to: GET http://recommendation-service:8083/recommendations/user/{userId}/hybrid?limit=10
Recommendation Service (:8083)
    ↓ GET http://movie-service:8081/movies (fetch all movies)
    ↓ GET http://movie-service:8081/ratings/user/{userId} (fetch user ratings)
    ↓ GET http://movie-service:8081/ratings (fetch all ratings)
Movie Service (:8081)
    ↓ Query Database
MySQL Database
    ↓ Return Data
Movie Service
    ↓ JSON Response
Recommendation Service (Processes with AI algorithms)
    ↓ JSON Response (Recommendations)
Movie Service (API Gateway) - Forwards response
    ↓ JSON Response (Recommendations)
Frontend (Displays Recommendations)
```

**Key Points**:
- **Frontend only communicates with Movie Service** (API Gateway pattern)
- Movie Service routes `/api/recommendations/**` requests to Recommendation Service
- **LoadBalancer** selects available instance (ready for horizontal scaling)
- Recommendation Service acts as a **proxy/aggregator** that calls Movie Service internally
- Uses **WebClient** (Spring WebFlux) for reactive HTTP calls
- Implements **Hybrid Algorithm** (Collaborative + Content-Based Filtering)

---

### 2. **Movie List Component**

**Component**: `movie-list.component.ts`

#### Flow 2.1: Load All Movies
```
Frontend (Movie List Component)
    ↓ GET http://localhost:8081/movies
Movie Service (:8081)
    ↓ Query Database
MySQL Database
    ↓ Return Movies (sorted by movieId DESC, top 30)
Movie Service
    ↓ JSON Response
Frontend (Displays Movie Grid)
```

#### Flow 2.2: Search Movies
```
Frontend (Movie List Component)
    ↓ GET http://localhost:8081/movies/search?query={searchTerm}
Movie Service (:8081)
    ↓ Query Database (LIKE search on title, genre, director)
MySQL Database
    ↓ Return Matching Movies
Movie Service
    ↓ JSON Response
Frontend (Displays Search Results)
```

#### Flow 2.3: Filter by Genre
```
Frontend (Movie List Component)
    ↓ GET http://localhost:8081/movies/genre/{genre}
Movie Service (:8081)
    ↓ Query Database (WHERE genre = {genre})
MySQL Database
    ↓ Return Filtered Movies
Movie Service
    ↓ JSON Response
Frontend (Displays Filtered Results)
```

#### Flow 2.4: Submit Movie Rating
```
Frontend (Movie List Component)
    ↓ POST http://localhost:8081/ratings
    │ Body: { userId, movieId, rating }
    │ Headers: Authorization: Bearer {token}
Movie Service (:8081)
    ↓ Insert/Update Rating in Database
MySQL Database
    ↓ Save Rating
Movie Service
    ↓ JSON Response (Success)
Frontend
    ↓ Dispatch Custom Event: 'ratingSubmitted'
Welcome Component (Listens for event)
    ↓ Reload Recommendations
```

---

### 3. **Booking Flow**

**Component**: `booking.component.ts`  
**Service**: `booking.service.ts`

#### Flow 3.1: Load Movie Details
```
Frontend (Booking Component)
    ↓ GET http://localhost:8081/movies/{movieId}
Movie Service (:8081)
    ↓ Query Database
MySQL Database
    ↓ Return Movie Details
Movie Service
    ↓ JSON Response
Frontend (Displays Movie Info)
```

#### Flow 3.2: Load Theaters & Showtimes
```
Frontend (Booking Component)
    ↓ GET http://localhost:8081/theaters
    ↓ GET http://localhost:8081/theater-movies?movieId={movieId}
Movie Service (:8081)
    ↓ Query Database
MySQL Database
    ↓ Return Theaters & Showtimes
Movie Service
    ↓ JSON Response
Frontend (Displays Theater Selection)
```

#### Flow 3.3: Create Booking
```
Frontend (Booking Component)
    ↓ POST http://localhost:8081/bookings
    │ Body: {
    │   userId, theaterMovieId, numberOfSeats,
    │   pricePerTicket, discountCode
    │ }
    │ Headers: Authorization: Bearer {token}
Movie Service (:8081)
    ↓ Validate Request
    ↓ Insert Booking in Database
MySQL Database
    ↓ Save Booking
Movie Service
    ↓ JSON Response (Booking Details)
Frontend (Displays Confirmation)
```

#### Flow 3.4: Ticket Booking Operations (via Gateway)
```
Frontend (Booking Component)
    ↓ GET/POST http://localhost:8081/api/ticket-booking/...
    │ Example: GET /api/ticket-booking/seats/{theaterMovieId}
Movie Service (API Gateway) (:8081)
    ↓ TicketBookingGatewayController receives request
    ↓ LoadBalancer selects instance (currently single instance)
    ↓ Routes to: GET http://ticket-booking-service:8085/seats/{theaterMovieId}
Ticket Booking Service (:8085)
    ↓ Process Request (seat selection, ticket generation, etc.)
    ↓ JSON Response
Movie Service (API Gateway) - Forwards response
    ↓ JSON Response
Frontend (Displays Ticket Information)
```

---

### 4. **User Authentication Flow**

**Component**: `sign-in-modal.component.ts`

#### Flow 4.1: User Login
```
Frontend (Sign In Component)
    ↓ POST http://localhost:8081/auth/login
    │ Body: { email, password }
Movie Service (:8081)
    ↓ Validate Credentials
    ↓ Query Database
MySQL Database
    ↓ Return User Data
Movie Service
    ↓ Generate JWT Token
    ↓ JSON Response: { token, user }
Frontend
    ↓ Store in localStorage:
    │   - authToken
    │   - user (JSON)
    ↓ Update UI (Show Logged-in State)
```

#### Flow 4.2: User Registration
```
Frontend (Registration Component)
    ↓ POST http://localhost:8081/user
    │ Body: { userID, name, email, password, ... }
Movie Service (:8081)
    ↓ Validate Data
    ↓ Insert User in Database
MySQL Database
    ↓ Save User
Movie Service
    ↓ JSON Response (User Created)
Frontend (Show Success Message)
```

---

## Service-to-Service Communication

### Frontend → Movie Service (API Gateway) → Recommendation Service

**Gateway Routing Pattern with Load Balancing**:
```
Frontend (:4200)
    ↓ GET http://localhost:8081/api/recommendations/user/4/hybrid?limit=10
Movie Service Gateway (:8081)
    ↓ RecommendationGatewayController receives request
    ↓ LoadBalancer selects instance from available instances
    ↓ Routes request to Recommendation Service
    ↓ GET http://recommendation-service:8083/recommendations/user/4/hybrid?limit=10
Recommendation Service (:8083)
    ↓ Processes request and returns recommendations
Movie Service Gateway (:8081)
    ↓ Forwards response to Frontend
Frontend (:4200)
    ↓ Receives recommendations
```

### Frontend → Movie Service (API Gateway) → Ticket Booking Service

**Gateway Routing Pattern with Load Balancing**:
```
Frontend (:4200)
    ↓ GET http://localhost:8081/api/ticket-booking/seats/{theaterMovieId}
Movie Service Gateway (:8081)
    ↓ TicketBookingGatewayController receives request
    ↓ LoadBalancer selects instance from available instances
    ↓ Routes request to Ticket Booking Service
    ↓ GET http://ticket-booking-service:8085/seats/{theaterMovieId}
Ticket Booking Service (:8085)
    ↓ Processes request and returns seat information
Movie Service Gateway (:8081)
    ↓ Forwards response to Frontend
Frontend (:4200)
    ↓ Receives seat information
```

### Recommendation Service → Movie Service

**Internal Network Communication** (Docker Network):
```
Recommendation Service (:8083)
    ↓ GET http://movie-service:8081/movies
    ↓ GET http://movie-service:8081/ratings/user/{userId}
    ↓ GET http://movie-service:8081/ratings
Movie Service (:8081)
    ↓ Process Request
    ↓ Query Database
MySQL Database
    ↓ Return Data
Movie Service
    ↓ Return JSON
Recommendation Service
    ↓ Process with Algorithms
    ↓ Return Recommendations
```

**Key Points**:
- **Frontend never directly calls Recommendation Service or Ticket Booking Service** - all requests go through Movie Service Gateway
- Movie Service Gateway routes:
  - `/api/recommendations/**` → Recommendation Service (via LoadBalancer)
  - `/api/ticket-booking/**` → Ticket Booking Service (via LoadBalancer)
- **LoadBalancer** automatically selects available instances (currently single instance, ready for scaling)
- Uses Docker service names (`movie-service`, `recommendation-service`, `ticket-booking-service`) for internal communication
- Gateway pattern abstracts microservices from the client

---

## API Gateway Pattern

### Gateway Routing Rules

**Movie Service acts as API Gateway** for all client requests:

| Frontend Request | Gateway Route | Target Service | Access Pattern | Load Balancing |
|-----------------|---------------|----------------|----------------|----------------|
| `/movies/**` | Direct (Movie Service) | Movie Service | Direct | N/A |
| `/ratings/**` | Direct (Movie Service) | Movie Service | Direct | N/A |
| `/bookings/**` | Direct (Movie Service) | Movie Service | Direct | N/A |
| `/theaters/**` | Direct (Movie Service) | Movie Service | Direct | N/A |
| `/api/recommendations/**` | Routes to Recommendation Service | Recommendation Service (:8083) | Via Gateway | ✅ Enabled |
| `/api/ticket-booking/**` | Routes to Ticket Booking Service | Ticket Booking Service (:8085) | Via Gateway | ✅ Enabled |
| `/api/payments/**` | Routes to Payment Service | Payment Service (:8082) | Via Gateway | Ready |
| `/api/users/**` | Routes to User Service | User Service (:8084) | Via Gateway | Ready |

### Load Balancing Implementation

**Current Configuration**:
- **LoadBalancer**: Spring Cloud LoadBalancer (Round-Robin algorithm)
- **Service Discovery**: Manual configuration via `ServiceInstanceListSupplier`
- **Current Instances**: Single instance per service (ready for horizontal scaling)
- **Health Checks**: Can be added for automatic failover

**How It Works**:
1. Gateway Controller receives request
2. LoadBalancer selects instance from configured list
3. Request routed to selected instance
4. Response forwarded back to client

**Scaling**:
- To add more instances, update `LoadBalancerConfig.java` with additional `DefaultServiceInstance`
- LoadBalancer automatically distributes requests across all instances
- No code changes needed in Gateway Controllers

### Gateway Benefits

1. **Single Entry Point**: Frontend only needs to know one URL (`http://localhost:8081`)
2. **Centralized Authentication**: All requests validated at gateway
3. **Service Abstraction**: Internal service structure hidden from clients
4. **Load Balancing**: Distributes traffic across service instances
5. **Monitoring**: Centralized logging and metrics
6. **Security**: Single point for CORS, rate limiting, etc.
7. **Scalability**: Easy horizontal scaling without frontend changes

### Gateway Implementation

**Movie Service Gateway Controllers**:
- `RecommendationGatewayController` - Routes `/api/recommendations/**`
- `TicketBookingGatewayController` - Routes `/api/ticket-booking/**`
- Uses `WebClient` (Spring WebFlux) with `@LoadBalanced` for reactive HTTP calls
- LoadBalancer automatically selects instance from configured list
- Forwards requests to target services on Docker network
- Returns responses transparently to frontend

---

## Port Mapping

| Service | Internal Port | External Port | Purpose | Access Pattern | Load Balancing |
|---------|--------------|---------------|---------|----------------|----------------|
| Frontend | 80 (nginx) | 4200 | Angular App | Direct | N/A |
| Movie Service | 8081 | 8081 | **API Gateway + Main API** | **Gateway for all services** | N/A |
| Payment Service | 8082 | 8082 | Payment Processing | Via Gateway (`/api/payments/**`) | Ready |
| Recommendation Service | 8083 | 8083 | AI Recommendations | Via Gateway (`/api/recommendations/**`) | ✅ Enabled |
| User Service | 8084 | 8084 | User Management | Via Gateway (`/api/users/**`) | Ready |
| Ticket Booking Service | 8085 | 8085 | Booking Management | Via Gateway (`/api/ticket-booking/**`) | ✅ Enabled |
| MySQL | 3306 | 3306 | Database | Internal only | N/A |
| Config Service | 8888 | 8888 | Configuration Server | Internal only | N/A |

**Note**: External ports for Payment, Recommendation, User, and Ticket Booking services are optional - they can be removed from Docker port mappings since clients access them through the Movie Service Gateway.

---

## CORS Configuration

**Frontend Origin**: `http://localhost:4200`

**Backend Services** allow requests from:
- `http://localhost:*`
- `http://127.0.0.1:*`

**Configuration Files**:
- `MovieService/src/main/java/com/spring5/movieservice/common/WebConfig.java` (if exists)
- Gateway handles CORS for all routed services

---

## Data Flow Examples

### Example 1: User Views Recommendations

1. **User logs in** → Frontend stores `userId` in localStorage
2. **Welcome page loads** → Checks `isLoggedIn` flag
3. **Frontend calls**: `GET http://localhost:8081/api/recommendations/user/4/hybrid?limit=10`
4. **Movie Service Gateway** receives request at `/api/recommendations/**`
5. **LoadBalancer** selects instance (currently single instance: `recommendation-service-1`)
6. **Movie Service Gateway** routes to: `GET http://recommendation-service:8083/recommendations/user/4/hybrid?limit=10`
7. **Recommendation Service**:
   - Fetches all movies from Movie Service
   - Fetches user's ratings from Movie Service
   - Fetches all ratings from Movie Service
   - Runs Collaborative Filtering algorithm
   - Runs Content-Based Filtering algorithm
   - Combines results (Hybrid)
   - Returns top 10 recommendations
8. **Movie Service Gateway** forwards response to Frontend
9. **Frontend displays** recommendations sorted by score

### Example 2: User Rates a Movie

1. **User clicks "Rate Movie"** → Opens rating modal
2. **User selects rating** (1-5 stars)
3. **Frontend calls**: `POST /ratings` with `{ userId, movieId, rating }`
4. **Movie Service** saves rating to database
5. **Frontend dispatches** `ratingSubmitted` event
6. **Welcome Component** listens for event
7. **Welcome Component** reloads recommendations (after 500ms delay)
8. **Updated recommendations** appear in UI

### Example 3: User Books a Movie

1. **User clicks "Book Now"** → Navigates to `/book?movieId=123`
2. **Booking Component** loads:
   - Movie details: `GET /movies/123`
   - Theaters: `GET /theaters`
   - Showtimes: `GET /theater-movies?movieId=123`
3. **User selects** theater, showtime, seats
4. **Frontend calculates** price breakdown (client-side)
5. **User confirms** → `POST /bookings` with booking details
6. **Movie Service** creates booking
7. **Frontend displays** confirmation

### Example 4: User Books Tickets (via Gateway)

1. **User clicks "Book Now"** → Navigates to `/book?movieId=123`
2. **Booking Component** loads:
   - Movie details: `GET http://localhost:8081/movies/123` (direct)
   - Theaters: `GET http://localhost:8081/theaters` (direct)
   - Showtimes: `GET http://localhost:8081/theater-movies?movieId=123` (direct)
3. **User selects** theater, showtime
4. **Frontend calls**: `GET http://localhost:8081/api/ticket-booking/seats/{theaterMovieId}` (via gateway)
5. **Movie Service Gateway** receives request
6. **LoadBalancer** selects instance (currently single instance: `ticket-booking-service-1`)
7. **Ticket Booking Service** returns available seats
8. **User selects seats** and confirms booking
9. **Frontend calls**: `POST http://localhost:8081/bookings` (direct to Movie Service)
10. **Movie Service** creates booking
11. **Frontend displays** confirmation

---

## Error Handling

### Frontend Error Handling
- **HTTP Errors**: Displayed in UI with error messages
- **Network Errors**: "Failed to load movies. Make sure the backend is running."
- **CORS Errors**: Logged to console, silently fail for recommendations

### Backend Error Handling
- **404 Not Found**: Returns appropriate error message
- **400 Bad Request**: Validation errors returned
- **500 Internal Server Error**: Logged, generic error returned
- **Service Unavailable**: Recommendation Service falls back to popular movies
- **LoadBalancer Failover**: If instance fails, LoadBalancer can retry on next instance (when multiple instances configured)

---

## Key Technologies

- **Frontend**: Angular 17+ (Standalone Components), HttpClient, RxJS
- **Backend**: Spring Boot, Spring MVC, Spring WebFlux (WebClient)
- **Load Balancing**: Spring Cloud LoadBalancer (Round-Robin)
- **Database**: MySQL 8.0
- **Containerization**: Docker, Docker Compose
- **Networking**: Docker Bridge Network (`movie-booking-network`)

---

## Security Considerations

1. **Authentication**: JWT tokens stored in localStorage
2. **Authorization**: Bearer token sent in `Authorization` header
3. **CORS**: Configured to allow only localhost origins
4. **SQL Injection**: Prevented by using parameterized queries (Spring Data)
5. **XSS**: Angular automatically escapes HTML
6. **Gateway Security**: Single point for security policies

---

## Performance Optimizations

1. **Pagination**: Movies limited to top 30 newest
2. **Caching**: Recommendations cached (can be implemented)
3. **Lazy Loading**: Components loaded on demand
4. **Reactive Programming**: WebFlux for non-blocking I/O
5. **Database Indexing**: On `movieId`, `userId`, `genre` columns
6. **Load Balancing**: Distributes load across instances

---

## Future Enhancements

1. ~~**API Gateway**: Add Spring Cloud Gateway for routing~~ ✅ **Implemented**: Movie Service acts as API Gateway
2. **Service Discovery**: Implement Eureka or Consul for dynamic service discovery
3. ~~**Load Balancing**: Multiple instances of services~~ ✅ **Implemented**: Spring Cloud LoadBalancer configured (single instance ready for scaling)
4. **Caching Layer**: Redis for frequently accessed data
5. **Message Queue**: RabbitMQ/Kafka for async processing
6. **Monitoring**: Prometheus + Grafana for metrics
7. **Health Checks**: Add health check-based instance selection
8. **Circuit Breaker**: Implement resilience patterns (Resilience4j)
