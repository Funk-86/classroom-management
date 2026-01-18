package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.classroom.entity.Class;
import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {

    // 根据班级代码查询
    @Select("SELECT * FROM classes WHERE class_code = #{classCode}")
    Class selectByClassCode(@Param("classCode") String classCode);

    // 查询所有班级（包含学院和校区信息）
    @Select("SELECT c.*, col.college_name, camp.campus_name, u.user_name as head_teacher_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.head_teacher_id = u.user_id " +
            "ORDER BY c.created_at DESC")
    List<Class> selectAllClassesWithInfo();

    // 分页查询班级
    @Select("SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{pageSize}")
    List<Class> selectClassesByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    // 根据学院查询班级
    @Select("SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE c.college_id = #{collegeId} " +
            "ORDER BY c.created_at DESC")
    List<Class> selectClassesByCollege(@Param("collegeId") String collegeId);

    // 根据校区查询班级
    @Select("SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE c.campus_id = #{campusId} " +
            "ORDER BY c.created_at DESC")
    List<Class> selectClassesByCampus(@Param("campusId") String campusId);

    // 搜索班级
    @Select("SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE c.class_name LIKE CONCAT('%', #{keyword}, '%') OR c.class_code LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY c.created_at DESC")
    List<Class> searchClasses(@Param("keyword") String keyword);

    // 分页搜索班级
    @Select("SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE c.class_name LIKE CONCAT('%', #{keyword}, '%') OR c.class_code LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY c.created_at DESC LIMIT #{offset}, #{pageSize}")
    List<Class> searchClassesByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 统计搜索数量
    @Select("SELECT COUNT(*) FROM classes WHERE class_name LIKE CONCAT('%', #{keyword}, '%') OR class_code LIKE CONCAT('%', #{keyword}, '%')")
    long countByKeyword(@Param("keyword") String keyword);

    // 统计班级学生人数
    @Select("SELECT COUNT(*) FROM users WHERE class_id = #{classId} AND user_role = 0")
    int countStudentsByClass(@Param("classId") String classId);

    // 查询班级详情（包含完整信息）
    @Select("SELECT c.*, col.college_name, camp.campus_name, u.user_name as head_teacher_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "LEFT JOIN users u ON c.head_teacher_id = u.user_id " +
            "WHERE c.class_id = #{classId}")
    Class selectClassDetail(@Param("classId") String classId);

    @Select("<script>" +
            "SELECT c.*, col.college_name, camp.campus_name " +
            "FROM classes c " +
            "LEFT JOIN colleges col ON c.college_id = col.college_id " +
            "LEFT JOIN campuses camp ON c.campus_id = camp.campus_id " +
            "WHERE 1=1 " +
            "<if test='collegeId != null and collegeId != \"\"'>" +
            "   AND c.college_id = #{collegeId} " +
            "</if>" +
            "<if test='campusId != null and campusId != \"\"'>" +
            "   AND c.campus_id = #{campusId} " +
            "</if>" +
            "ORDER BY c.created_at DESC" +
            "</script>")
    List<Class> selectClassesByCollegeAndCampus(@Param("collegeId") String collegeId, @Param("campusId") String campusId);
}