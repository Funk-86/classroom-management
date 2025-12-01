package org.example.classroom.dto;

public class CourseRequest {
    private String courseCode;
    private String courseName;
    private String collegeId;
    private Integer creditHours;
    private Integer courseType;
    private String description;
    private String academicYear;
    private Integer semester;
    private String teacherId;

    // 构造函数
    public CourseRequest() {}

    // Getter和Setter方法
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getCollegeId() { return collegeId; }
    public void setCollegeId(String collegeId) { this.collegeId = collegeId; }

    public Integer getCreditHours() { return creditHours; }
    public void setCreditHours(Integer creditHours) { this.creditHours = creditHours; }

    public Integer getCourseType() { return courseType; }
    public void setCourseType(Integer courseType) { this.courseType = courseType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    // 验证方法
    public String validate() {
        if (courseCode == null || courseCode.trim().isEmpty()) {
            return "课程代码不能为空";
        }
        if (courseName == null || courseName.trim().isEmpty()) {
            return "课程名称不能为空";
        }
        if (collegeId == null || collegeId.trim().isEmpty()) {
            return "学院ID不能为空";
        }
        if (creditHours == null || creditHours < 1) {
            return "学分必须大于0";
        }
        if (courseType == null || (courseType != 0 && courseType != 1 && courseType != 2)) {
            return "课程类型必须为0(必修)、1(选修)或2(实践)";
        }
        if (academicYear == null || academicYear.trim().isEmpty()) {
            return "学年不能为空";
        }
        if (semester == null || (semester != 1 && semester != 2)) {
            return "学期必须为1(春季)或2(秋季)";
        }
        if (teacherId == null || teacherId.trim().isEmpty()) {
            return "教师ID不能为空";
        }
        return null;
    }
}