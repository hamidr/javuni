
# Chargepoints — README
A small service for managing EV charging points. This document explains how to run the service with podman-compose and shows example API requests.

## Prerequisites
- podman and podman-compose installed (or use docker-compose if you prefer).
- Port 8081 available on localhost.

## Run the service

Start in detached mode:
```
source .env
podman-compose up -d
```

Stop and remove containers:
```
podman-compose down
```

View logs:
```
podman-compose logs -f
```

Rebuild images (if needed):
```
podman-compose build
```

## API base
All examples assume:
Base URL: http://localhost:8081

Content-Type: application/json

## Examples

Create a charging point
```
curl -X POST "http://localhost:8081/chargepoints" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Downtown Station A",
    "status": "AVAILABLE",
    "latitude": 37.7749,
    "longitude": -122.4194
  }'
```

Get nearby charging points
- Query params: lat, lon, radius (radius unit is kilometers)
```
curl -X GET "http://localhost:8081/chargepoints/nearby?lat=37.7749&lon=-122.4194&radius=5"
```

Get a charging point by ID
```
curl -X GET "http://localhost:8081/chargepoints/<chargepoint-id>"
```

Replace `<chargepoint-id>` with the UUID returned from the create request, for example:
```
curl -X GET "http://localhost:8081/chargepoints/fb6ea39e-7eba-4fb3-a6d1-2fef5be2ac7d"
```

## Notes
- If you use docker-compose instead of podman-compose, the commands are the same but starting with `docker-compose`.
- If the service exposes additional endpoints (health, metrics, docs), consult the running service logs or its API documentation for details.

