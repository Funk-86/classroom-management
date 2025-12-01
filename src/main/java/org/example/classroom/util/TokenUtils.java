package org.example.classroom.util;

import org.example.classroom.dto.LoginResponse;
import org.example.classroom.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenUtils {

    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    @Autowired
    private UserService userService;

    public String extractUserIdFromToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("token不能为空");
        }

        // 处理Bearer token格式
        if (token.startsWith("Bearer ")) {
            String sessionId = token.substring(7);
            log.debug("解析的sessionId: {}", sessionId);

            // 使用UserService验证session
            LoginResponse response = userService.validateToken(sessionId);
            if (response.isSuccess()) {
                return response.getUserInfo().getUserId();
            } else {
                throw new IllegalArgumentException("Session验证失败: " + response.getMessage());
            }
        }

        // 如果不是Bearer格式，直接当作sessionId处理
        log.debug("直接使用token作为sessionId: {}", token);
        LoginResponse response = userService.validateToken(token);
        if (response.isSuccess()) {
            return response.getUserInfo().getUserId();
        }

        throw new IllegalArgumentException("无效的token格式");
    }
}

