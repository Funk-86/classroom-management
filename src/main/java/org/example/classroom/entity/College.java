package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.Date;

@TableName("colleges")
public class College {
    @TableId(type = IdType.ASSIGN_ID)
    private String collegeId;

    private String collegeName;
    private String campusId;
    private String deanId;
    private String contactPhone;
    private String officeLocation;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // 关联字段（非数据库字段）
    @TableField(exist = false)
    private String campusName;

    @TableField(exist = false)
    private String deanName;

    // 构造函数
    public College() {}

    public College(String collegeName, String campusId, String contactPhone, String officeLocation) {
        this.collegeName = collegeName;
        this.campusId = campusId;
        this.contactPhone = contactPhone;
        this.officeLocation = officeLocation;
    }

    // Getter和Setter方法
    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getDeanId() { return deanId; }
    public void setDeanId(String deanId) { this.deanId = deanId; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCampusName() { return campusName; }
    public void setCampusName(String campusName) { this.campusName = campusName; }

    public String getDeanName() { return deanName; }
    public void setDeanName(String deanName) { this.deanName = deanName; }

    @Override
    public String toString() {
        return "College{" +
                "collegeId='" + collegeId + '\'' +
                ", collegeName='" + collegeName + '\'' +
                ", campusId='" + campusId + '\'' +
                ", deanId='" + deanId + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", officeLocation='" + officeLocation + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}