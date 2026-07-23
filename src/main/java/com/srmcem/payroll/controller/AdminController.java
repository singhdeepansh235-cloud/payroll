package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.AdminResponse;
import com.srmcem.payroll.dto.ChangePasswordRequest;
import com.srmcem.payroll.dto.LoginRequest;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for Admin authentication.
 *
 * <p>Base path: {@code /api/auth}
 *
 * <ul>
 *   <li>{@code POST /api/auth/login}           — authenticate and retrieve admin details</li>
 *   <li>{@code POST /api/auth/change-password} — change the logged-in admin's password</li>
 * </ul>
 *
 * <p>No JWT is used — Spring Security's built-in session mechanism handles state.
 * The {@code login} endpoint performs a manual credential check via {@link AdminService}
 * and returns admin details; Spring Security itself manages the session.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication Module", description = "Endpoints for Admin authentication and password management")
public class AdminController {

    private final AdminService adminService;

    // -----------------------------------------------------------------------
    // POST /api/auth/login
    // -----------------------------------------------------------------------

    /**
     * Validates credentials and returns the admin profile.
     *
     * <p>Spring Security's {@code UsernamePasswordAuthenticationFilter} is NOT
     * used here — we call the service directly so we can return our standard
     * {@link ApiResponse} envelope instead of Spring's default JSON.
     */
    @PostMapping("/login")
    @io.swagger.v3.oas.annotations.Operation(summary = "Admin Login", description = "Validates session credentials and returns the Admin profile.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AdminResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AdminResponse adminResponse = adminService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful.", adminResponse));
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/change-password
    // -----------------------------------------------------------------------

    /**
     * Changes the password for the currently authenticated admin.
     *
     * <p>Requires the caller to be authenticated (Spring Security enforces this
     * via the {@code anyRequest().authenticated()} rule in {@code SecurityConfig}).
     * The username is read from the {@link Authentication} principal — no path
     * variable needed.
     */
    @PostMapping("/change-password")
    @io.swagger.v3.oas.annotations.Operation(summary = "Change Password", description = "Changes the password for the currently authenticated Admin session.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid inputs or passwords mismatch"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - must be logged in")
    })
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        adminService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully."));
    }
}
