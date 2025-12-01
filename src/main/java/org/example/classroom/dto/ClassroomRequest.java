package org.example.classroom.dto;

public class ClassroomRequest {
    private String classroomName;
    private String buildingId;
    private Integer floorNum;
    private Integer capacity;
    private String equipment;
    private Integer status;

    // 构造函数
    public ClassroomRequest() {}

    // Getter和Setter
    public String getClassroomName() { return classroomName; }
    public void setClassroomName(String classroomName) { this.classroomName = classroomName; }

    public String getBuildingId() { return buildingId; }
    public void setBuildingId(String buildingId) { this.buildingId = buildingId; }

    public Integer getFloorNum() { return floorNum; }
    public void setFloorNum(Integer floorNum) { this.floorNum = floorNum; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    // 验证方法
    public String validate() {
        if (classroomName == null || classroomName.trim().isEmpty()) {
            return "教室名称不能为空";
        }
        if (buildingId == null || buildingId.trim().isEmpty()) {
            return "教学楼ID不能为空";
        }
        if (floorNum == null || floorNum < 1) {
            return "楼层号必须大于0";
        }
        if (capacity == null || capacity < 0) {
            return "容量不能为负数";
        }
        if (status == null || (status != 0 && status != 1)) {
            return "状态必须为0(空闲)或1(占用)";
        }
        return null;
    }
}