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
    last_login_at DATETIME,
    created_at  DATETIME DEFAULT NOW(),
    updated_at  DATETIME DEFAULT NOW() ON UPDATE NOW(),
    FOREIGN KEY (dept_id) REFERENCES tb_department(dept_id)
);
-- 최고관리자 계정은 애플리케이션 기동 시 AdminBootstrapRunner가 자동으로 생성합니다.
-- (환경변수 ADMIN_EMPLOYEE_NO / ADMIN_PASSWORD / ADMIN_NAME 으로 설정 가능, 기본값 admin/admin1234)

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
    perm_id     INT AUTO_INCREMENT PRIMARY KEY,
    perm_code   VARCHAR(50)  UNIQUE NOT NULL,
    perm_name   VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    created_at  DATETIME DEFAULT NOW()
);
INSERT INTO tb_permission (perm_code, perm_name, description) VALUES
    ('PASSWORD.EDIT', '패스워드 수정', '패스워드 관리대장 항목 등록·수정 권한'),
    ('PASSWORD.APPROVE', '패스워드 승인', '패스워드 관리대장 항목 승인 권한');

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
    type_id     INT AUTO_INCREMENT PRIMARY KEY,
    type_name   VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    created_at  DATETIME DEFAULT NOW()
);
INSERT INTO tb_account_type (type_name, description) VALUES
    ('서버', 'Linux / Windows 서버 SSH·RDP 계정'),
    ('NAS', '스토리지 관리자 계정'),
    ('SVN', '형상관리 저장소 계정'),
    ('GitLab', 'Git 저장소·CI 토큰');

CREATE TABLE tb_password (
    pw_id         INT AUTO_INCREMENT PRIMARY KEY,
    created_at    DATETIME DEFAULT NOW(),
    updated_at    DATETIME DEFAULT NOW() ON UPDATE NOW(),
    last_modifier VARCHAR(50),
    type_id       INT NOT NULL,
    requester_name VARCHAR(20),
    user_id       INT NOT NULL,
    -- 승인자·최종확인자는 동일 개념이라 하나로 합침. 등록 시점엔 비어있고, 승인 시 자동으로 채워짐.
    approver_name VARCHAR(20),
    approved_at   DATETIME,
    status        ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    rejected_by   VARCHAR(20),
    rejected_at   DATETIME,
    reject_reason VARCHAR(200),
    account_id    VARCHAR(100),
    changed_at    DATETIME,
    expire_at     DATE,
    change_reason VARCHAR(200),
    note          VARCHAR(100),
    FOREIGN KEY (type_id)      REFERENCES tb_account_type(type_id),
    FOREIGN KEY (user_id)      REFERENCES tb_user(user_id)
);

CREATE TABLE tb_refresh_token (
    token_id   VARCHAR(36) PRIMARY KEY,
    user_id    INT NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked    TINYINT(1) DEFAULT 0,
    created_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_notification (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    type        VARCHAR(50) NOT NULL,
    title       VARCHAR(200) NOT NULL,
    message     VARCHAR(300),
    link        VARCHAR(200),
    is_read     TINYINT(1) DEFAULT 0,
    created_at  DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);

CREATE TABLE tb_audit_log (
    log_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,
    action     VARCHAR(100) NOT NULL,
    target     VARCHAR(100),
    detail     TEXT,
    ip_address VARCHAR(45),
    result     ENUM('성공','실패','경고') NOT NULL DEFAULT '성공',
    created_at DATETIME DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id)
);
