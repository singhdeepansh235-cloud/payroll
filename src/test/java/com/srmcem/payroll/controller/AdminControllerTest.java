package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.AdminResponse;
import com.srmcem.payroll.dto.ChangePasswordRequest;
import com.srmcem.payroll.dto.LoginRequest;
import com.srmcem.payroll.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link AdminController} using MockMvc.
 */
@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Spring Security filter chain
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("POST /api/auth/login - Success")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        AdminResponse response = AdminResponse.builder()
                .id(1L)
                .username("admin")
                .name("Admin User")
                .email("admin@test.com")
                .build();

        when(adminService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful."))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.email").value("admin@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Validation Failure")
    void login_validationFailure() throws Exception {
        LoginRequest request = new LoginRequest(); // blank username and password

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/change-password - Success")
    void changePassword_success() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");
        request.setConfirmPassword("newPass");

        doNothing().when(adminService).changePassword(eq("admin"), any(ChangePasswordRequest.class));

        org.springframework.security.core.Authentication principal = 
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("admin", null, java.util.Collections.emptyList());

        mockMvc.perform(post("/api/auth/change-password")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully."));
    }
}

