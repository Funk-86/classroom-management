package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("course_schedules")
public class CourseSchedule {
    @TableId(type = IdType.ASSIGN_ID)
    private String scheduleId;

    private String courseId;
    private String classroomId;
    private String campusId;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate scheduleDate;
    private Integer scheduleType;
    private Integer startWeek;
    private Integer endWeek;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String courseCode;

    @TableField(exist = false)
    private String classroomName;

    @TableField(exist = false)
    private String buildingName;

    @TableField(exist = false)
    private String campusName;

    @TableField(exist = false)
    private String teacherId; // 教师ID（从course关联获取）

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private Integer courseType; // 课程类型

    @TableField(exist = false)
    private Integer creditHours; // 学分

    // 构造函数
    public CourseSchedule() {}

    // Getter和Setter方法
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public Integer getScheduleType() { return scheduleType; }
    public void setScheduleType(Integer scheduleType) { this.scheduleType = scheduleType; }

    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }

    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getCampusName() { return campusName; }
    public void setCampusName(String campusName) { this.campusName = campusName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getCourseType() { return courseType; }
    public void setCourseType(Integer courseType) { this.courseType = courseType; }

    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

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

    // 安排类型文本转换
    public String getScheduleTypeText() {
        if (scheduleType == null) return "未知";
        switch (scheduleType) {
            case 0: return "每周重复";
            case 1: return "单次安排";
            default: return "未知";
        }
    }

    // 星期几文本转换
    public String getDayOfWeekText() {
        if (dayOfWeek == null) return "单次安排";
        String[] days = {"", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "未知";
    }
}