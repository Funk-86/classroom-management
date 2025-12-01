package org.example.classroom.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CourseScheduleRequest {
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

    // 构造函数
    public CourseScheduleRequest() {}

    // Getter和Setter方法
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

    // 验证方法
    public String validate() {
        if (courseId == null || courseId.trim().isEmpty()) {
            return "课程ID不能为空";
        }
        if (classroomId == null || classroomId.trim().isEmpty()) {
            return "教室ID不能为空";
        }
        if (campusId == null || campusId.trim().isEmpty()) {
            return "校区ID不能为空";
        }
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            return "时间设置无效，结束时间必须晚于开始时间";
        }
        if (scheduleType == null || (scheduleType != 0 && scheduleType != 1)) {
            return "安排类型必须为0(每周重复)或1(单次安排)";
        }

        if (scheduleType == 0) {
            // 每周重复安排验证
            if (dayOfWeek == null || dayOfWeek < 1 || dayOfWeek > 7) {
                return "星期几必须为1-7之间的数字";
            }
            if (startWeek == null || startWeek < 1 || startWeek > 20) {
                return "开始周次必须为1-20之间的数字";
            }
            if (endWeek == null || endWeek < 1 || endWeek > 20 || endWeek < startWeek) {
                return "结束周次必须大于等于开始周次且在1-20之间";
            }
        } else {
            // 单次安排验证
            if (scheduleDate == null) {
                return "单次安排必须指定具体日期";
            }
        }
        return null;
    }
}