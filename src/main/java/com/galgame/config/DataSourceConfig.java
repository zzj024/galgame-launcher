package com.galgame.config;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * SQLite 数据源配置。
 * 启动时自动启用 WAL 模式和 foreign_keys，以保障数据完整性和并发性能。
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Bean
    @Primary
    public DataSource dataSource() {
        // 确保数据库文件所在目录存在
        ensureDatabaseDirectoryExists();

        DataSource dataSource = DataSourceBuilder.create()
                .url(dbUrl)
                .driverClassName("org.sqlite.JDBC")
                .build();

        // 启用 WAL 模式和 foreign keys
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL");
            stmt.execute("PRAGMA foreign_keys=ON");
            stmt.execute("PRAGMA busy_timeout=5000");
            log.info("SQLite pragmas configured: WAL mode, foreign_keys ON, busy_timeout=5000");
        } catch (Exception e) {
            log.error("Failed to configure SQLite pragmas", e);
        }

        return dataSource;
    }

    /**
     * 确保数据库文件所在目录存在，不存在则创建。
     */
    private void ensureDatabaseDirectoryExists() {
        // 从 JDBC URL 中提取数据库文件路径
        // 格式: jdbc:sqlite:/path/to/db/file.db
        String dbPath = dbUrl.replace("jdbc:sqlite:", "");
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (created) {
                log.info("Created database directory: {}", parentDir.getAbsolutePath());
            } else {
                log.error("Failed to create database directory: {}", parentDir.getAbsolutePath());
            }
        }
    }
}
