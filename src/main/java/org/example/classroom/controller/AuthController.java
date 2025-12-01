package org.example.classroom.controller;

import org.example.classroom.dto.LoginRequest;
import org.example.classroom.dto.LoginResponse;
import org.example.classroom.dto.R;
import org.example.classroom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R login(@jakarta.validation.Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.login(
                    loginRequest.getUsername(),
                    loginRequest.getPassword(),
                    loginRequest.getRole()
            );

            if (response.isSuccess()) {
                return R.ok().put("data", response);
            } else {
                return R.error(response.getMessage());
            }
        } catch (Exception e) {
            return R.error("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/validate")
    public R validateSession(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            LoginResponse response = userService.validateToken(sessionId);
            return response.isSuccess() ? R.ok().put("data", response) : R.error(response.getMessage());
        } catch (Exception e) {
            return R.error("Session验证失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public R logout(@RequestHeader("X-Session-ID") String sessionId) {
        try {
            boolean result = userService.logout(sessionId);
            return result ? R.ok("登出成功") : R.error("登出失败");
        } catch (Exception e) {
            return R.error("登出失败: " + e.getMessage());
        }
    }

    @GetMapping("/session/count")
    public R getActiveSessionCount() {
        try {
            int count = userService.getActiveSessionCount();
            return R.ok().put("activeSessions", count);
        } catch (Exception e) {
            return R.error("获取Session数量失败: " + e.getMessage());
        }
    }

    @PostMapping("/force-logout")
    public R forceLogout(@RequestParam String userId) {
        try {
            boolean result = userService.forceLogoutUser(userId);
            return result ? R.ok("强制下线成功") : R.error("强制下线失败或用户未登录");
        } catch (Exception e) {
            return R.error("强制下线失败: " + e.getMessage());
        }
    }
}