-- V1: 初始数据库结构
-- 包含全部 7 张表 + 默认数据

-- 保存位置
CREATE TABLE IF NOT EXISTS save_locations (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    path        TEXT    NOT NULL UNIQUE,
    is_default  BOOLEAN DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 类型标签
CREATE TABLE IF NOT EXISTS type_tags (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    is_builtin  BOOLEAN DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入默认类型标签
INSERT OR IGNORE INTO type_tags (name, is_builtin) VALUES ('纯爱', 1);
INSERT OR IGNORE INTO type_tags (name, is_builtin) VALUES ('悬疑', 1);
INSERT OR IGNORE INTO type_tags (name, is_builtin) VALUES ('剧情', 1);
INSERT OR IGNORE INTO type_tags (name, is_builtin) VALUES ('其他', 1);

-- 自定义标签
CREATE TABLE IF NOT EXISTS custom_tags (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 游戏主表
CREATE TABLE IF NOT EXISTS games (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    name                TEXT    NOT NULL,
    cover_path          TEXT,
    exe_path            TEXT    NOT NULL,
    folder_path         TEXT    NOT NULL,
    save_location_id    INTEGER REFERENCES save_locations(id),
    status              TEXT    NOT NULL DEFAULT 'pending',
    type_id             INTEGER REFERENCES type_tags(id),
    custom_tags         TEXT    DEFAULT '[]',
    is_pinned           BOOLEAN DEFAULT 0,
    is_deleted          BOOLEAN DEFAULT 0,
    play_time_seconds   INTEGER DEFAULT 0,
    note                TEXT    DEFAULT '',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 游玩记录
CREATE TABLE IF NOT EXISTS play_sessions (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    game_id          INTEGER NOT NULL REFERENCES games(id),
    started_at       TIMESTAMP NOT NULL,
    ended_at         TIMESTAMP,
    duration_seconds INTEGER DEFAULT 0
);

-- 主题
CREATE TABLE IF NOT EXISTS themes (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL,
    config      TEXT    NOT NULL,
    is_builtin  BOOLEAN DEFAULT 0,
    is_active   BOOLEAN DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 设置
CREATE TABLE IF NOT EXISTS settings (
    key   TEXT PRIMARY KEY,
    value TEXT
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_games_status ON games(status);
CREATE INDEX IF NOT EXISTS idx_games_type ON games(type_id);
CREATE INDEX IF NOT EXISTS idx_games_pinned ON games(is_pinned);
CREATE INDEX IF NOT EXISTS idx_games_deleted ON games(is_deleted);
CREATE INDEX IF NOT EXISTS idx_play_sessions_game ON play_sessions(game_id);
