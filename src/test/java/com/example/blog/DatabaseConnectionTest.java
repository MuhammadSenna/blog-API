package com.example.blog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldConnectToDatabase() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1), "Database connection should be valid");
            
            DatabaseMetaData metaData = connection.getMetaData();
            assertNotNull(metaData, "Database metadata should not be null");
            
            System.out.println("Database: " + metaData.getDatabaseProductName());
            System.out.println("Version: " + metaData.getDatabaseProductVersion());
            System.out.println("URL: " + metaData.getURL());
        }
    }

    @Test
    void shouldHaveAllEntityTablesCreated() throws SQLException {
        List<String> expectedTables = List.of("users", "posts", "comments", "categories", "tags", "post_tags");
        List<String> actualTables = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Get all tables in the blog_db database
            try (ResultSet rs = metaData.getTables("blog_db", null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    actualTables.add(tableName.toLowerCase());
                    System.out.println("Found table: " + tableName);
                }
            }
        }

        // Verify all expected tables exist
        for (String expectedTable : expectedTables) {
            assertTrue(actualTables.contains(expectedTable), 
                "Table '" + expectedTable + "' should exist in the database");
        }
        
        System.out.println("\nAll required tables verified:");
        expectedTables.forEach(table -> System.out.println("  ✓ " + table));
    }
}
