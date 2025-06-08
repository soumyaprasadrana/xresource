-- V1__aco_schema.sql

-- Table: aco_schema_graph_edge
CREATE TABLE IF NOT EXISTS aco_schema_graph_edge (
    id SERIAL PRIMARY KEY,
    from_table VARCHAR(128) NOT NULL,
    to_table VARCHAR(128) NOT NULL,
    join_condition TEXT NOT NULL,
    latency DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    pheromone_level DOUBLE PRECISION NOT NULL DEFAULT 0.1,
    last_decay TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    join_hash VARCHAR(64) NOT NULL,
    hash_updated BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT unique_edge UNIQUE (from_table, to_table)
);

-- Table: aco_path_request
-- Stores individual query path requests made by ants
CREATE TABLE IF NOT EXISTS aco_path_request (
    id SERIAL PRIMARY KEY,
    request_time TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    path TEXT NOT NULL, -- serialized path as JSON array of tables or edge IDs
    cost DOUBLE PRECISION NOT NULL,
    pheromone_contribution DOUBLE PRECISION NOT NULL,
    materialized_view_created BOOLEAN NOT NULL DEFAULT FALSE
);

-- Table: aco_materialized_view
-- Tracks views generated for optimized paths
CREATE TABLE IF NOT EXISTS aco_materialized_view (
    id SERIAL PRIMARY KEY,
    view_name VARCHAR(128) NOT NULL UNIQUE,
    definition TEXT NOT NULL, -- SQL definition of the view
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    usage_count BIGINT NOT NULL DEFAULT 0
);

-- Table: aco_index_tracking
-- Tracks indexes created as part of optimization
CREATE TABLE IF NOT EXISTS aco_index_tracking (
    id SERIAL PRIMARY KEY,
    index_name VARCHAR(128) NOT NULL UNIQUE,
    table_name VARCHAR(128) NOT NULL,
    columns TEXT NOT NULL, -- comma-separated list of columns or JSON array
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    usage_count BIGINT NOT NULL DEFAULT 0
);
