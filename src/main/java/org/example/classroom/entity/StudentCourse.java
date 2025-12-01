package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("student_courses")
public class StudentCourse {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String studentId;
    private String courseId;
    private String collegeId;
    private String academicYear;
    private Integer semester;
    private Integer enrollmentStatus;
    private LocalDate enrollmentDate;
    private BigDecimal score;
    private Integer isAssigned; // 是否管理员分配
    private String assignedBy; // 分配人ID
    private LocalDateTime assignedAt; // 分配时间
    private String classId; // 分配班级ID（用于批量分配）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String collegeName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String className; // 班级名称

    @TableField(exist = false)
    private String courseCode; // 课程代码

    @TableField(exist = false)
    private Integer creditHours; // 学分

    @TableField(exist = false)
    private Integer courseType; // 课程类型

    // 构造函数
    public StudentCourse() {}

    public StudentCourse(String studentId, String courseId, String collegeId,
                         String academicYear, Integer semester) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.collegeId = collegeId;
        this.academicYear = academicYear;
        this.semester = semester;
        this.enrollmentStatus = 1; // 默认已选
        this.enrollmentDate = LocalDate.now();
    }

    // Getter和Setter方法
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public Integer getEnrollmentStatus() { return enrollmentStatus; }
    public void setEnrollmentStatus(Integer enrollmentStatus) { this.enrollmentStatus = enrollmentStatus; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getIsAssigned() {
        return isAssigned;
    }

    public void setIsAssigned(Integer isAssigned) {
        this.isAssigned = isAssigned;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    // 选课状态文本转换
    public String getEnrollmentStatusText() {
        if (enrollmentStatus == null) return "未知";
        switch (enrollmentStatus) {
            case 1: return "已选";
            case 2: return "退选";
            case 3: return "完成";
            default: return "未知";
        }
    }

    public String getIsAssignedText() {
        if (isAssigned == null) return "学生自选";
        return isAssigned == 1 ? "管理员分配" : "学生自选";
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public Integer getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(Integer creditHours) {
        this.creditHours = creditHours;
    }

    public Integer getCourseType() {
        return courseType;
    }

    public void setCourseType(Integer courseType) {
        this.courseType = courseType;
    }

    // 课程类型文本转换
    public String getCourseTypeText() {
        if (courseType == null) return "未知类型";
        switch (courseType) {
            case 0: return "必修课";
            case 1: return "选修课";
            case 2: return "实践课";
            default: return "未知类型";
        }
    }
}