package org.example.classroom.dto;

public class UserRequest {
    private String userName;
    private String userPhone;
    private String userAvatar;
    private Integer userRole;
    private String collegeId; // 学院ID字段
    private String classId;   // 新增班级ID字段
    private String password;

    // Getter和Setter
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public Integer getUserRole() { return userRole; }
    public void setUserRole(Integer userRole) { this.userRole = userRole; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // 手动验证方法
    public String validate() {
        if (userName == null || userName.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (userName.trim().length() < 2) {
            return "用户名至少需要2个字符";
        }
        if (userName.trim().length() > 20) {
            return "用户名不能超过20个字符";
        }
        if (userPhone != null && !userPhone.trim().isEmpty()) {
            if (!userPhone.matches("^1[3-9]\\d{9}$")) {
                return "手机号格式不正确";
            }
        }
        return null;
    }

    // 添加用户时的验证方法
    public String validateForAdd() {
        if (getUserName() == null || getUserName().trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (getUserName().trim().length() < 2 || getUserName().trim().length() > 20) {
            return "用户名长度必须在2-20个字符之间";
        }
        if (getUserRole() != null && (getUserRole() < 0 || getUserRole() > 2)) {
            return "角色参数无效（0-学生，1-教师，2-管理员）";
        }
        return null;
    }

    // toString方法
    @Override
    public String toString() {
        return "UserRequest{" +
                "userName='" + userName + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", userRole=" + userRole +
                ", collegeId='" + collegeId + '\'' +
                ", classId='" + classId + '\'' +
                '}';
    }
}