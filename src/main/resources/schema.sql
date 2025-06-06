CREATE TABLE IF NOT EXISTS user (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS clients (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       nom VARCHAR(255),
    prenom VARCHAR(255),
    cin VARCHAR(255) NOT NULL UNIQUE,
    date_naissance DATE,
    email VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    nationalite VARCHAR(255),
    sexe VARCHAR(255),
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES user(id)
    );

CREATE TABLE IF NOT EXISTS policies (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        numero_police VARCHAR(255) UNIQUE,
    type_assurance VARCHAR(255),
    date_debut DATETIME,
    date_fin DATETIME,
    valid BOOLEAN,
    client_id BIGINT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
    );

CREATE TABLE IF NOT EXISTS policy_guarantees (
                                                 policy_id BIGINT,
                                                 guarantees VARCHAR(255),
    FOREIGN KEY (policy_id) REFERENCES policies(id)
    );

CREATE TABLE IF NOT EXISTS assets (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      type VARCHAR(255),
    identifier VARCHAR(255),
    description VARCHAR(255),
    policy_id BIGINT,
    FOREIGN KEY (policy_id) REFERENCES policies(id)
    );

-- Update experts table to include location and constrain specialite
ALTER TABLE experts
    ADD COLUMN location VARCHAR(255),
    ADD CONSTRAINT check_specialite CHECK (specialite IN ('vehicle', 'home', 'health', 'property'));

--- Ensure expert_id_seq exists
CREATE SEQUENCE IF NOT EXISTS expert_id_seq START 1 INCREMENT 1;

-- Update experts table to enforce specialite constraint
ALTER TABLE experts
    ADD CONSTRAINT check_specialite CHECK (specialite IN ('vehicle', 'home', 'health', 'property'))
    ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS sinistres (
                                         id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                         type VARCHAR(255),
    date TIMESTAMP,
    lieu VARCHAR(255),
    description TEXT,
    fraud_score DOUBLE PRECISION DEFAULT 0.0,
    status VARCHAR(50),
    priority_score INTEGER,
    policy_id BIGINT,
    expert_id BIGINT,
    admin_id BIGINT,
    montant_indemnisation DECIMAL,
    numero_sinistre VARCHAR(255) UNIQUE,
    vehicle_type VARCHAR(255),
    vehicle_make VARCHAR(255),
    vehicle_model VARCHAR(255),
    vehicle_year VARCHAR(255),
    vin VARCHAR(255),
    accident_type VARCHAR(255),
    third_party_involved BOOLEAN,
    police_report_number VARCHAR(255),
    property_address VARCHAR(255),
    damage_type VARCHAR(255),
    damage_extent VARCHAR(255),
    emergency_services_called BOOLEAN,
    medical_condition VARCHAR(255),
    treatment_location VARCHAR(255),
    treatment_date TIMESTAMP,
    doctor_name VARCHAR(255),
    medical_bill_amount DOUBLE PRECISION,
    hospitalization_required BOOLEAN,
    property_type VARCHAR(255),
    incident_cause VARCHAR(255),
    property_damage_description TEXT,
    estimated_loss_value DOUBLE PRECISION,
    business_interruption BOOLEAN,
    FOREIGN KEY (policy_id) REFERENCES policies(id),
    FOREIGN KEY (expert_id) REFERENCES experts(id),
    FOREIGN KEY (admin_id) REFERENCES admins(id)
    );

CREATE TABLE IF NOT EXISTS roles (
                                     id BIGSERIAL PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL UNIQUE
    );

INSERT INTO roles (name) VALUES ('ROLE_CLIENT') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_EXPERT') ON CONFLICT (name) DO NOTHING;

CREATE TABLE IF NOT EXISTS media_references (
                                                id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                                file_path VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    sinistre_id BIGINT,
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id) ON DELETE CASCADE

    CREATE TABLE IF NOT EXISTS sinistre_affected_areas (
                                                           sinistre_id BIGINT,
                                                           affected_area VARCHAR(255),
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id)
    );

    -- Ensure expert_id_seq exists
    CREATE SEQUENCE IF NOT EXISTS expert_id_seq START 1 INCREMENT 1;

-- Update experts table to enforce specialite constraint
    ALTER TABLE experts
    ADD CONSTRAINT check_specialite CHECK (specialite IN ('vehicle', 'home', 'health', 'property'))
                                                       ON CONFLICT DO NOTHING;

-- Create reports table
    CREATE TABLE IF NOT EXISTS reports (
                                           id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                           sinistre_id BIGINT NOT NULL,
                                           expert_id BIGINT NOT NULL,
                                           content TEXT NOT NULL,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           FOREIGN KEY (sinistre_id) REFERENCES sinistres(id),
    FOREIGN KEY (expert_id) REFERENCES experts(id)
    );

-- Create meetings table
    CREATE TABLE IF NOT EXISTS meetings (
                                            id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                            sinistre_id BIGINT NOT NULL,
                                            expert_id BIGINT NOT NULL,
                                            meeting_type VARCHAR(50) NOT NULL CHECK (meeting_type IN ('INITIAL', 'FINAL')),
    meeting_date TIMESTAMP NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id),
    FOREIGN KEY (expert_id) REFERENCES experts(id)
    );

-- Create notifications table
    CREATE TABLE IF NOT EXISTS notifications (
                                                 id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                                 user_id BIGINT NOT NULL,
                                                 sinistre_id BIGINT NOT NULL,
                                                 message TEXT NOT NULL,
                                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 is_read BOOLEAN NOT NULL DEFAULT FALSE,
                                                 FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (sinistre_id) REFERENCES sinistres(id)
    );

-- Ensure user_roles table exists
    CREATE TABLE IF NOT EXISTS user_roles (
                                              user_id BIGINT NOT NULL,
                                              role_id BIGINT NOT NULL,
                                              PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

-- Ensure roles table exists
    CREATE TABLE IF NOT EXISTS roles (
                                         id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                         name VARCHAR(50) UNIQUE NOT NULL
    );

-- Ensure users table exists
    CREATE TABLE IF NOT EXISTS users (
                                         id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                         username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL
    );

-- Ensure admins table exists
    CREATE TABLE IF NOT EXISTS admins (
                                          id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                                          nom VARCHAR(255) NOT NULL,
    contact VARCHAR(15) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Create admin_audit_trail table
    CREATE TABLE IF NOT EXISTS admin_audit_trail (
                                                     admin_id BIGINT NOT NULL,
                                                     audit_entry TEXT NOT NULL,
                                                     FOREIGN KEY (admin_id) REFERENCES admins(id)
    );