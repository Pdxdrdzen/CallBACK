-- Flyway Database Migration: V1__init_schema.sql
-- CallBACK Initial Relational Schema

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================================
-- 1. USERS TABLE
-- ========================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ========================================================
-- 2. CONTACTS TABLE
-- ========================================================
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30) NOT NULL,
    email VARCHAR(255),
    priority_role VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contacts_user_phone ON contacts (user_id, phone_number);

-- ========================================================
-- 3. EMERGENCY KEYWORDS TABLE
-- ========================================================
CREATE TABLE emergency_keywords (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    weight INT NOT NULL CHECK (weight BETWEEN 1 AND 100),
    is_regex BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_keywords_user_active ON emergency_keywords (user_id, is_active);

-- ========================================================
-- 4. NOTIFICATION RULES TABLE
-- ========================================================
CREATE TABLE notification_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    min_priority VARCHAR(50) NOT NULL,
    sound_channel VARCHAR(100) NOT NULL,
    vibration_pattern VARCHAR(100) NOT NULL,
    override_dnd BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notification_rules_user ON notification_rules (user_id);

-- ========================================================
-- 5. INCOMING MESSAGES TABLE
-- ========================================================
CREATE TABLE incoming_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_identifier VARCHAR(100) NOT NULL,
    raw_content TEXT NOT NULL,
    sanitized_content TEXT NOT NULL,
    calculated_priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incoming_messages_recipient_created ON incoming_messages (recipient_user_id, created_at DESC);

-- ========================================================
-- 6. INTERACTION SLA RECORDS TABLE
-- ========================================================
CREATE TABLE interaction_sla_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES incoming_messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    deadline_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(50) NOT NULL,
    acknowledged_at TIMESTAMPTZ
);

CREATE INDEX idx_sla_records_status_deadline ON interaction_sla_records (status, deadline_at);
CREATE INDEX idx_sla_records_user ON interaction_sla_records (user_id);

-- ========================================================
-- 7. OUTBOX EVENTS TABLE (Transactional Outbox Pattern)
-- ========================================================
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_unprocessed ON outbox_events (processed, created_at) WHERE processed = FALSE;