package com.financial.modules.auth.service;

import com.financial.common.exception.*;
import com.financial.modules.auth.dto.AuthResponse;
import com.financial.modules.auth.dto.LoginRequest;
import com.financial.modules.auth.dto.RefreshTokenRequest;
import com.financial.modules.auth.dto.RegisterRequest;
import com.financial.modules.auth.entity.RefreshToken;
import com.financial.modules.auth.repository.RefreshTokenRepository;
import com.financial.modules.auth.security.JwtTokenProvider;
import com.financial.modules.auth.security.TokenHashUtil;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.auth.service.impl.AuthServiceImpl;
import com.financial.modules.user.dto.UserResponse;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import com.financial.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthServiceImpl authService;

    private User sampleUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                "mock-google-client-id"
        );

        sampleUser = User.builder()
                .id(1L)
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("$2a$10$hashedPassword")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("Password@123")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("Password@123")
                .build();

        lenient().when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("mock.jwt.token");
        lenient().when(jwtTokenProvider.getJwtExpirationMs()).thenReturn(900000L);
        lenient().when(jwtTokenProvider.getRefreshExpirationMs()).thenReturn(604800000L);
    }

    @Test
    @DisplayName("register should save user and return tokens when valid")
    void register_Success() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("register should throw EmailAlreadyExistsException when email exists")
    void register_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login should return AuthResponse on valid credentials")
    void login_Success() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), sampleUser.getPassword())).thenReturn(true);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getUser().getEmail()).isEqualTo("john.doe@example.com");

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("login should throw InvalidCredentialsException on invalid password")
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), sampleUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    @DisplayName("login should throw AccountLockedException when account is LOCKED")
    void login_LockedAccount_ThrowsException() {
        sampleUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), sampleUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("Account is locked");
    }

    @Test
    @DisplayName("login should throw AccountInactiveException when account is INACTIVE")
    void login_InactiveAccount_ThrowsException() {
        sampleUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), sampleUser.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AccountInactiveException.class)
                .hasMessageContaining("Account is inactive");
    }

    @Test
    @DisplayName("refreshToken should rotate token and return new tokens")
    void refreshToken_Success() {
        String rawToken = "sample-raw-refresh-token";
        String tokenHash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .user(sampleUser)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

        AuthResponse response = authService.refreshToken(new RefreshTokenRequest(rawToken));

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mock.jwt.token");
        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("refreshToken should throw TokenRevokedException if token is revoked")
    void refreshToken_Revoked_ThrowsException() {
        String rawToken = "revoked-token";
        String tokenHash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(sampleUser)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(rawToken)))
                .isInstanceOf(TokenRevokedException.class)
                .hasMessageContaining("revoked");
    }

    @Test
    @DisplayName("refreshToken should throw TokenExpiredException if token is expired")
    void refreshToken_Expired_ThrowsException() {
        String rawToken = "expired-token";
        String tokenHash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(sampleUser)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest(rawToken)))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("logout should mark token as revoked")
    void logout_Success() {
        String rawToken = "logout-token";
        String tokenHash = TokenHashUtil.sha256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(sampleUser)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(refreshToken));

        authService.logout(new RefreshTokenRequest(rawToken));

        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    @DisplayName("getCurrentUser should return UserResponse")
    void getCurrentUser_Success() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse response = authService.getCurrentUser(principal);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("processOAuth2Login should create new GOOGLE user if not exists")
    void processOAuth2Login_NewUser_Success() {
        when(userRepository.findByEmail("google.user@example.com")).thenReturn(Optional.empty());

        User googleUser = User.builder()
                .id(2L)
                .fullName("Google User")
                .email("google.user@example.com")
                .provider(AuthProvider.GOOGLE)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(googleUser);

        AuthResponse response = authService.processOAuth2Login("google.user@example.com", "Google User", "https://avatar.png");

        assertThat(response).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("google.user@example.com");
        assertThat(response.getUser().getProvider()).isEqualTo(AuthProvider.GOOGLE);
    }
}

