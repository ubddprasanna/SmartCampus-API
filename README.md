# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures

**Assignment:** Coursework – REST API Design, Development, and Implementation

**Student (Author):** Dilshan D Prasanna

**Student ID:** 20240194 | W2119857

---

## Table of Contents

1. [API Overview](#api-overview)
2. [Project Structure](#project-structure)
3. [How to Build and Run](#how-to-build-and-run)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Sample Postman Requests](#sample-postman-requests)
6. [Report – Conceptual Questions](#report--conceptual-questions)

---

## API Overview

This project implements a RESTful API for the University's **Smart Campus** initiative.
The system manages physical **Rooms** and the **Sensors** deployed within them. It also maintains a
historical log of **Sensor Readings**.

The API is built using:
- **JAX-RS** (Java API for RESTful Web Services)
- **Jersey 2.32** (JAX-RS implementation)
- **Jackson** (JSON serialisation/deserialisation)
- **Apache Tomcat 9** (Servlet container)
- **Maven** (Dependency and build management)
- **NetBeans** (IDE)

All data is stored **in-memory** using Java `ConcurrentHashMap` and `List` structures.
The API base path is `/api/v1`.

### Core Resources

| Resource | Description |
|---|---|
| Room | A physical campus room with an ID, name, capacity and list of assigned sensor IDs |
| Sensor | An IoT sensor assigned to a room with a type, status and current value |
| SensorReading | A historical reading record with a UUID, epoch timestamp and measured value |

---

## Project Structure

    SmartCampus/
    |   ├── src/main/java/com/smartcampus/
    |   ├── SmartCampusApplication.java     Entry point — @ApplicationPath("/api/v1")
    │   ├── model/
    │   │   ├── Room.java
    │   │   ├── Sensor.java
    │   │   ├── SensorReading.java
    │   │   └── ErrorResponse.java          Standardised JSON error body
    │   ├── resource/
    │   │   ├── DiscoveryResource.java       GET /api/v1
    │   │   ├── SensorRoomResource.java      GET, POST, DELETE /api/v1/rooms
    │   │   ├── SensorResource.java          GET, POST /api/v1/sensors + sub-resource locator
    │   │   └── SensorReadingResource.java   GET, POST /api/v1/sensors/{id}/readings
    │   ├── storage/
    │   │   └── CampusDataStore.java         Singleton in-memory store (ConcurrentHashMaps)
    │   ├── filter/
    │   │   └── LoggingFilter.java           Request/Response logging via @Provider
    │   └── exception/
    │       ├── RoomNotEmptyException.java
    │       ├── RoomNotEmptyMapper.java              409 Conflict
    │       ├── LinkedResourceNotFoundException.java
    │       ├── LinkedResourceNotFoundMapper.java    422 Unprocessable Entity
    │       ├── SensorUnavailableException.java
    │       ├── SensorUnavailableMapper.java         403 Forbidden
    │       └── GlobalExceptionMapper.java           500 Internal Server Error
    └── pom.xml

---

## How to Build and Run

### Prerequisites

- Java JDK 8 or above
- Apache Maven 3.x
- Apache Tomcat 9.x
- NetBeans IDE 18
- Postman (for testing the API)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/ubddprasanna/SmartCampus-API.git
```

### Step 2 — Open in NetBeans

1. Open NetBeans
2. Go to **File → Open Project**
3. Navigate to the cloned `SmartCampus-API` folder and click **Open Project**

### Step 3 — Set Up Apache Tomcat

If you have not already added Tomcat to NetBeans:

1. Go to the **Services** tab
2. Right-click **Servers → Add Server**
3. Select **Apache Tomcat or TomEE**
4. Browse to your extracted Tomcat folder and click **Finish**

### Step 4 — Build the Project

Right-click the project in NetBeans → **Clean and Build**

You should see `BUILD SUCCESS` in the Output window.

### Step 5 — Run the Project

Right-click the project → **Run**

Tomcat will start and deploy the application.

### Step 6 — Access the API

Open Postman or a browser and navigate to:

    http://localhost:8080/SmartCampus/api/v1

You should receive a JSON discovery response confirming the API is running.

---

## API Endpoints Reference

### Discovery

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1` | API metadata and resource links | 200 |

### Rooms

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/rooms` | Get all rooms | 200 |
| POST | `/api/v1/rooms` | Create a new room | 201 |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room | 200 |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors present) | 204 |

### Sensors

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors` | Get all sensors | 200 |
| GET | `/api/v1/sensors?type=CO2` | Get sensors filtered by type | 200 |
| POST | `/api/v1/sensors` | Register a new sensor (validates roomId) | 201 |

### Sensor Readings (Sub-Resource)

| Method | Endpoint | Description | Success Code |
|---|---|---|---|
| GET | `/api/v1/sensors/{sensorId}/readings` | Get full reading history | 200 |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a reading + update currentValue | 201 |

### Error Responses

| HTTP Code | Scenario |
|---|---|
| 400 | Missing or invalid request fields |
| 403 | Posting a reading to a MAINTENANCE sensor |
| 404 | Room or sensor ID not found |
| 409 | Deleting a room that still has sensors |
| 422 | Creating a sensor with a roomId that does not exist |
| 500 | Any unexpected server error |

---

## Sample Postman Requests

> **Note:** Run these in order as some depend on data created by earlier requests.
> Make sure the server is running before testing.

### Request 1 — GET API Discovery

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1` |
| **Headers** | `Accept: application/json` |

**Expected:** `200 OK` with API version, description, and resource links.

---

### Request 2 — POST Create a New Room

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/rooms` |
| **Headers** | `Content-Type: application/json`, `Accept: application/json` |

**Body (raw JSON):**

```json
{
    "id": "ENG-101",
    "name": "Engineering Workshop",
    "capacity": 40
}
```

**Expected:** `201 Created` with the new room object echoed back.

---

### Request 3 — POST Create a New Sensor (linked to ENG-101)

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/sensors` |
| **Headers** | `Content-Type: application/json`, `Accept: application/json` |

**Body (raw JSON):**

```json
{
    "id": "TEMP-002",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "ENG-101"
}
```

**Expected:** `201 Created` with the new sensor object.

---

### Request 4 — POST Add a Reading and Update currentValue

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/sensors/TEMP-002/readings` |
| **Headers** | `Content-Type: application/json`, `Accept: application/json` |

**Body (raw JSON):**

```json
{
    "value": 23.8
}
```

**Expected:** `201 Created` with reading details and `updatedCurrentValue: 23.8`.

---

### Request 5 — DELETE Room That Has Sensors — 409 Error

| Field | Value |
|---|---|
| **Method** | `DELETE` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/rooms/ENG-101` |
| **Headers** | `Accept: application/json` |

**Expected:** `409 Conflict` with JSON error explaining sensors are still assigned.

---

### Request 6 — POST Sensor with Invalid Room ID — 422 Error

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/sensors` |
| **Headers** | `Content-Type: application/json`, `Accept: application/json` |

**Body (raw JSON):**

```json
{
    "id": "HUM-005",
    "type": "Humidity",
    "status": "ACTIVE",
    "currentValue": 0.0,
    "roomId": "FAKE-999"
}
```

**Expected:** `422 Unprocessable Entity` with JSON error explaining the referenced room does not exist.

---

### Request 7 — GET Room That Does Not Exist — 404 Error

| Field | Value |
|---|---|
| **Method** | `GET` |
| **URL** | `http://localhost:8080/SmartCampus/api/v1/rooms/FAKE-999` |
| **Headers** | `Accept: application/json` |

**Expected:** `404 Not Found` with JSON error message.

---

## Report – Conceptual Questions

---

### Part 1: Service Architecture & Setup

---

#### Question 1.1 — JAX-RS Resource Class Lifecycle

> **Question:** In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronise your in-memory data structures (maps/lists) to prevent data loss or race conditions.

By default, JAX-RS resource classes are **request-scoped**, meaning the runtime instantiates a new object for every single incoming HTTP request. Once the response is sent, the instance is discarded.

**Impact on Data:** This decision means that instance variables in a resource class cannot reliably store data; they would be wiped clean for every new request.

**Synchronisation:** To prevent data loss in our Smart Campus system, I implemented the `CampusDataStore` as a **Singleton**. This ensures a single shared instance exists. Since multiple requests (threads) access this one instance, I used `ConcurrentHashMap` to handle synchronisation and prevent race conditions without the performance cost of full-method locking.

---

#### Question 1.2 — HATEOAS and Hypermedia

> **Question:** Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

Hypermedia (HATEOAS) is considered a hallmark of advanced REST because it enables APIs to be **self-descriptive**. Instead of a client knowing all URLs in advance, the server provides navigational links in its responses.

**Benefit to Developers:** This approach decouples the client from the server's URL structure. If the API path changes (e.g., from `/api/v1/rooms` to `/api/v2/campus-rooms`), the client following the links in the discovery endpoint won't break. It significantly reduces the need for external documentation because the API provides a "roadmap" of what actions can be taken next.

---

### Part 2: Room Management

---

#### Question 2.1 — Returning IDs vs Full Objects

> **Question:** When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.

**Returning Only IDs:** This minimises network bandwidth and results in a very small payload. However, it forces the client to issue "N+1" requests — one to retrieve the list and another for each room ID to fetch its details — which increases latency and server load.

**Returning Full Objects:** While this uses more bandwidth per request, it is much more efficient for client-side processing. It allows the client to display a full list of rooms with their names and capacities in a single call, which is generally the preferred approach for modern web and mobile apps to ensure a snappy user experience.

---

#### Question 2.2 — DELETE Idempotency

> **Question:** Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Yes, the DELETE operation in my implementation is **idempotent**.

Because idempotency means that performing an operation multiple times has the same effect on the server's state as performing it once.

**The Scenario:** If a client sends the first DELETE request, the room is removed (`200 OK`). If they mistakenly send it a second or third time, the server returns a `404 Not Found`. Even though the status code changes from 200 to 404, the server's state remains the same: the room is gone.

---

### Part 3: Sensor Operations & Linking

---

#### Question 3.1 — @Consumes and Content-Type Mismatch

> **Question:** We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?

The `@Consumes` annotation acts as a strict guard for the method.

**JAX-RS Handling:** If a client sends data in a different format (like `text/plain` or `application/xml`), the JAX-RS runtime will intercept the request before it even reaches my code and return an **HTTP 415 Unsupported Media Type** error.

**Benefit:** This prevents the logic from parsing incompatible data, thereby improving security and reducing the need for manual format-checking code in resource methods.

---

#### Question 3.2 — @QueryParam vs Path-Based Filtering

> **Question:** You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?

Query parameters (e.g., `/sensors?type=CO2`) are superior for filtering because they treat the filter as an **optional modifier** on a collection, rather than a unique resource.

**Path Parameters:** These are best used to identify a unique resource (e.g., `/sensors/TEMP-001`).

**Query Parameters:** These are much more flexible for searching and sorting. It allows users to easily combine multiple filters (e.g., `?type=CO2&status=ACTIVE`) without creating a confusing, deeply nested URL path that would be hard to maintain and map in code.

---

### Part 4: Deep Nesting

---

#### Question 4.1 — Sub-Resource Locator Pattern

> **Question:** Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?

The Sub-Resource Locator pattern is an architectural win for **separation of concerns**.

**Managing Complexity:** Instead of one massive "God Class" controller handling every possible path, I can delegate logic to dedicated classes like `SensorReadingResource`.

**Modularity:** This makes the code much cleaner and easier to test. For example, `SensorResource` handles the hardware metadata, while `SensorReadingResource` focuses entirely on the historical data logs. It mirrors the physical hierarchy of the "Smart Campus" (Room → Sensor → Reading) in a logical, maintainable way.

---

### Part 5: Exception Mapping & Security

---

#### Question 5.1 — Why HTTP 422 Over 404

> **Question:** Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

An HTTP `404` technically means "URL Not Found," which could mislead a developer into thinking they typed the endpoint wrong.

**Accuracy of 422:** An HTTP `422 Unprocessable Entity` is semantically better when the request is syntactically perfect (valid JSON) and the endpoint is correct, but the business logic is invalid (the referenced `roomId` doesn't exist). It tells the developer: *"Your URL is fine, but the data inside your payload is logically impossible to process."*

---

#### Question 5.2 — Security Risks of Exposing Stack Traces

> **Question:** From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

Exposing stack traces is a significant **information leakage** vulnerability.

**Attacker Intelligence:** An attacker can see internal package structures (e.g., `com.smartcampus.storage`), class names, and specific line numbers where the code failed.

**Fingerprinting:** It reveals the versions of third-party libraries (such as Jersey or Jackson) in use. Attackers can then cross-reference these versions against known vulnerability databases (CVEs) to launch targeted exploits.

---

#### Question 5.3 — JAX-RS Filters for Cross-Cutting Concerns

> **Question:** Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?

Using filters for cross-cutting concerns, such as logging, is much more efficient than manual statements.

**DRY Principle:** Instead of repeating `Logger.info()` in 20 different methods, a single filter captures every request and response automatically.

**Consistency:** It ensures that logging is applied uniformly across the entire API. If we need to change the logging format or add a security header later, we can do so in a single place (the filter) rather than editing every resource class.
