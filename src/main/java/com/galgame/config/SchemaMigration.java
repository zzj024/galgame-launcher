package com.galgame.config;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/**
 * 数据库 schema 版本迁移。
 * 启动时检测 schema_version 表，若不存在则创建并按版本号顺序执行迁移脚本。
 * 迁移脚本命名规范：V{version}__{description}.sql，存放在 classpath:db/migration/。
 *
 * 使用 Spring ScriptUtils 执行 SQL 脚本，自动处理分句、注释跳过、事务等。
 */
@Component
public class SchemaMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Value("${galgame.db.target-version:1}")
    private int targetVersion;

    public SchemaMigration(DataSource dataSource, ResourceLoader resourceLoader) {
        this.dataSource = dataSource;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting database migration...");

        try (Connection conn = dataSource.getConnection()) {
            // 确保 schema_version 表存在
            ensureSchemaVersionTable(conn);

            int currentVersion = getCurrentVersion(conn);
            log.info("Current schema version: {}, target: {}", currentVersion, targetVersion);

            // 逐版本执行迁移
            for (int v = currentVersion + 1; v <= targetVersion; v++) {
                applyMigration(conn, v);
            }

            log.info("Database migration completed successfully.");
        }
    }

    private void ensureSchemaVersionTable(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY," +
                    "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
        }
    }

    private int getCurrentVersion(Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT MAX(version) FROM schema_version");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int v = rs.getInt(1);
                return rs.wasNull() ? 0 : v;
            }
        }
        return 0;
    }

    private void applyMigration(Connection conn, int version) throws Exception {
        String location = "classpath:db/migration/V%d__*.sql".formatted(version);
        Resource[] resources = ((org.springframework.core.io.support.ResourcePatternResolver) resourceLoader)
                .getResources(location);

        if (resources.length == 0) {
            log.warn("No migration script found for version {}", version);
            return;
        }

        for (Resource resource : resources) {
            log.info("Applying migration: {}", resource.getFilename());

            // 使用 Spring ScriptUtils 执行 SQL 脚本
            // 自动处理：分号分割、单行注释（--）、块注释（/* */）、事务控制
            ScriptUtils.executeSqlScript(conn, resource);

            // 记录版本
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO schema_version (version) VALUES (?)")) {
                pstmt.setInt(1, version);
                pstmt.executeUpdate();
            }

            log.info("Migration {} applied successfully.", version);
        }
    }
}
