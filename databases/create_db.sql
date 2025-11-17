-- Создание базы данных: database/study_platform.db
-- Запустится автоматически

-- 1. users
CREATE TABLE IF NOT EXISTS users (
    userId INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    email TEXT UNIQUE,
    password_hash TEXT
);

-- 2. groups
CREATE TABLE IF NOT EXISTS groups (
    groupId INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT,
    description TEXT,
    created_by INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(userId)
);

-- 3. memberships
CREATE TABLE IF NOT EXISTS memberships (
    membershipId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER,
    groupId INTEGER,
    role TEXT DEFAULT 'member',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId),
    FOREIGN KEY (groupId) REFERENCES groups(groupId)
);

-- 4. tasks
CREATE TABLE IF NOT EXISTS tasks (
    taskId INTEGER PRIMARY KEY AUTOINCREMENT,
    groupId INTEGER,
    createdBy INTEGER,
    title TEXT,
    description TEXT,
    status TEXT DEFAULT 'open',
    deadline DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupId) REFERENCES groups(groupId),
    FOREIGN KEY (createdBy) REFERENCES users(userId)
);

-- 5. resources
CREATE TABLE IF NOT EXISTS resources (
    resourceId INTEGER PRIMARY KEY AUTOINCREMENT,
    groupId INTEGER,
    uploadedBy INTEGER,
    title TEXT,
    type TEXT,
    path_or_url TEXT,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (groupId) REFERENCES groups(groupId),
    FOREIGN KEY (uploadedBy) REFERENCES users(userId)
);

-- 6. activity_log
CREATE TABLE IF NOT EXISTS activity_log (
    logId INTEGER PRIMARY KEY AUTOINCREMENT,
    userId INTEGER,
    action TEXT,
    details TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES users(userId)
);

-- Индексы для скорости
CREATE INDEX IF NOT EXISTS idx_memberships_group ON memberships(groupId);
CREATE INDEX IF NOT EXISTS idx_tasks_group ON tasks(groupId);
CREATE INDEX IF NOT EXISTS idx_resources_group ON resources(groupId);
