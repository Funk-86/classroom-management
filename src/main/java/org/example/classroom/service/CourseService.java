package org.example.classroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.controller.CourseController;
import org.example.classroom.entity.Course;
import org.example.classroom.entity.CourseSchedule;
import org.example.classroom.entity.StudentCourse;
import org.example.classroom.util.WeekCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CourseService extends IService<Course> {

    // 课程相关方法
    IPage<Course> getCoursesWithDetail(String collegeId, String academicYear, Integer semester,
                                       Integer courseType, String teacherId, String studentId,
                                       Integer enrollmentStatus, Integer isRequired, String keyword,
                                       Integer page, Integer size);

    Course getCourseWithDetail(String courseId);

    List<Course> getCoursesByTeacher(String teacherId, String academicYear, Integer semester);

    boolean addCourse(Course course);

    boolean updateCourse(Course course);

    boolean deleteCourse(String courseId);

    // 课程安排相关方法
    IPage<CourseSchedule> getSchedulesWithDetail(String courseId, String classroomId, String campusId,
                                                 Integer dayOfWeek, Integer scheduleType,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Integer page, Integer size);

    CourseSchedule getScheduleWithDetail(String scheduleId);

    boolean addCourseSchedule(CourseSchedule schedule);

    boolean updateCourseSchedule(CourseSchedule schedule);

    boolean deleteCourseSchedule(String scheduleId);

    List<CourseSchedule> getStudentTimetable(String studentId, LocalDate date);

    List<CourseSchedule> getTeacherTimetable(String teacherId, LocalDate date);

    boolean checkScheduleConflict(CourseSchedule schedule);

    IPage<StudentCourse> getStudentCoursesWithDetail(String studentId, String courseId, String collegeId,
                                                     String academicYear, Integer semester,
                                                     Integer enrollmentStatus, Integer page, Integer size);

    boolean enrollCourse(StudentCourse studentCourse);

    boolean withdrawCourse(String studentCourseId);

    boolean updateCourseScore(String studentCourseId, BigDecimal score);

    List<StudentCourse> getStudentCourseList(String studentId, String academicYear, Integer semester);

    // 获取学生的所有课程（包括班级课程和已选课程）
    List<Course> getAllStudentCourses(String studentId, String classId, String academicYear, Integer semester);

    boolean checkCourseEnrollment(String studentId, String courseId, String academicYear, Integer semester);

    // 为学生分配课程
    boolean assignCourseToStudent(String courseId, String studentId, Integer isRequired, String assignerId);

    // 批量分配课程
    int batchAssignCourses(List<CourseController.BatchAssignRequest> requests, String assignerId);

    // 获取学生可选课程（未选的选修课）
    List<Course> getAvailableCoursesForStudent(String studentId, String academicYear, Integer semester);

    // 获取学生必修课程
    List<Course> getRequiredCoursesForStudent(String studentId, String academicYear, Integer semester);

    // 检查学生是否可以选择某课程
    boolean canStudentEnrollCourse(String studentId, String courseId);

    // 新增：分配课程给班级（支持多班级）
    boolean assignCourseToClass(String courseId, String classId, String assignerId);

    // 新增：批量分配课程给多个班级
    boolean batchAssignCourseToClasses(String courseId, List<String> classIds, String assignerId);

    // 新增：移除课程的班级关联
    boolean removeCourseFromClass(String courseId, String classId);

    // 新增：获取课程的所有关联班级
    List<org.example.classroom.entity.CourseClass> getCourseClasses(String courseId);

    // 新增：获取班级的所有关联课程
    List<org.example.classroom.entity.CourseClass> getClassCourses(String classId);

    // 新增：根据周次获取学生课表
    List<CourseSchedule> getStudentTimetableByWeek(String studentId, int weekNumber);

    // 新增：根据周次获取教师课表
    List<CourseSchedule> getTeacherTimetableByWeek(String teacherId, int weekNumber);

    // 新增：根据周次获取班级课表
    List<CourseSchedule> getClassTimetableByWeek(String classId, int weekNumber);

    // 新增：根据周次获取教室课表
    List<CourseSchedule> getClassroomTimetableByWeek(String classroomId, int weekNumber);

    // 新增：获取当前周次
    int getCurrentWeek();

    // 新增：根据日期获取周次
    int getWeekNumber(LocalDate date);

    // 新增：获取学期信息
    WeekCalculator.AcademicYearSemester getCurrentAcademicYearSemester();

    // 新增：根据周次获取课程安排（通用方法）
    List<CourseSchedule> getSchedulesByWeek(String courseId, String classroomId,
                                            String campusId, int weekNumber);
}