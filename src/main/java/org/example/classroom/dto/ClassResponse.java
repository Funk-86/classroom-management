package org.example.classroom.dto;

import org.example.classroom.entity.Class;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClassResponse {
    private String classId;
    private String classCode;
    private String className;
    private String collegeId;
    private String collegeName;
    private String campusId;
    private String campusName;
    private String headTeacherId;
    private String headTeacherName;
    private String grade;
    private String majorName;
    private Integer classType;
    private String classTypeName;
    private Integer status;
    private String statusName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer studentCount; // 学生人数统计
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public ClassResponse() {}

    public ClassResponse(Class clazz) {
        this.classId = clazz.getClassId();
        this.classCode = clazz.getClassCode();
        this.className = clazz.getClassName();
        this.collegeId = clazz.getCollegeId();
        this.campusId = clazz.getCampusId();
        this.headTeacherId = clazz.getHeadTeacherId();
        this.grade = clazz.getGrade();
        this.majorName = clazz.getMajorName();
        this.classType = clazz.getClassType();
        this.status = clazz.getStatus();
        this.startDate = clazz.getStartDate();
        this.endDate = clazz.getEndDate();
        this.createdAt = clazz.getCreatedAt();
        this.updatedAt = clazz.getUpdatedAt();

        // 设置类型名称
        this.classTypeName = getClassTypeName(classType);
        this.statusName = getStatusName(status);
    }

    // Getter和Setter
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getCampusName() { return campusName; }
    public void setCampusName(String campusName) { this.campusName = campusName; }

    public String getHeadTeacherId() { return headTeacherId; }
    public void setHeadTeacherId(String headTeacherId) { this.headTeacherId = headTeacherId; }

    public String getHeadTeacherName() { return headTeacherName; }
    public void setHeadTeacherName(String headTeacherName) { this.headTeacherName = headTeacherName; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    public Integer getClassType() { return classType; }
    public void setClassType(Integer classType) {
        this.classType = classType;
        this.classTypeName = getClassTypeName(classType);
    }

    public String getClassTypeName() { return classTypeName; }
    public void setClassTypeName(String classTypeName) { this.classTypeName = classTypeName; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) {
        this.status = status;
        this.statusName = getStatusName(status);
    }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 类型名称转换
    private String getClassTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 0: return "普通班";
            case 1: return "实验班";
            case 2: return "重点班";
            default: return "未知";
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "解散";
            case 1: return "正常";
            case 2: return "毕业";
            default: return "未知";
        }
    }

    @Override
    public String toString() {
        return "ClassResponse{" +
                "classId='" + classId + '\'' +
                ", classCode='" + classCode + '\'' +
                ", className='" + className + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", collegeName='" + collegeName + '\'' +
                ", campusId='" + campusId + '\'' +
                ", campusName='" + campusName + '\'' +
                ", headTeacherId='" + headTeacherId + '\'' +
                ", headTeacherName='" + headTeacherName + '\'' +
                ", grade='" + grade + '\'' +
                ", majorName='" + majorName + '\'' +
                ", classType=" + classType +
                ", classTypeName='" + classTypeName + '\'' +
                ", status=" + status +
                ", statusName='" + statusName + '\'' +
                ", studentCount=" + studentCount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}