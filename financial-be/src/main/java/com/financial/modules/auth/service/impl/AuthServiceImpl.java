package com.financial.modules.auth.service.impl;

import com.financial.common.exception.*;
import com.financial.modules.auth.dto.*;
import com.financial.modules.auth.entity.RefreshToken;
import com.financial.modules.auth.repository.RefreshTokenRepository;
import com.financial.modules.auth.security.JwtTokenProvider;
import com.financial.modules.auth.security.TokenHashUtil;
import com.financial.modules.auth.security.UserPrincipal;
import com.financial.modules.auth.service.AuthService;
import com.financial.modules.user.dto.UserResponse;
import com.financial.modules.user.entity.User;
import com.financial.modules.user.enums.AuthProvider;
import com.financial.modules.user.enums.UserStatus;
import com.financial.modules.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final String googleClientId;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.googleClientId = googleClientId;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .build();

        User savedUser = userRepository.save(user);
        return generateAuthResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        validateUserStatus(user);

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = TokenHashUtil.sha256(request.getRefreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new TokenRevokedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        validateUserStatus(user);

        // Rotate token: revoke old token and issue new
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        if (request != null && StringUtils.hasText(request.getRefreshToken())) {
            String tokenHash = TokenHashUtil.sha256(request.getRefreshToken());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new InvalidTokenException("Unauthenticated user");
        }

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userPrincipal.getId()));

        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public AuthResponse processOAuth2Login(String email, String name, String picture) {
        if (!StringUtils.hasText(email)) {
            throw new InvalidTokenException("Email not provided by Google OAuth2");
        }

        User user = userRepository.findByEmail(email).map(existingUser -> {
            validateUserStatus(existingUser);
            if (existingUser.getAvatarUrl() == null && picture != null) {
                existingUser.setAvatarUrl(picture);
                return userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            String displayName = (StringUtils.hasText(name)) ? name : email;
            User newUser = User.builder()
                    .fullName(displayName)
                    .email(email)
                    .password(null)
                    .avatarUrl(picture)
                    .status(UserStatus.ACTIVE)
                    .provider(AuthProvider.GOOGLE)
                    .build();
            return userRepository.save(newUser);
        });

        return generateAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new InvalidTokenException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            return processOAuth2Login(email, name, picture);
        } catch (InvalidTokenException | AccountLockedException | AccountInactiveException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new InvalidTokenException("Failed to verify Google ID token: " + e.getMessage());
        }
    }

    private void validateUserStatus(User user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AccountLockedException("Account is locked. Please contact support.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new AccountInactiveException("Account is inactive. Please contact support.");
        }
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getFullName());
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = TokenHashUtil.sha256(rawRefreshToken);

        Instant expiryDate = Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getJwtExpirationMs() / 1000)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}

