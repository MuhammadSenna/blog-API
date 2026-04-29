package com.example.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @InjectMocks
    private OpenApiConfig openApiConfig;

    @Test
    void customOpenAPI_ReturnsConfiguredOpenAPI() {
        // Act
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(openAPI, "OpenAPI object should not be null");
        
        Info info = openAPI.getInfo();
        assertNotNull(info, "Info object should not be null");
        assertEquals("Blog REST API", info.getTitle(), "API title should match");
        assertEquals("1.0", info.getVersion(), "API version should match");
        assertEquals("REST API for blog application with JWT authentication", 
                     info.getDescription(), "API description should match");
        
        assertNotNull(info.getContact(), "Contact should not be null");
        assertEquals("API Support", info.getContact().getName(), "Contact name should match");
        assertEquals("support@example.com", info.getContact().getEmail(), "Contact email should match");
    }

    @Test
    void customOpenAPI_IncludesSecurityRequirement() {
        // Act
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(openAPI.getSecurity(), "Security requirements should not be null");
        assertFalse(openAPI.getSecurity().isEmpty(), "Security requirements should not be empty");
        
        SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
        assertTrue(securityRequirement.containsKey("bearerAuth"), 
                   "Security requirement should contain bearerAuth");
    }

    @Test
    void customOpenAPI_SecuritySchemeAnnotationPresent() {
        // Assert
        assertTrue(OpenApiConfig.class.isAnnotationPresent(
            io.swagger.v3.oas.annotations.security.SecurityScheme.class),
            "OpenApiConfig should have @SecurityScheme annotation");
    }
}
