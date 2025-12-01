package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("classes")
public class Class {
    @TableId(type = IdType.ASSIGN_ID)
    private String classId;

    private String classCode;
    private String className;
    private String collegeId;
    private String campusId;
    private String headTeacherId;
    private String grade;
    private String majorName;
    private Integer classType;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 构造函数
    public Class() {}

    public Class(String classCode, String className, String collegeId, String campusId, String grade) {
        this.classCode = classCode;
        this.className = className;
        this.collegeId = collegeId;
        this.campusId = campusId;
        this.grade = grade;
    }

    // Getter和Setter方法
    public String getClassId() { return classId; }
    public void setClassId(String classId) { this.classId = classId; }

    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getHeadTeacherId() { return headTeacherId; }
    public void setHeadTeacherId(String headTeacherId) { this.headTeacherId = headTeacherId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    public Integer getClassType() { return classType; }
    public void setClassType(Integer classType) { this.classType = classType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Class{" +
                "classId='" + classId + '\'' +
                ", classCode='" + classCode + '\'' +
                ", className='" + className + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", campusId='" + campusId + '\'' +
                ", headTeacherId='" + headTeacherId + '\'' +
                ", grade='" + grade + '\'' +
                ", majorName='" + majorName + '\'' +
                ", classType=" + classType +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}