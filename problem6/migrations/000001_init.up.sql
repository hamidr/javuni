CREATE EXTENSION IF NOT EXISTS "postgis";

CREATE TYPE charge_point_status AS ENUM (
    'AVAILABLE',
    'OCCUPIED',
    'OFFLINE'
);

-- Create the charge_points table
CREATE TABLE IF NOT EXISTS charge_points (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    status charge_point_status NOT NULL DEFAULT 'OFFLINE',
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    geopoint GEOGRAPHY(Point, 4326) GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)) STORED,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_charge_points_name_lat_lon UNIQUE (name, latitude, longitude)
);

CREATE INDEX idx_geopoint ON charge_points USING GIST (geopoint);

INSERT INTO charge_points (name, latitude, longitude, status) 
VALUES
    ('Downtown Station A', -122.4194, 37.7749, 'AVAILABLE'),
    ('Downtown Station B', -122.4195, 37.7750, 'OCCUPIED'),
    ('Airport Charging Hub', -122.3750, 37.6213, 'AVAILABLE'),
    ('Shopping Mall Station', -122.4068, 37.7849, 'OFFLINE');

