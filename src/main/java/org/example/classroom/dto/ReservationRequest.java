package org.example.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationRequest {
    @NotBlank(message = "教室ID不能为空")
    private String classroomId;
    
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    @NotBlank(message = "预约用途不能为空")
    private String purpose;
    
    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "不能预约过去的日期")
    private LocalDate date;
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    // 构造函数
    public ReservationRequest() {}

    public ReservationRequest(String classroomId, String userId, String purpose,
                              LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.classroomId = classroomId;
        this.userId = userId;
        this.purpose = purpose;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getter和Setter
    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    // 验证方法
    public String validate() {
        if (classroomId == null || classroomId.trim().isEmpty()) {
            return "教室ID不能为空";
        }
        if (userId == null || userId.trim().isEmpty()) {
            return "用户ID不能为空";
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            return "预约用途不能为空";
        }
        if (date == null) {
            return "预约日期不能为空";
        }
        if (startTime == null) {
            return "开始时间不能为空";
        }
        if (endTime == null) {
            return "结束时间不能为空";
        }
        if (!endTime.isAfter(startTime)) {
            return "结束时间必须晚于开始时间";
        }
        if (date.isBefore(LocalDate.now())) {
            return "不能预约过去的日期";
        }
        return null;
    }
}