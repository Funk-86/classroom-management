package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.StudentCourse;

import java.util.List;

@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

    @Select("SELECT sc.*, u.user_name as student_name, c.course_name, " +
            "col.college_name, u_teacher.user_name as teacher_name " +
            "FROM student_courses sc " +
            "LEFT JOIN users u ON sc.student_id = u.user_id " +
            "LEFT JOIN courses c ON sc.course_id = c.course_id " +
            "LEFT JOIN colleges col ON sc.college_id = col.college_id " +
            "LEFT JOIN users u_teacher ON c.teacher_id = u_teacher.user_id " +
            "WHERE sc.id = #{id}")
    StudentCourse selectStudentCourseWithDetail(String id);

    @Select("<script>" +
            "SELECT sc.*, u.user_name as student_name, c.course_name, " +
            "col.college_name, u_teacher.user_name as teacher_name " +
            "FROM student_courses sc " +
            "LEFT JOIN users u ON sc.student_id = u.user_id " +
            "LEFT JOIN courses c ON sc.course_id = c.course_id " +
            "LEFT JOIN colleges col ON sc.college_id = col.college_id " +
            "LEFT JOIN users u_teacher ON c.teacher_id = u_teacher.user_id " +
            "WHERE 1=1 " +
            "<if test='studentId != null and studentId != \"\"'> AND sc.student_id = #{studentId} </if>" +
            "<if test='courseId != null and courseId != \"\"'> AND sc.course_id = #{courseId} </if>" +
            "<if test='collegeId != null and collegeId != \"\"'> AND sc.college_id = #{collegeId} </if>" +
            "<if test='academicYear != null and academicYear != \"\"'> AND sc.academic_year = #{academicYear} </if>" +
            "<if test='semester != null'> AND sc.semester = #{semester} </if>" +
            "<if test='enrollmentStatus != null'> AND sc.enrollment_status = #{enrollmentStatus} </if>" +
            "ORDER BY sc.academic_year DESC, sc.semester DESC, sc.created_at DESC" +
            "</script>")
    IPage<StudentCourse> selectStudentCoursesWithDetail(Page<StudentCourse> page,
                                                        @Param("studentId") String studentId,
                                                        @Param("courseId") String courseId,
                                                        @Param("collegeId") String collegeId,
                                                        @Param("academicYear") String academicYear,
                                                        @Param("semester") Integer semester,
                                                        @Param("enrollmentStatus") Integer enrollmentStatus);

    @Select("SELECT COUNT(*) FROM student_courses " +
            "WHERE student_id = #{studentId} AND course_id = #{courseId} " +
            "AND academic_year = #{academicYear} AND semester = #{semester} " +
            "AND enrollment_status = 1")
    int checkCourseEnrollment(@Param("studentId") String studentId,
                              @Param("courseId") String courseId,
                              @Param("academicYear") String academicYear,
                              @Param("semester") Integer semester);

    @Select("<script>" +
            "SELECT sc.*, c.course_name, c.course_code, c.credit_hours, c.course_type, " +
            "u_teacher.user_name as teacher_name " +
            "FROM student_courses sc " +
            "JOIN courses c ON sc.course_id = c.course_id " +
            "LEFT JOIN users u_teacher ON c.teacher_id = u_teacher.user_id " +
            "WHERE sc.student_id = #{studentId} AND sc.enrollment_status = 1 " +
            "<if test='academicYear != null and academicYear != \"\"'> " +
            "AND sc.academic_year = #{academicYear} " +
            "</if>" +
            "<if test='semester != null'> " +
            "AND sc.semester = #{semester} " +
            "</if>" +
            "ORDER BY sc.academic_year DESC, sc.semester DESC" +
            "</script>")
    List<StudentCourse> selectStudentCourses(@Param("studentId") String studentId,
                                             @Param("academicYear") String academicYear,
                                             @Param("semester") Integer semester);


}