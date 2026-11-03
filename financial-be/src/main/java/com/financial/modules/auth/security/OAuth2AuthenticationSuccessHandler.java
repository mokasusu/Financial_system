package com.financial.modules.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.common.response.ApiResponse;
import com.financial.modules.auth.dto.AuthResponse;
import com.financial.modules.auth.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String defaultRedirectUri;

    public OAuth2AuthenticationSuccessHandler(@Lazy AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            AuthResponse authResponse = authService.processOAuth2Login(email, name, picture);

            String targetUrl = request.getParameter("redirect_uri");
            if (targetUrl == null || targetUrl.isBlank()) {
                targetUrl = defaultRedirectUri;
            }

            if (targetUrl != null && !targetUrl.isBlank()) {
                String redirectWithTokens = UriComponentsBuilder.fromUriString(targetUrl)
                        .queryParam("accessToken", authResponse.getAccessToken())
                        .queryParam("refreshToken", authResponse.getRefreshToken())
                        .build().toUriString();
                getRedirectStrategy().sendRedirect(request, response, redirectWithTokens);
                return;
            }

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success("Google OAuth2 login successful", authResponse);
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        }
    }
}
