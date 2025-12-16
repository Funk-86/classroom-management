package org.example.classroom.dto;

import org.example.classroom.entity.User;

import java.time.LocalDateTime;

public class UserResponse {
    private String userId;
    private String userName;
    private String userPhone;
    private Integer userRole;
    private String userAvatar;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String collegeId; // 新增学院ID
    private String collegeName; // 新增学院名称
    private String classId; // 新增班级ID
    private String className; // 新增班级名称

    // 构造函数
    public UserResponse() {}

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userPhone = user.getUserPhone();
        this.userRole = user.getUserRole();
        this.userAvatar = user.getUserAvatar() != null ? user.getUserAvatar() : "/static/avatars/default.png";
        this.roleName = getRoleName(user.getUserRole());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.collegeId = user.getCollegeId();
        this.classId = user.getClassId();
        this.collegeName = user.getCollegeName();
        this.className = user.getClassName();
    }

    // Getter和Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public Integer getUserRole() { return userRole; }
    public void setUserRole(Integer userRole) {
        this.userRole = userRole;
        this.roleName = getRoleName(userRole);
    }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    // 角色名称转换
    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 0: return "学生";
            case 1: return "教师";
            case 2: return "管理员";
            default: return "未知";
        }
    }

    // toString方法
    @Override
    public String toString() {
        return "UserResponse{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userRole=" + userRole +
                ", userAvatar='" + userAvatar + '\'' +
                ", roleName='" + roleName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}