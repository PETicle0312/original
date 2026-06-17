package com.example.demo.config;

import com.example.demo.game.service.PointService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class OpenApiPointInterceptor implements HandlerInterceptor {

    private final PointService pointService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 🚨 open-api로 시작하는 요청만 포인트 지급
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/open/")) {
            // Swagger에서 넘겨준 학번(userId) 헤더로 받기
            String userId = request.getHeader("x-user-id");

            if (userId != null && !userId.isBlank()) {
                try {
                    pointService.addPointForApiCall(userId, uri);
                } catch (Exception e) {
                    System.err.println("[OpenApiPointInterceptor] 포인트 적립 실패: " + e.getMessage());
                }
            }
        }

        return true;

    }
}
