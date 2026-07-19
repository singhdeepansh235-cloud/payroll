package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.AdminResponse;
import com.srmcem.payroll.dto.ChangePasswordRequest;
import com.srmcem.payroll.dto.LoginRequest;

public interface AdminService {

    /**
     * Authenticates the admin with the given credentials.
     *
     * @return an {@link AdminResponse} with safe admin details (no password)
     * @throws com.srmcem.payroll.exception.BadRequestException if credentials are invalid
     */
    AdminResponse login(LoginRequest request);

    /**
     * Changes the password of the admin identified by {@code username}.
     *
     * @throws com.srmcem.payroll.exception.BadRequestException  if the current password is wrong
     *                                                            or new passwords don't match
     * @throws com.srmcem.payroll.exception.ResourceNotFoundException if admin is not found
     */
    void changePassword(String username, ChangePasswordRequest request);
}
