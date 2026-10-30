CREATE DATABASE IF NOT EXISTS rythusahayak_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE rythusahayak_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(254) NOT NULL,
    mobile VARCHAR(10) NULL,
    password_hash VARCHAR(60) NOT NULL,
    role ENUM('FARMER', 'ADMIN') NOT NULL DEFAULT 'FARMER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_mobile CHECK (mobile IS NULL OR mobile REGEXP '^[6-9][0-9]{9}$'),
    INDEX idx_users_role_active (role, is_active)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    reference_number VARCHAR(29) NOT NULL,
    user_id BIGINT UNSIGNED NULL,
    farmer_name VARCHAR(80) NOT NULL,
    mobile VARCHAR(10) NOT NULL,
    village VARCHAR(80) NOT NULL,
    district VARCHAR(80) NOT NULL,
    crop ENUM('Rice', 'Cotton', 'Tomato', 'Chilli', 'Maize', 'Groundnut', 'Other') NOT NULL,
    category ENUM('Crop Disease', 'Pest Concern', 'Irrigation', 'Soil/Nutrition', 'Weather Damage', 'Other') NOT NULL,
    description VARCHAR(1000) NOT NULL,
    observed_date DATE NOT NULL,
    status ENUM('PENDING', 'UNDER_REVIEW', 'ACTION_RECORDED', 'RESOLVED') NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_complaints_reference UNIQUE (reference_number),
    CONSTRAINT fk_complaints_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_complaints_status_created (status, created_at),
    INDEX idx_complaints_created (created_at),
    INDEX idx_complaints_user (user_id)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS complaint_updates (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    complaint_id BIGINT UNSIGNED NOT NULL,
    admin_user_id BIGINT UNSIGNED NOT NULL,
    previous_status ENUM('PENDING', 'UNDER_REVIEW', 'ACTION_RECORDED', 'RESOLVED') NOT NULL,
    new_status ENUM('PENDING', 'UNDER_REVIEW', 'ACTION_RECORDED', 'RESOLVED') NOT NULL,
    public_remark VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_updates_complaint FOREIGN KEY (complaint_id) REFERENCES complaints (id) ON DELETE CASCADE,
    CONSTRAINT fk_updates_admin FOREIGN KEY (admin_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_updates_complaint_created (complaint_id, created_at),
    INDEX idx_updates_admin (admin_user_id)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
