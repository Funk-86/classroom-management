package org.example.classroom.dto;

import org.springframework.util.StringUtils;
import java.time.LocalDate;

public class ClassRequest {
    private String classCode;
    private String className;
    private String collegeId;
    private String campusId;
    private String headTeacherId;
    private String grade;
    private String majorName;
    private Integer classType;
    private Integer status;
    private LocalDate startDate;
    private LocalDate endDate;

    // Getter和Setter
    public String getClassCode() { return classCode; }
    public void setClassCode(String classCode) { this.classCode = classCode; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public String getCampusId() { return campusId; }
    public void setCampusId(String campusId) { this.campusId = campusId; }

    public String getHeadTeacherId() { return headTeacherId; }
    public void setHeadTeacherId(String headTeacherId) { this.headTeacherId = headTeacherId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getMajorName() { return majorName; }
    public void setMajorName(String majorName) { this.majorName = majorName; }

    public Integer getClassType() { return classType; }
    public void setClassType(Integer classType) { this.classType = classType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // 验证方法
    public String validate() {
        if (!StringUtils.hasText(classCode)) {
            return "班级代码不能为空";
        }
        if (!StringUtils.hasText(className)) {
            return "班级名称不能为空";
        }
        if (!StringUtils.hasText(collegeId)) {
            return "学院ID不能为空";
        }
        if (!StringUtils.hasText(campusId)) {
            return "校区ID不能为空";
        }
        if (!StringUtils.hasText(grade)) {
            return "年级不能为空";
        }
        if (classType != null && (classType < 0 || classType > 2)) {
            return "班级类型无效（0-普通班，1-实验班，2-重点班）";
        }
        if (status != null && (status < 0 || status > 2)) {
            return "班级状态无效（1-正常，2-毕业，0-解散）";
        }
        return null;
    }

    @Override
    public String toString() {
        return "ClassRequest{" +
                "classCode='" + classCode + '\'' +
                ", className='" + className + '\'' +
                ", collegeId='" + collegeId + '\'' +
                ", campusId='" + campusId + '\'' +
                ", headTeacherId='" + headTeacherId + '\'' +
                ", grade='" + grade + '\'' +
                ", majorName='" + majorName + '\'' +
                ", classType=" + classType +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}