package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

@TableName("courses")
public class Course {
    @TableId(type = IdType.ASSIGN_ID)
    private String courseId;

    private String courseCode;
    private String courseName;
    private String collegeId;
    private Integer creditHours;
    private Integer courseType;
    private String description;
    private String academicYear;
    private Integer semester;
    private String teacherId;
    private String campusId;
    private String classroomId;
    private String classId;
    private String studentId; // 学生ID，可为空
    private Integer enrollmentStatus; // 选课状态
    private Integer isRequired; // 是否必修
    private String assignedBy; // 分配人ID
    private LocalDateTime assignedAt; // 分配时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String collegeName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String className; // 班级名称

    @TableField(exist = false)
    private String classCode; // 新增：班级代码


    // 构造函数
    public Course() {}

    public Course(String courseCode, String courseName, String collegeId, Integer creditHours,
                  Integer courseType, String academicYear, Integer semester, String teacherId,
                  String studentId, Integer isRequired){
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.collegeId = collegeId;
        this.creditHours = creditHours;
        this.courseType = courseType;
        this.academicYear = academicYear;
        this.semester = semester;
        this.teacherId = teacherId;
        this.studentId = studentId;
        this.isRequired = isRequired;
        this.enrollmentStatus = studentId != null ? 1 : 0; // 如果有学生ID，状态设为已选
    }

    // Getter和Setter方法
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

    public Integer getCourseType() { return courseType; }
    public void setCourseType(Integer courseType) { this.courseType = courseType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Integer getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(Integer enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
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

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    // 选课状态文本转换
    public String getEnrollmentStatusText() {
        if (enrollmentStatus == null) return "未知";
        switch (enrollmentStatus) {
            case 0: return "未选";
            case 1: return "已选";
            case 2: return "退选";
            case 3: return "完成";
            default: return "未知";
        }
    }

    // 课程类型文本转换
    public String getCourseTypeText() {
        if (courseType == null) return "未知";
        switch (courseType) {
            case 0: return "必修";
            case 1: return "选修";
            case 2: return "实践";
            default: return "未知";
        }
    }

    // 是否必修文本转换
    public String getIsRequiredText() {
        if (isRequired == null) return "选修";
        return isRequired == 1 ? "必修" : "选修";
    }

    // 学期文本转换
    public String getSemesterText() {
        if (semester == null) return "未知";
        switch (semester) {
            case 1: return "春季学期";
            case 2: return "秋季学期";
            default: return "未知";
        }
    }
}