package org.example.classroom.dto;

import java.time.LocalDate;

public class TimeConflictRequest {
    private String classroomId;
    private LocalDate date;
    private String startTime;
    private String endTime;

    // 构造函数
    public TimeConflictRequest() {}

    public TimeConflictRequest(String classroomId, LocalDate date, String startTime, String endTime) {
        this.classroomId = classroomId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter和Setter
    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    // 验证方法
    public String validate() {
        if (classroomId == null || classroomId.trim().isEmpty()) {
            return "教室ID不能为空";
        }
        if (date == null) {
            return "日期不能为空";
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            return "开始时间不能为空";
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            return "结束时间不能为空";
        }
        return null;
    }
}