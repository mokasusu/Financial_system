package com.financial.modules.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Controller
public class OAuth2CallbackFallbackController {

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String defaultRedirectUri;

    @GetMapping(value = "/oauth2/callback")
    @ResponseBody
    public String handleOAuth2CallbackFallback(
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.warn("Direct request received at backend /oauth2/callback. Checking redirection target: {}", defaultRedirectUri);

        // Nếu defaultRedirectUri là một URL frontend hợp lệ (bắt đầu bằng http/https và không trỏ vào chính backend)
        String requestHost = request.getServerName();
        boolean isSelfRedirect = defaultRedirectUri.contains(requestHost) || !defaultRedirectUri.startsWith("http");

        if (!isSelfRedirect && accessToken != null && !accessToken.isBlank()) {
            String redirectUrl = UriComponentsBuilder.fromUriString(defaultRedirectUri)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken != null ? refreshToken : "")
                    .build().toUriString();
            response.sendRedirect(redirectUrl);
            return null;
        }

        response.setContentType(MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <title>Xác thực thành công</title>
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; background: #f8fafc; }
                        .card { max-width: 480px; padding: 32px; background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); border: 1px solid #e2e8f0; text-align: center; }
                        h2 { color: #0f172a; margin-top: 0; font-size: 20px; }
                        p { color: #475569; font-size: 14px; line-height: 1.6; }
                        .code { background: #f1f5f9; padding: 4px 8px; border-radius: 4px; font-family: monospace; font-size: 13px; color: #0284c7; }
                        .warning { background: #fef2f2; border: 1px solid #fecaca; color: #991b1b; padding: 12px; border-radius: 8px; font-size: 13px; margin: 16px 0; text-align: left; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <h2>🎉 Đăng nhập Google thành công!</h2>
                        <div class="warning">
                            <strong>⚠️ Lưu ý cấu hình tên miền:</strong><br/>
                            Địa chỉ chuyển hướng hiện tại đang trỏ về Backend (Render) thay vì Frontend (Vercel).
                        </div>
                        <p>Vui lòng cập nhật biến môi trường trên Render:</p>
                        <p><span class="code">APP_FRONTEND_URL = https://&lt;domain-vercel-cua-ban&gt;.vercel.app</span></p>
                    </div>
                </body>
                </html>
                """;
    }
}

