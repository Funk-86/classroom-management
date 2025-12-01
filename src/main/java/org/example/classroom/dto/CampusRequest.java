package org.example.classroom.dto;

public class CampusRequest {
    private Double latitude;
    private Double longitude;

    // 默认构造函数
    public CampusRequest() {
    }

    // 带参构造函数
    public CampusRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter 和 Setter 方法
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // 手动验证方法
    public String validate() {
        if (latitude == null) {
            return "纬度不能为空";
        }
        if (latitude < -90.0 || latitude > 90.0) {
            return "纬度必须在-90到90度之间";
        }
        if (longitude == null) {
            return "经度不能为空";
        }
        if (longitude < -180.0 || longitude > 180.0) {
            return "经度必须在-180到180度之间";
        }
        return null;
    }

    // toString 方法
    @Override
    public String toString() {
        return "CampusRequest{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}