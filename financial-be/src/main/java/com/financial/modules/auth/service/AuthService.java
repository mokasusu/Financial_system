package com.financial.modules.auth.service;

import com.financial.modules.auth.dto.*;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.user.dto.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    UserResponse getCurrentUser(UserPrincipal userPrincipal);

    AuthResponse processOAuth2Login(String email, String name, String picture);

    AuthResponse googleLogin(GoogleLoginRequest request);
}

