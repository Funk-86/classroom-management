package org.example.classroom.dto;

import java.time.LocalDateTime;

public class CampusResponse {
    private String campusId;
    private String campusName;
    private String district;
    private String address;
    private Double longitude;
    private Double latitude;
    private Double distance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 默认构造函数
    public CampusResponse() {
    }

    // Getter 和 Setter 方法
    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString 方法
    @Override
    public String toString() {
        return "CampusResponse{" +
                "campusId='" + campusId + '\'' +
                ", campusName='" + campusName + '\'' +
                ", district='" + district + '\'' +
                ", address='" + address + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", distance=" + distance +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CampusResponse that = (CampusResponse) o;

        if (campusId != null ? !campusId.equals(that.campusId) : that.campusId != null) return false;
        if (campusName != null ? !campusName.equals(that.campusName) : that.campusName != null) return false;
        if (district != null ? !district.equals(that.district) : that.district != null) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null) return false;
        if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null) return false;
        if (distance != null ? !distance.equals(that.distance) : that.distance != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        return updatedAt != null ? updatedAt.equals(that.updatedAt) : that.updatedAt == null;
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        int result = campusId != null ? campusId.hashCode() : 0;
        result = 31 * result + (campusName != null ? campusName.hashCode() : 0);
        result = 31 * result + (district != null ? district.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (distance != null ? distance.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }
}