package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.classroom.entity.User;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM users WHERE user_name = #{username} AND user_role = #{role}")
    User selectByUsernameAndRole(@Param("username") String username, @Param("role") Integer role);

    @Select("SELECT * FROM users WHERE user_name = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("SELECT * FROM users ORDER BY created_at DESC")
    List<User> selectAllUsers();

    @Select("SELECT * FROM users ORDER BY created_at DESC LIMIT #{offset}, #{pageSize}")
    List<User> selectUsersByPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT * FROM users WHERE user_role = #{role} ORDER BY created_at DESC")
    List<User> selectUsersByRole(@Param("role") Integer role);

    // 新增的查询方法（移除状态相关方法）
    @Select("SELECT * FROM users WHERE user_name LIKE CONCAT('%', #{keyword}, '%') OR user_id LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_at DESC")
    List<User> searchUsers(@Param("keyword") String keyword);

    @Select("SELECT * FROM users WHERE user_name LIKE CONCAT('%', #{keyword}, '%') OR user_id LIKE CONCAT('%', #{keyword}, '%') ORDER BY created_at DESC LIMIT #{offset}, #{pageSize}")
    List<User> searchUsersByPage(@Param("keyword") String keyword, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM users WHERE user_name LIKE CONCAT('%', #{keyword}, '%') OR user_id LIKE CONCAT('%', #{keyword}, '%')")
    long countByKeyword(@Param("keyword") String keyword);

    @Update("UPDATE users SET password = #{password} WHERE user_id = #{userId}")
    int updatePassword(@Param("userId") String userId, @Param("password") String password);

    // 新增：根据学院查询用户
    @Select("SELECT * FROM users WHERE college_id = #{collegeId} ORDER BY created_at DESC")
    List<User> selectUsersByCollege(@Param("collegeId") String collegeId);

    // 新增：根据学院和角色查询用户
    @Select("SELECT * FROM users WHERE college_id = #{collegeId} AND user_role = #{role} ORDER BY created_at DESC")
    List<User> selectUsersByCollegeAndRole(@Param("collegeId") String collegeId, @Param("role") Integer role);

    // 新增：根据班级查询用户
    @Select("SELECT * FROM users WHERE class_id = #{classId} ORDER BY created_at DESC")
    List<User> selectUsersByClass(@Param("classId") String classId);

    // 新增：根据班级和角色查询用户
    @Select("SELECT * FROM users WHERE class_id = #{classId} AND user_role = #{role} ORDER BY created_at DESC")
    List<User> selectUsersByClassAndRole(@Param("classId") String classId, @Param("role") Integer role);

}