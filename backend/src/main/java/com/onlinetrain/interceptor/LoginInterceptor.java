package com.onlinetrain.interceptor;

import com.onlinetrain.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器 - 检查JWT Token
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();

        // Swagger 路径放行
        if (requestURI.contains("/doc.html") ||
                requestURI.contains("/webjars/") ||
                requestURI.contains("/swagger-resources") ||
                requestURI.contains("/v2/api-docs") ||
                requestURI.contains("/v3/api-docs") ||
                requestURI.contains("/favicon.ico") ||
                requestURI.contains("/error")) {
            return true;
        }

        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            log.warn("未提供Token, URI: {}", requestURI);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
            return false;
        }

        try {
            // 验证并解析Token
            if (!jwtUtils.isTokenValid(token)) {
                log.warn("Token无效, URI: {}", requestURI);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效或已过期\"}");
                return false;
            }

            Long userId = jwtUtils.getUserId(token);
            String role = jwtUtils.getRole(token);

            // 设置用户信息到请求属性
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);

            log.debug("用户认证成功: userId={}, role={}, URI={}", userId, role, requestURI);
            return true;

        } catch (Exception e) {
            log.error("Token解析异常: {}", e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"认证失败\"}");
            return false;
        }
    }
}
