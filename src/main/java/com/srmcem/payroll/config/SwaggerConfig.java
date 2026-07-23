package com.srmcem.payroll.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger documentation configuration.
 *
 * <p>Accessible at:
 * <ul>
 *   <li>Swagger UI  → <a href="http://localhost:8080/swagger-ui/index.html">http://localhost:8080/swagger-ui/index.html</a></li>
 *   <li>Raw JSON    → <a href="http://localhost:8080/v3/api-docs">http://localhost:8080/v3/api-docs</a></li>
 * </ul>
 *
 * <p>Authentication scheme: HTTP Basic (matches Spring Security session-based auth).
 * Click "Authorize" in Swagger UI and enter {@code admin} / {@code admin123}.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI payrollOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(localServer()))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .components(new Components()
                        .addSecuritySchemes("basicAuth", basicAuthScheme()));
    }

    // ── API metadata ──────────────────────────────────────────────────────────

    private Info apiInfo() {
        return new Info()
                .title("SRMCEM Payroll Management System API")
                .description("""
                        REST API for the SRMCEM College Payroll Management System.

                        **Modules covered:**
                        - 🔐 Authentication (login / change password)
                        - 👤 Employee management (CRUD + search)
                        - 🏢 Department & Designation management
                        - 📅 Attendance tracking (mark / update / monthly report)
                        - 🏖️ Leave management (apply / approve / reject)
                        - 💰 Payroll (generate / view / monthly summary)
                        - 📄 Payslip PDF download (OpenPDF)
                        - 📊 Dashboard statistics

                        **Default credentials:** `admin` / `admin123`
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("SRMCEM Development Team")
                        .email("admin@payroll.com"))
                .license(new License()
                        .name("College Project — For Educational Use Only"));
    }

    // ── Server ────────────────────────────────────────────────────────────────

    private Server localServer() {
        return new Server()
                .url("http://localhost:8080")
                .description("Local Development Server");
    }

    // ── Security scheme ───────────────────────────────────────────────────────

    private SecurityScheme basicAuthScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Use admin credentials. Default: admin / admin123");
    }
}
