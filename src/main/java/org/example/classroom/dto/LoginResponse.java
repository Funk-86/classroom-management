package org.example.classroom.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private UserInfo userInfo;

    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponse(boolean success, String message, String token, UserInfo userInfo) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.userInfo = userInfo;
    }

    // Getter和Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public static class UserInfo {
        private String userId;
        private String userName;
        private Integer userRole;
        private String collegeId;
        private String classId;
        private String studentNumber; // 学号
        private String teacherNumber; // 教师工号
        private String realName;      // 真实姓名
        private Integer gender;       // 性别

        public UserInfo(String userId, String userName, Integer userRole, String collegeId, String classId) {
            this.userId = userId;
            this.userName = userName;
            this.userRole = userRole;
            this.collegeId = collegeId;
            this.classId = classId;
        }

        public UserInfo(String userId, String userName, Integer userRole, String collegeId, String classId,
                        String studentNumber, String teacherNumber, String realName, Integer gender) {
            this.userId = userId;
            this.userName = userName;
            this.userRole = userRole;
            this.collegeId = collegeId;
            this.classId = classId;
            this.studentNumber = studentNumber;
            this.teacherNumber = teacherNumber;
            this.realName = realName;
            this.gender = gender;
        }

        // Getter和Setter
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public Integer getUserRole() {
            return userRole;
        }

        public void setUserRole(Integer userRole) {
            this.userRole = userRole;
        }

        public String getCollegeId() { return collegeId; }
        public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

        public String getClassId() { return classId; }
        public void setClassId(String classId) { this.classId = classId; }

        public String getStudentNumber() { return studentNumber; }
        public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

        public String getTeacherNumber() { return teacherNumber; }
        public void setTeacherNumber(String teacherNumber) { this.teacherNumber = teacherNumber; }

        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }

        public Integer getGender() { return gender; }
        public void setGender(Integer gender) { this.gender = gender; }
    }
}