-- 创建数据库和表结构
CREATE DATABASE IF NOT EXISTS hospital DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL,
    real_name VARCHAR(50),
    id_card VARCHAR(18),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    no_show_count INT DEFAULT 0,
    no_show_ban_until DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 科室表
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    floor_location VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 医生表
CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    title VARCHAR(50),
    specialty VARCHAR(200),
    introduction VARCHAR(1000),
    avatar VARCHAR(500),
    status ENUM('AVAILABLE', 'UNAVAILABLE') DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_department_id (department_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 排班模板表（每周固定排班）
CREATE TABLE IF NOT EXISTS schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week TINYINT NOT NULL COMMENT '1=周一, 7=周日',
    time_slot ENUM('MORNING', 'AFTERNOON') NOT NULL,
    max_appointments INT DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_day_slot (doctor_id, day_of_week, time_slot),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    UNIQUE KEY uk_doctor_day_slot (doctor_id, day_of_week, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 实际排班表（每日排班，可临时调整）
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    time_slot ENUM('MORNING', 'AFTERNOON') NOT NULL,
    max_appointments INT DEFAULT 10,
    booked_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_temporary_adjusted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_date (schedule_date),
    INDEX idx_doctor_date_slot (doctor_id, schedule_date, time_slot),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    UNIQUE KEY uk_doctor_date_slot (doctor_id, schedule_date, time_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 预约表
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_no VARCHAR(32) NOT NULL UNIQUE,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    schedule_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    time_slot ENUM('MORNING', 'AFTERNOON') NOT NULL,
    queue_number INT,
    patient_name VARCHAR(50),
    patient_id_card VARCHAR(18),
    patient_phone VARCHAR(20),
    symptoms TEXT,
    status ENUM('PENDING', 'VISITED', 'NO_SHOW', 'CANCELLED') DEFAULT 'PENDING',
    cancel_reason VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status),
    INDEX idx_date (appointment_date),
    FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 症状-科室映射表
CREATE TABLE IF NOT EXISTS symptom_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symptom_keyword VARCHAR(50) NOT NULL,
    department_id BIGINT NOT NULL,
    priority INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_keyword (symptom_keyword),
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化管理员账号 (password: admin123)
INSERT INTO users (username, password, role, real_name, is_active) VALUES 
('admin', '$2a$10$vI8aWBnW3fID.ZQ38G1v2eW5hH7kK9lL0mN2pQ4rS6tU8vW0xY2z', 'ADMIN', '系统管理员', TRUE);

-- 初始化示例科室
INSERT INTO departments (name, description, floor_location) VALUES 
('内科', '内科诊疗中心，擅长心血管、呼吸、消化等内科疾病', '门诊楼3层'),
('外科', '外科诊疗中心，擅长普外科、骨科等手术治疗', '门诊楼2层'),
('儿科', '儿科诊疗中心，擅长儿童常见疾病诊治', '门诊楼1层'),
('妇产科', '妇产科诊疗中心，擅长妇科、产科疾病', '门诊楼4层'),
('眼科', '眼科诊疗中心，擅长眼科疾病诊治', '门诊楼5层'),
('耳鼻喉科', '耳鼻喉科诊疗中心，擅长耳鼻喉疾病', '门诊楼5层'),
('皮肤科', '皮肤科诊疗中心，擅长皮肤疾病诊治', '门诊楼1层'),
('口腔科', '口腔科诊疗中心，擅长口腔疾病诊治', '门诊楼2层');

-- 初始化症状-科室映射
INSERT INTO symptom_department (symptom_keyword, department_id, priority) VALUES 
('咳嗽', 1, 1), ('发烧', 1, 1), ('感冒', 1, 1), ('头痛', 1, 1), ('胸闷', 1, 1),
('腹痛', 1, 2), ('腹泻', 1, 2), ('恶心', 1, 2), ('呕吐', 1, 2),
('骨折', 2, 1), ('外伤', 2, 1), ('疼痛', 2, 2), ('肿块', 2, 1),
('发热', 3, 1), ('哭闹', 3, 1), ('食欲不振', 3, 1), ('发育', 3, 1),
('月经', 4, 1), ('怀孕', 4, 1), ('白带', 4, 1), ('腹痛', 4, 2),
('视力', 5, 1), ('眼睛', 5, 1), ('失明', 5, 1), ('红肿', 5, 1),
('耳朵', 6, 1), ('鼻子', 6, 1), ('喉咙', 6, 1), ('听力', 6, 1),
('皮肤', 7, 1), ('瘙痒', 7, 1), ('皮疹', 7, 1), ('过敏', 7, 1),
('牙齿', 8, 1), ('口腔', 8, 1), ('牙龈', 8, 1), ('牙痛', 8, 1);
