# Kaizo - Zendesk Ticket Polling Service

A Scala-based microservice that polls Zendesk tickets incrementally for multiple customers using a fair scheduling algorithm.

## Overview

Kaizo is a polling engine that manages Zendesk API integrations for multiple customers. It ensures fair resource distribution by scheduling API calls in a round-robin fashion while respecting Zendesk's rate limits (10 requests per minute).

### Key Features

- **Fair Scheduling**: Customers are queued and polled fairly, preventing any single customer from monopolizing API resources
- **Incremental Polling**: Uses Zendesk's incremental API to fetch only new/updated tickets since the last poll
- **Error Tracking**: Logs all API failures to a database for monitoring and debugging
- **Concurrent Processing**: Processes multiple customers in parallel (configurable concurrency level)
- **REST API**: Provides HTTP endpoints to register new customers and retrieve their information
- **Persistent State**: Stores customer data and cursors in DuckDB for reliability across restarts

## Architecture

The application is built using:
- **Cats Effect** for functional effect management
- **FS2** for streaming data processing
- **Http4s** with Ember for HTTP server and client
- **Doobie** for database access
- **DuckDB** as the embedded database
- **Circe** for JSON encoding/decoding

## Database Schema

### Customers Table
- `id`: Unique customer identifier
- `domain`: Zendesk domain URL
- `token`: Bearer token for API authentication
- `queue_at`: Next scheduled polling time
- `start_from`: Cursor for incremental polling (last fetched timestamp)
- `created_at`, `updated_at`: Audit timestamps

### Errors Table
- `customer_id`: Reference to the customer
- `message`: Error message from failed API calls
- `createdAt`: When the error occurred

## API Endpoints

### Register a Customer
```bash
curl -X POST http://localhost:8080/customers \
  -H "Content-Type: application/json" \
  -d '{
    "domain": "https://d3v-kaizo-dev.zendesk.com",
    "token": "b2a1e1c461dd0b2dfb3317751e2ffb4ea165900001b9c3464f704eca75878eec",
    "startFrom": "2025-10-08T22:11:40.958432+02:00"
  }'
```

**Parameters**:
- `domain` (required): Your Zendesk domain URL
- `token` (required): Bearer token for API authentication
- `startFrom` (optional): ISO 8601 timestamp with timezone offset to start polling from. If omitted, defaults to one minute ago.

**Response**: Returns the created customer record with a scheduled `queue_at` time.

### Get Customer by ID
```bash
curl http://localhost:8080/customers/2
```

**Response**: Returns the customer record including their current polling cursor.

**Example Response**:
```json
{
  "id": 2,
  "domain": "https://d3v-kaizo-dev.zendesk.com",
  "token": "b2a1e1c461dd0b2dfb3317751e2ffb4ea165900001b9c3464f704eca75878eec",
  "queueAt": "2025-10-09T17:36:45.533316+02:00",
  "startFrom": "2025-10-09T10:01:07+02:00"
}
```

## Configuration

Current configuration values (hardcoded, marked with TODO for externalization):

- **Server Host**: `0.0.0.0`
- **Server Port**: `8080`
- **Polling Interval**: Every 6 seconds (checks for customers ready to poll)
- **Concurrency Level**: 10 parallel customer polls
- **Per Page**: 100 ticket per API request

## How It Works

1. **Customer Registration**: Customers are added via the REST API with their Zendesk domain and authentication token
2. **Scheduling**: Each customer is assigned a `queue_at` timestamp, spaced a few seconds apart to respect rate limits
3. **Polling Loop**: Every 6 seconds, the engine checks for customers whose `queue_at` time has passed
4. **API Calls**: Selected customers are processed concurrently, fetching tickets from Zendesk's incremental API
5. **Cursor Update**: After successful polling, each customer's `start_from` cursor is updated to the last ticket timestamp
6. **Rescheduling**: Customers are rescheduled for their next poll window
7. **Error Handling**: Failed API calls are logged to the errors table, but don't block other customers

## Running the Application

```bash
sbt run
```

The application will:
- Initialize the DuckDB database (`data.db`)
- Create necessary tables and indexes
- Start the HTTP server on port 8080
- Begin the polling engine

## Data Flow

```
HTTP Request → Customer Registration → Database
                                          ↓
Polling Engine ← Database ← Scheduler (every 3s)
      ↓
Zendesk API (parallel requests)
      ↓
Tickets → Console Output
      ↓
Update Cursor → Database
```

## Cross-Cutting Concerns

### Observability

**Current State**: Basic `IO.println` logging

**Needs Improvement**: Structured logging, metrics, tracing

### Configuration

**Current State**: Hardcoded values with TODO comments

**Needs Improvement**: External configuration files, environment variables

### Testing

**Current State**: No unit tests included in this project

**Rationale for Omission**: The decision to exclude unit tests was made pragmatically based on project constraints. Writing comprehensive unit tests for this codebase would have extended the development timeline significantly beyond the specified project deadline. The primary value of unit tests lies in preventing regressions and reducing stress when making future modifications or adjustments to the codebase. However, given that this is a one-off project with no anticipated future contributions or maintenance, the cost-benefit analysis favored delivering working functionality within the time constraint over building a test suite that would never be utilized. In a production system with ongoing development, unit tests would be essential, but for a time-bounded proof-of-concept or demo project, the investment cannot be justified.

**If Tests Were Added**: Unit tests, integration tests, property-based tests for scheduling logic

### Deployment

**Current State**: Single-instance design

**Needs Improvement**: Multi-instance support, containerization, orchestration
## Potential Improvements

As noted in the code TODOs:

- Externalize configuration (host, port, polling intervals, concurrency)
- Add observability and metrics
- Implement GET endpoint for error logs
- Consider connection pooling optimizations
- Add retry logic with exponential backoff
- Implement graceful shutdown
- Add health check endpoints
- Consider using a production database instead of DuckDB for multi-instance deployments

## Dependencies

See `build.sbt` for complete dependency list. Major dependencies include:
- Cats Effect 3.x
- FS2
- Http4s Ember
- Doobie
- Circe
- DuckDB JDBC driver

## License

GPLv3

