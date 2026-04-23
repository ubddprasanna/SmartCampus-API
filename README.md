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

## Report – Answers to Conceptual Questions in each Part

---

### Part 1 — Q1: JAX-RS Resource Class Lifecycle

By default, JAX-RS resource classes are **Request-scoped**, meaning a new instance is created for every
incoming HTTP request. Once the response is sent back, that instance is discarded. This is fine for
stateless processing, but it creates a problem for data that needs to persist between requests.

In this project, the `CampusDataStore` is implemented as a **Singleton** — only one instance exists for
the entire lifetime of the application, and every resource class accesses the same shared data through
`CampusDataStore.getInstance()`. To keep things safe when multiple users hit the API at the same time,
we use `ConcurrentHashMap` instead of a regular `HashMap`. This prevents "race conditions" where two
simultaneous requests might try to modify the same room or sensor entry and end up corrupting data.

In short, the per-request lifecycle of JAX-RS resources is fine for handling logic, but shared state
needs to live in a thread-safe singleton so that it survives beyond a single request.

---

### Part 1 — Q2: HATEOAS and Hypermedia

**HATEOAS** (Hypermedia as the Engine of Application State) means that the server doesn't just return
raw data — it also tells the client what it can do next by embedding navigational links in the response.

In this project, the `GET /api/v1` discovery endpoint returns a map of resource names and their URLs
(e.g., `"rooms": "/api/v1/rooms"`). A client that receives this response can follow the links without
needing to know the URL structure in advance. This is better than relying on static documentation because
the links always reflect the current state of the API. If a URL changes on the server, the client picks
up the new link automatically from the response instead of breaking because it had an old URL hardcoded.

It also makes onboarding easier — a new developer can simply call the root endpoint and explore the
API by following links, rather than hunting through external docs that may be outdated.

---

### Part 2 — Q2: Why 409 Conflict Over 400 Bad Request for Room Decommissioning

A `400 Bad Request` means the client sent something the server couldn't even parse — malformed JSON,
missing brackets, wrong data types. The request itself is broken at a syntactic level.

A `409 Conflict`, on the other hand, is used when the request is perfectly valid from a technical
standpoint, but it clashes with the current state of the server. In our case, trying to delete a room
that still has sensors assigned to it isn't a formatting mistake — it's a business rule violation. The
JSON is fine, the URL is correct, but the server can't carry out the deletion because of an existing
dependency.

Using 409 gives the client a much clearer signal: "Your request was well-formed, but the resource
is in a state that prevents this action." That distinction helps clients handle errors more intelligently.

---

### Part 3 — Q2: @QueryParam vs Path-Based Filtering

Path parameters are meant to identify a **specific, unique resource** in the hierarchy — for example,
`/sensors/TEMP-01` points to one particular sensor. They represent the resource's identity.

Query parameters, on the other hand, are designed for **filtering, sorting, or searching** within a
collection — for example, `/sensors?type=CO2` asks for all sensors of a certain type. The filter is
optional; if you drop it, you still get a valid response (all sensors).

Using `@QueryParam` for filtering follows RESTful conventions because it treats the filter as a modifier
on the collection rather than as a separate resource in the URL tree. It also scales much more cleanly —
you can chain multiple filters like `/sensors?type=CO2&status=ACTIVE` without the URL becoming an
unreadable mess like `/sensors/type/CO2/status/ACTIVE`.

---

### Part 4 — Q1: Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern is all about **Separation of Concerns** — keeping each class focused
on one job.

In this project, `SensorResource` handles everything related to sensor CRUD operations, while
`SensorReadingResource` is a dedicated class that deals exclusively with readings. When a request
comes in for `/sensors/{id}/readings`, the locator method in `SensorResource` creates a new instance
of `SensorReadingResource` and passes the `sensorId` into its constructor. From that point, the
sub-resource takes over.

This approach has a few clear advantages. First, it prevents the main `SensorResource` class from
becoming bloated and hard to maintain. Second, the URL structure `/sensors/{id}/readings` naturally
mirrors the domain relationship — a reading belongs to a sensor. Third, if we ever need to add
features like pagination or authentication specifically for readings, we only touch
`SensorReadingResource` without risking side effects in the sensor logic.

---

### Part 5 — Q1: Why 422 Over 404 for Payload Reference Issues

A `404 Not Found` tells the client that the **URL they requested** doesn't exist on the server.
If someone sends a POST to `/api/v1/sensors` with a `roomId` that points to a non-existent room,
the URL itself is perfectly valid and reachable — the endpoint is right there. The problem is inside
the **request body**, not the URL.

A `422 Unprocessable Entity` is the correct choice here. It tells the client: "I understood your
request, the JSON is well-formed, and the endpoint exists — but the data you provided doesn't make
logical sense." Specifically, the `roomId` references a room that isn't in the system.

This distinction matters because a `404` might send the developer on a wild goose chase debugging
their URL, when the actual fix is to correct the `roomId` in the payload.

---

### Part 5 — Q2: Security Risks of Exposing Java Stack Traces

Exposing raw stack traces to API consumers is a well-known security flaw called **Information Exposure**,
and it creates several risks:

**Internal architecture exposure:** Stack traces reveal the full package structure, class names, and
method signatures (e.g., `com.smartcampus.storage.CampusDataStore`). An attacker can use this to map
out how the application is organised internally.

**Library version fingerprinting:** Error messages often include the exact versions of third-party
libraries being used. An attacker can cross-reference these versions against public vulnerability
databases (like CVE lists) to find known exploits that apply.

**Pinpointing failure paths:** Line numbers in stack traces show exactly where the code broke. This
makes it much easier for an attacker to craft specific inputs designed to trigger and exploit those
failure points.

**Business logic leakage:** Exception messages from service layers can accidentally reveal internal
validation rules, data flow patterns, or database schema details that help an attacker understand
and bypass security checks.

In this project, the `GlobalExceptionMapper` intercepts any unhandled `Throwable` and returns a clean,
generic `500 Internal Server Error` with a safe message — no stack traces, no class names, no clues
for attackers.
