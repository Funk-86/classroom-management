package org.example.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class LoginRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotNull(message = "角色不能为空")
    @Min(value = 0, message = "角色值必须在0-2之间")
    @Max(value = 2, message = "角色值必须在0-2之间")
    private Integer role; // 0-学生, 1-教师, 2-管理员

    // 构造函数
    public LoginRequest() {
    }

    public LoginRequest(String username, String password, Integer role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getter和Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }
}