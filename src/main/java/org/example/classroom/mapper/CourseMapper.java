package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.Course;

import java.util.List;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {


    // 新增：根据班级获取课程列表
    @Select("SELECT c.* FROM courses c WHERE c.class_id = #{classId} " +
            "AND c.academic_year = #{academicYear} AND c.semester = #{semester}")
    List<Course> selectCoursesByClass(@Param("classId") String classId,
                                      @Param("academicYear") String academicYear,
                                      @Param("semester") Integer semester);

    @Select("<script>" +
            "SELECT c.*, col.college_name, u.user_name as teacher_name, " +
            "cl.class_name, b.building_name, camp.campus_name " +
            "FROM courses c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "LEFT JOIN classes cl ON c.class_id = cl.class_id " +
            "LEFT JOIN classrooms room ON c.classroom_id = room.classroom_id " +
            "LEFT JOIN buildings b ON room.building_id = b.building_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE 1=1 " +
            "<if test='collegeId != null and collegeId != \"\"'> AND c.college_id = #{collegeId} </if>" +
            "<if test='academicYear != null and academicYear != \"\"'> AND c.academic_year = #{academicYear} </if>" +
            "<if test='semester != null'> AND c.semester = #{semester} </if>" +
            "<if test='courseType != null'> AND c.course_type = #{courseType} </if>" +
            "<if test='teacherId != null and teacherId != \"\"'> AND c.teacher_id = #{teacherId} </if>" +
            "<if test='studentId != null and studentId != \"\"'> " +
            "AND c.course_id IN (SELECT course_id FROM student_courses WHERE student_id = #{studentId}) " +
            "</if>" +
            "<if test='enrollmentStatus != null'> AND c.enrollment_status = #{enrollmentStatus} </if>" +
            "<if test='isRequired != null'> AND c.is_required = #{isRequired} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "AND (c.course_name LIKE CONCAT('%', #{keyword}, '%') OR c.course_code LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY c.academic_year DESC, c.semester DESC, c.created_at DESC" +
            "</script>")
    IPage<Course> selectCoursesWithDetail(Page<Course> page,
                                          @Param("collegeId") String collegeId,
                                          @Param("academicYear") String academicYear,
                                          @Param("semester") Integer semester,
                                          @Param("courseType") Integer courseType,
                                          @Param("teacherId") String teacherId,
                                          @Param("studentId") String studentId,
                                          @Param("enrollmentStatus") Integer enrollmentStatus,
                                          @Param("isRequired") Integer isRequired,
                                          @Param("keyword") String keyword);

    @Select("SELECT c.*, col.college_name, u.user_name as teacher_name, " +
            "cl.class_name, b.building_name, camp.campus_name " +
            "FROM courses c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "LEFT JOIN classes cl ON c.class_id = cl.class_id " +
            "LEFT JOIN classrooms room ON c.classroom_id = room.classroom_id " +
            "LEFT JOIN buildings b ON room.building_id = b.building_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE c.course_id = #{courseId}")
    Course selectCourseWithDetail(String courseId);

    @Select("SELECT c.*, col.college_name, u.user_name as teacher_name " +
            "FROM courses c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN users u ON c.teacher_id = u.user_id " +
            "WHERE c.teacher_id = #{teacherId} " +
            "<if test='academicYear != null and academicYear != \"\"'> AND c.academic_year = #{academicYear} </if>" +
            "<if test='semester != null'> AND c.semester = #{semester} </if>" +
            "ORDER BY c.academic_year DESC, c.semester DESC")
    List<Course> selectCoursesByTeacher(@Param("teacherId") String teacherId,
                                        @Param("academicYear") String academicYear,
                                        @Param("semester") Integer semester);

    @Select("SELECT COUNT(*) FROM courses WHERE course_code = #{courseCode} " +
            "AND academic_year = #{academicYear} AND semester = #{semester}")
    int checkCourseExists(@Param("courseCode") String courseCode,
                          @Param("academicYear") String academicYear,
                          @Param("semester") Integer semester);

    @Select("<script>" +
            "SELECT c.* " +
            "FROM courses c " +
            "WHERE c.course_id NOT IN (" +
            "   SELECT sc.course_id FROM student_courses sc WHERE sc.student_id = #{studentId} " +
            "   AND sc.academic_year = #{academicYear} AND sc.semester = #{semester}" +
            ") " +
            "AND c.academic_year = #{academicYear} AND c.semester = #{semester} " +
            "AND c.course_type = 1 " + // 选修课
            "AND c.enrollment_status = 0 " + // 未选满
            "</script>")
    List<Course> selectAvailableCoursesForStudent(@Param("studentId") String studentId,
                                                  @Param("academicYear") String academicYear,
                                                  @Param("semester") Integer semester);

    @Select("<script>" +
            "SELECT c.* " +
            "FROM courses c " +
            "WHERE (c.is_required = 1 OR c.class_id IN (" +
            "   SELECT class_id FROM users WHERE user_id = #{studentId}" +
            ")) " +
            "AND c.academic_year = #{academicYear} AND c.semester = #{semester} " +
            "</script>")
    List<Course> selectRequiredCoursesForStudent(@Param("studentId") String studentId,
                                                 @Param("academicYear") String academicYear,
                                                 @Param("semester") Integer semester);
}