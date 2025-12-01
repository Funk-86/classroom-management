package org.example.classroom.dto;

import java.time.LocalDateTime;

public class ClassroomResponse {
    private String classroomId;
    private String classroomName;
    private String buildingId;
    private String buildingName;
    private String campusId;
    private String campusName;
    private Integer floorNum;
    private Integer capacity;
    private String equipment;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public ClassroomResponse() {}

    // Getter和Setter
    public String getClassroomId() { return classroomId; }
    public void setClassroomId(String classroomId) { this.classroomId = classroomId; }

    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getCampusName() { return campusName; }
    public void setCampusName(String campusName) { this.campusName = campusName; }

    public Integer getFloorNum() { return floorNum; }
    public void setFloorNum(Integer floorNum) { this.floorNum = floorNum; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) {
        this.status = status;
        this.statusText = getStatusText(status);
    }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 状态文本转换
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "空闲";
            case 1: return "占用";
            default: return "未知";
        }
    }
}