package org.example.classroom.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class UserReservationHistoryResponse {
    private String reservationId;
    private String classroomId;
    private String classroomName;
    private String buildingName;
    private String purpose;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;

    // 构造函数
    public UserReservationHistoryResponse() {}

    // Getter和Setter方法
    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName!=null?classroomName:"未知教室"; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getBuildingName() { return buildingName!=null?buildingName:"未知教学楼"; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) {
        this.status = status;
        this.statusText = getStatusText(status);
    }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // 状态文本转换
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已拒绝";
            case 3: return "已取消";
            case 4: return "已完成";
            default: return "未知";
        }
    }
}