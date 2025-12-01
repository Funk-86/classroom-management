package org.example.classroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 数据源配置诊断类
 * 在应用启动时输出数据库连接信息（不包含密码），用于诊断环境变量是否正确读取
 */
@Component
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    @Value("${DB_HOST:}")
    private String dbHost;

    @Value("${DB_PORT:}")
    private String dbPort;

    @Value("${DB_NAME:}")
    private String dbName;

    @Value("${DB_USERNAME:}")
    private String dbUsername;

    @Value("${MYSQL_HOST:}")
    private String mysqlHost;

    @Value("${MYSQL_PORT:}")
    private String mysqlPort;

    @Value("${MYSQL_DATABASE:}")
    private String mysqlDatabase;

    @Value("${MYSQL_USERNAME:}")
    private String mysqlUsername;

    @EventListener(ApplicationReadyEvent.class)
    public void logDataSourceInfo() {
        log.info("========================================");
        log.info("数据库连接配置诊断信息");
        log.info("========================================");

        // 输出环境变量（不包含密码）
        log.info("环境变量 DB_HOST: {}", dbHost.isEmpty() ? "(未设置)" : dbHost);
        log.info("环境变量 DB_PORT: {}", dbPort.isEmpty() ? "(未设置)" : dbPort);
        log.info("环境变量 DB_NAME: {}", dbName.isEmpty() ? "(未设置)" : dbName);
        log.info("环境变量 DB_USERNAME: {}", dbUsername.isEmpty() ? "(未设置)" : dbUsername);
        log.info("环境变量 DB_PASSWORD: {}", "***已设置***");

        log.info("环境变量 MYSQL_HOST: {}", mysqlHost.isEmpty() ? "(未设置)" : mysqlHost);
        log.info("环境变量 MYSQL_PORT: {}", mysqlPort.isEmpty() ? "(未设置)" : mysqlPort);
        log.info("环境变量 MYSQL_DATABASE: {}", mysqlDatabase.isEmpty() ? "(未设置)" : mysqlDatabase);
        log.info("环境变量 MYSQL_USERNAME: {}", mysqlUsername.isEmpty() ? "(未设置)" : mysqlUsername);
        log.info("环境变量 MYSQL_PASSWORD: {}", "***已设置***");

        // 输出最终使用的连接信息（不包含密码）
        log.info("最终数据库连接URL: {}", datasourceUrl);
        log.info("最终数据库用户名: {}", datasourceUsername);
        log.info("========================================");

        // 检查关键配置
        if (datasourceUrl.contains("localhost") && dbHost.isEmpty() && mysqlHost.isEmpty()) {
            log.warn("⚠️  警告：数据库主机仍然是 localhost，请检查环境变量 DB_HOST 或 MYSQL_HOST 是否正确设置");
        }

        if (datasourceUrl.contains("3306") && dbPort.isEmpty() && mysqlPort.isEmpty()) {
            log.warn("⚠️  警告：数据库端口仍然是 3306，请检查环境变量 DB_PORT 或 MYSQL_PORT 是否正确设置");
        }

        if (datasourceUsername.isEmpty() || "root".equals(datasourceUsername)) {
            log.info("✓ 数据库用户名: {}", datasourceUsername);
        }
    }
}

