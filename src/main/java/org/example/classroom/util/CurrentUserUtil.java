package org.example.classroom.util;

import org.example.classroom.service.UserService;
import org.example.classroom.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CurrentUserUtil {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private UserService userService;

    /**
     * 从token中获取当前用户ID
     */
    public String getCurrentUserId(String token) {
        if (!StringUtils.hasText(token)) {
            throw new RuntimeException("未提供认证token");
        }

        String userId = tokenUtils.extractUserIdFromToken(token);

        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("token无效或已过期");
        }

        // 验证用户是否存在
        User currentUser = userService.getById(userId);
        if (currentUser == null) {
            throw new RuntimeException("用户不存在或已被删除");
        }

        return userId;
    }

    /**
     * 获取当前用户完整信息
     */
    public User getCurrentUser(String token) {
        String userId = getCurrentUserId(token);
        return userService.getById(userId);
    }

    /**
     * 验证当前用户是否为管理员
     */
    public boolean isAdmin(String token) {
        User currentUser = getCurrentUser(token);
        return currentUser != null && currentUser.getUserRole() == 2;
    }

    /**
     * 验证当前用户是否有权限操作目标用户
     */
    public boolean hasPermission(String token, String targetUserId) {
        String currentUserId = getCurrentUserId(token);
        User currentUser = userService.getById(currentUserId);

        // 管理员可以操作任何用户，普通用户只能操作自己
        return currentUser != null &&
                (currentUser.getUserRole() == 2 || currentUserId.equals(targetUserId));
    }
}

