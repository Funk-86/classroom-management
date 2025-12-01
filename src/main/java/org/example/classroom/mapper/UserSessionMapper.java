package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.classroom.entity.UserSession;
import java.util.Date;
import java.util.List;

@Mapper
public interface UserSessionMapper extends BaseMapper<UserSession> {

    // 根据sessionId查询
    @Select("SELECT * FROM user_session WHERE session_id = #{sessionId}")
    UserSession selectBySessionId(@Param("sessionId") String sessionId);

    // 根据userId查询所有session
    @Select("SELECT * FROM user_session WHERE user_id = #{userId}")
    List<UserSession> selectByUserId(@Param("userId") String userId);

    // 查询过期session
    @Select("SELECT * FROM user_session WHERE expire_time < #{currentTime}")
    List<UserSession> selectExpiredSessions(@Param("currentTime") Date currentTime);

    // 删除过期session
    @Delete("DELETE FROM user_session WHERE expire_time < #{currentTime}")
    int deleteExpiredSessions(@Param("currentTime") Date currentTime);

    // 根据userId删除所有session
    @Delete("DELETE FROM user_session WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") String userId);

    // 更新最后访问时间和过期时间
    @Update("UPDATE user_session SET last_access_time = #{lastAccessTime}, expire_time = #{expireTime}, update_time = NOW() WHERE session_id = #{sessionId}")
    int updateAccessTime(@Param("sessionId") String sessionId,
                         @Param("lastAccessTime") Date lastAccessTime,
                         @Param("expireTime") Date expireTime);

    // 统计活跃session数量
    @Select("SELECT COUNT(*) FROM user_session WHERE expire_time > #{currentTime}")
    int countActiveSessions(@Param("currentTime") Date currentTime);
}