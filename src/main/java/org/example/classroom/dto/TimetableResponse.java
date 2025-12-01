package org.example.classroom.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimetableResponse {
    private String scheduleId;
    private String courseId;
    private String courseCode;
    private String courseName;
    private String classroomId;
    private String classroomName;
    private String buildingName;
    private String campusName;
    private Integer dayOfWeek;
    private String dayOfWeekText;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate scheduleDate;
    private Integer scheduleType;
    private String scheduleTypeText;
    private String teacherName;
    private Integer creditHours;
    private String courseType;
    private Integer startWeek;
    private Integer endWeek;
    private List<String> classNames; // 上课班级列表

    // 构造函数
    public TimetableResponse() {}

    // Getter和Setter方法
    public String getScheduleId() { return scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getCampusName() { return campusName; }
    public void setCampusName(String campusName) { this.campusName = campusName; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.dayOfWeekText = getDayOfWeekText(dayOfWeek);
    }

    public String getDayOfWeekText() { return dayOfWeekText; }
    public void setDayOfWeekText(String dayOfWeekText) { this.dayOfWeekText = dayOfWeekText; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public Integer getScheduleType() { return scheduleType; }
    public void setScheduleType(Integer scheduleType) {
        this.scheduleType = scheduleType;
        this.scheduleTypeText = getScheduleTypeText(scheduleType);
    }

    public String getScheduleTypeText() { return scheduleTypeText; }
    public void setScheduleTypeText(String scheduleTypeText) { this.scheduleTypeText = scheduleTypeText; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Integer getStartWeek() { return startWeek; }
    public void setStartWeek(Integer startWeek) { this.startWeek = startWeek; }

    public Integer getEndWeek() { return endWeek; }
    public void setEndWeek(Integer endWeek) { this.endWeek = endWeek; }

    public List<String> getClassNames() { return classNames; }
    public void setClassNames(List<String> classNames) { this.classNames = classNames; }

    // 工具方法
    private String getDayOfWeekText(Integer dayOfWeek) {
        if (dayOfWeek == null) return "单次安排";
        String[] days = {"", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : "未知";
    }

    private String getScheduleTypeText(Integer scheduleType) {
        if (scheduleType == null) return "未知";
        switch (scheduleType) {
            case 0: return "每周重复";
            case 1: return "单次安排";
            default: return "未知";
        }
    }

    // 获取时间段文本
    public String getTimeSlotText() {
        return startTime + " - " + endTime;
    }

    // 获取课程完整信息
    public String getCourseFullInfo() {
        return courseCode + " " + courseName + " (" + teacherName + ")";
    }
}