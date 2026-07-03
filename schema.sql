-- ============================================================
-- AMS (사내 자산 통합 관리 시스템) DB 스키마
-- MariaDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS ams_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ams_db;

CREATE TABLE tb_department (
    dept_id    INT AUTO_INCREMENT PRIMARY KEY,
    dept_name  VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT NOW()
);
INSERT INTO tb_department (dept_name) VALUES ('개발부'), ('IT사업부');

CREATE TABLE tb_user (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    employee_no VARCHAR(20)  UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(20)  NOT NULL,
    dept_id     INT,
    is_admin    TINYINT(1)   DEFAULT 0,
    is_active   TINYINT(1)   DEFAULT 1,
    must_change_password TINYINT(1) DEFAULT 1,
    created_at  DATETIME DEFAULT NOW(),
    updated_at  DATETIME DEFAULT NOW() ON UPDATE NOW(),
    FOREIGN KEY (dept_id) REFERENCES tb_department(dept_id)
);
-- 초기 관리자 계정: employee_no = admin / password = admin1234 (BCrypt)
INSERT INTO tb_user (employee_no, password, name, is_admin, must_change_password)
VALUES ('admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa/3xBpVfIwXc9l3zBIOVYY.9x1YU.XK', '관리자', 1, 0);

CREATE TABLE tb_user_role (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    role       ENUM('DEPT_MANAGER','USER') NOT NULL,
    granted_by INT NOT NULL,
    granted_at DATETIME DEFAULT NOW(),
    UNIQUE KEY uk_user_role (user_id, role),
    FOREIGN KEY (user_id)    REFERENCES tb_user(user_id),
    FOREIGN KEY (granted_by) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_permission (
    perm_id    INT AUTO_INCREMENT PRIMARY KEY,
    perm_code  VARCHAR(50)  UNIQUE NOT NULL,
    perm_name  VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT NOW()
);
INSERT INTO tb_permission (perm_code, perm_name) VALUES ('PASSWORD.EDIT', '패스워드 수정');

CREATE TABLE tb_user_permission (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    perm_id    INT NOT NULL,
    granted_by INT NOT NULL,
    granted_at DATETIME DEFAULT NOW(),
    UNIQUE KEY uk_user_perm (user_id, perm_id),
    FOREIGN KEY (user_id)    REFERENCES tb_user(user_id),
    FOREIGN KEY (perm_id)    REFERENCES tb_permission(perm_id),
    FOREIGN KEY (granted_by) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_account_type (
    type_id    INT AUTO_INCREMENT PRIMARY KEY,
    type_name  VARCHAR(50) NOT NULL,
    created_at DATETIME DEFAULT NOW()
);
INSERT INTO tb_account_type (type_name) VALUES ('서버'), ('NAS'), ('SVN'), ('GitLab');

CREATE TABLE tb_password (
    pw_id         INT AUTO_INCREMENT PRIMARY KEY,
    created_at    DATETIME DEFAULT NOW(),
    updated_at    DATETIME DEFAULT NOW() ON UPDATE NOW(),
    last_modifier VARCHAR(50),
    type_id       INT NOT NULL,
    requester_id  INT,
    user_id       INT NOT NULL,
    approver_id   INT,
    account_id    VARCHAR(100),
    changed_at    DATETIME,
    expire_at     DATE,
    change_reason VARCHAR(200),
    confirmed_at  DATETIME,
    confirmed_by  INT,
    note          VARCHAR(100),
    FOREIGN KEY (type_id)      REFERENCES tb_account_type(type_id),
    FOREIGN KEY (requester_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (user_id)      REFERENCES tb_user(user_id),
    FOREIGN KEY (approver_id)  REFERENCES tb_user(user_id),
    FOREIGN KEY (confirmed_by) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_refresh_token (
    token_id   VARCHAR(36) PRIMARY KEY,
    user_id    INT NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked    TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_audit_log (
    log_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,
    action     VARCHAR(100) NOT NULL,
    target     VARCHAR(100),
    detail     TEXT,
    created_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);
