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
    private String studentNumber; // 学号
    private String teacherNumber; // 教师工号
    private String realName;      // 真实姓名
    private Integer gender;       // 性别

    // 构造函数
    public UserResponse() {}

    public UserResponse(User user) {
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.userPhone = user.getUserPhone();
        this.userRole = user.getUserRole();
        // 统一默认头像路径为小程序内置的 /pages/static/user_image/user.png
        this.userAvatar = user.getUserAvatar() != null ? user.getUserAvatar() : "/pages/static/user_image/user.png";
        this.roleName = getRoleName(user.getUserRole());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.collegeId = user.getCollegeId();
        this.classId = user.getClassId();
        this.collegeName = user.getCollegeName();
        this.className = user.getClassName();
        this.realName = user.getRealName();
        this.gender = user.getGender();

        // 学号：使用用户编号的后6位
        this.studentNumber = extractFromUserId(user.getUserId(), 6);

        // 工号：使用用户编号的后4位
        this.teacherNumber = extractFromUserId(user.getUserId(), 4);
    }

    /**
     * 从userId中提取后N位
     * @param userId 用户ID
     * @param length 提取的长度（学号6位，工号4位）
     * @return 提取的编号
     */
    private String extractFromUserId(String userId, int length) {
        if (userId == null || userId.length() == 0) {
            return null;
        }
        // 提取userId中的数字部分
        String digits = userId.replaceAll("[^0-9]", "");
        if (digits.length() == 0) {
            // 如果没有数字，直接使用userId的后N位
            if (userId.length() >= length) {
                return userId.substring(userId.length() - length);
            } else {
                // 如果userId长度不足，前面补0
                return String.format("%0" + length + "s", userId);
            }
        }
        // 如果有数字，取数字的后N位
        if (digits.length() >= length) {
            return digits.substring(digits.length() - length);
        } else {
            // 如果数字不足N位，前面补0
            return String.format("%0" + length + "d", Integer.parseInt(digits));
        }
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

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getTeacherNumber() { return teacherNumber; }
    public void setTeacherNumber(String teacherNumber) { this.teacherNumber = teacherNumber; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public Integer getGender() { return gender; }
    public void setGender(Integer gender) { this.gender = gender; }

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