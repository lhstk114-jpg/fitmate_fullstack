package org.spring.backend.member.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenValidationService {
    String validateRefreshAndGetEmail(HttpServletRequest request, HttpServletResponse response);

    void deleteRefreshCookie(HttpServletResponse response);
}
