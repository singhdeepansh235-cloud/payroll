package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.AdminResponse;
import com.srmcem.payroll.dto.ChangePasswordRequest;
import com.srmcem.payroll.dto.LoginRequest;
import com.srmcem.payroll.entity.Admin;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.AdminRepository;
import com.srmcem.payroll.service.impl.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminServiceImpl}.
 *
 * Strategy:
 * - All dependencies are Mockito mocks; no Spring context is loaded.
 * - Each test covers one logical path (happy path or error path).
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Admin sampleAdmin;

    @BeforeEach
    void setUp() {
        sampleAdmin = Admin.builder()
                .id(1L)
                .username("admin")
                .password("$2a$10$hashedPassword")
                .name("System Admin")
                .email("admin@company.com")
                .build();
    }

    // -----------------------------------------------------------------------
    // login()
    // -----------------------------------------------------------------------

    /**
     * TC-ADMIN-01: Successful login returns AdminResponse with correct fields.
     * Verifies the happy path: username found in DB and password matches.
     */
    @Test
    @DisplayName("TC-ADMIN-01: login() — valid credentials return AdminResponse")
    void login_validCredentials_returnsAdminResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));
        when(passwordEncoder.matches("admin123", sampleAdmin.getPassword())).thenReturn(true);
        doNothing().when(auditLogService).log(anyString(), anyString(), anyString());

        // Act
        AdminResponse response = adminService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getName()).isEqualTo("System Admin");
        assertThat(response.getEmail()).isEqualTo("admin@company.com");

        verify(adminRepository).findByUsername("admin");
        verify(passwordEncoder).matches("admin123", sampleAdmin.getPassword());
    }

    /**
     * TC-ADMIN-02: login() throws BadRequestException when username is not found.
     * Ensures an invalid username is rejected with the correct error message.
     */
    @Test
    @DisplayName("TC-ADMIN-02: login() — unknown username throws BadRequestException")
    void login_unknownUsername_throwsBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("pass");

        when(adminRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid username or password.");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    /**
     * TC-ADMIN-03: login() throws BadRequestException when password does not match.
     * Ensures a wrong password is rejected even when the username is valid.
     */
    @Test
    @DisplayName("TC-ADMIN-03: login() — wrong password throws BadRequestException")
    void login_wrongPassword_throwsBadRequestException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongPass");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));
        when(passwordEncoder.matches("wrongPass", sampleAdmin.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> adminService.login(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid username or password.");
    }

    // -----------------------------------------------------------------------
    // changePassword()
    // -----------------------------------------------------------------------

    /**
     * TC-ADMIN-04: Successful password change saves the new encoded password.
     * Verifies the happy path: current password matches and new passwords agree.
     */
    @Test
    @DisplayName("TC-ADMIN-04: changePassword() — valid request saves encoded password")
    void changePassword_validRequest_savesNewPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass123");
        request.setConfirmPassword("newPass123");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));
        when(passwordEncoder.matches("oldPass", sampleAdmin.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$10$newHashedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(sampleAdmin);

        // Act
        adminService.changePassword("admin", request);

        // Assert
        verify(adminRepository).save(argThat(a -> a.getPassword().equals("$2a$10$newHashedPassword")));
    }

    /**
     * TC-ADMIN-05: changePassword() throws ResourceNotFoundException when admin username is not found.
     */
    @Test
    @DisplayName("TC-ADMIN-05: changePassword() — unknown username throws ResourceNotFoundException")
    void changePassword_unknownUsername_throwsResourceNotFoundException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");
        request.setConfirmPassword("new");

        when(adminRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> adminService.changePassword("ghost", request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(adminRepository, never()).save(any());
    }

    /**
     * TC-ADMIN-06: changePassword() throws BadRequestException when current password is incorrect.
     */
    @Test
    @DisplayName("TC-ADMIN-06: changePassword() — incorrect current password throws BadRequestException")
    void changePassword_incorrectCurrentPassword_throwsBadRequestException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrongCurrent");
        request.setNewPassword("new123");
        request.setConfirmPassword("new123");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));
        when(passwordEncoder.matches("wrongCurrent", sampleAdmin.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> adminService.changePassword("admin", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect.");

        verify(adminRepository, never()).save(any());
    }

    /**
     * TC-ADMIN-07: changePassword() throws BadRequestException when new and confirm passwords do not match.
     */
    @Test
    @DisplayName("TC-ADMIN-07: changePassword() — password mismatch throws BadRequestException")
    void changePassword_passwordsMismatch_throwsBadRequestException() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass1");
        request.setConfirmPassword("newPass2");

        when(adminRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));
        when(passwordEncoder.matches("oldPass", sampleAdmin.getPassword())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> adminService.changePassword("admin", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("do not match");

        verify(adminRepository, never()).save(any());
    }
}
