package org.example.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.classroom.entity.Announcement;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    // 查询有效公告（当前时间在生效和失效时间之间）
    @Select("SELECT a.*, u.user_name as admin_name FROM announcements a " +
            "LEFT JOIN users u ON a.admin_id = u.user_id " +
            "WHERE a.start_time <= NOW() AND a.end_time >= NOW() " +
            "ORDER BY a.priority DESC, a.created_at DESC")
    IPage<Announcement> selectActiveAnnouncements(Page<Announcement> page);

    // 查询最新公告（限制数量）
    @Select("SELECT a.*, u.user_name as admin_name FROM announcements a " +
            "LEFT JOIN users u ON a.admin_id = u.user_id " +
            "WHERE a.start_time <= NOW() AND a.end_time >= NOW() " +
            "ORDER BY a.priority DESC, a.created_at DESC LIMIT #{limit}")
    List<Announcement> selectLatestAnnouncements(@Param("limit") Integer limit);

    // 根据ID查询公告详情
    @Select("SELECT a.*, u.user_name as admin_name FROM announcements a " +
            "LEFT JOIN users u ON a.admin_id = u.user_id " +
            "WHERE a.announcement_id = #{id}")
    Announcement selectByIdWithAdminName(@Param("id") String id);

    // 根据管理员ID查询公告
    @Select("SELECT * FROM announcements WHERE admin_id = #{adminId} " +
            "ORDER BY created_at DESC")
    IPage<Announcement> selectByAdminId(Page<Announcement> page, @Param("adminId") String adminId);

    // 查询过期公告
    @Select("SELECT * FROM announcements WHERE end_time < NOW()")
    List<Announcement> selectExpiredAnnouncements();

    // 根据优先级查询公告 - 修复版本
    @Select("SELECT a.*, u.user_name as admin_name FROM announcements a " +
            "LEFT JOIN users u ON a.admin_id = u.user_id " +
            "WHERE a.priority = #{priority} AND a.start_time <= NOW() AND a.end_time >= NOW() " +
            "ORDER BY a.created_at DESC")
    IPage<Announcement> selectByPriority(Page<Announcement> page, @Param("priority") Integer priority);

    // 根据状态查询公告 - 使用 CASE WHEN 替代 script 标签
    @Select({
            "<script>",
            "SELECT a.*, u.user_name as admin_name FROM announcements a ",
            "LEFT JOIN users u ON a.admin_id = u.user_id ",
            "WHERE 1=1 ",
            "<when test='status == 1'>",
            "AND a.start_time &lt;= NOW() AND a.end_time &gt;= NOW() ",
            "</when>",
            "<when test='status == 2'>",
            "AND a.start_time &gt; NOW() ",
            "</when>",
            "<when test='status == 3'>",
            "AND a.end_time &lt; NOW() ",
            "</when>",
            "ORDER BY a.priority DESC, a.created_at DESC",
            "</script>"
    })
    IPage<Announcement> selectByStatus(Page<Announcement> page, @Param("status") Integer status);

    // 搜索公告标题或内容 - 修复版本
    @Select("SELECT a.*, u.user_name as admin_name FROM announcements a " +
            "LEFT JOIN users u ON a.admin_id = u.user_id " +
            "WHERE (a.title LIKE CONCAT('%', #{keyword}, '%') OR a.content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY a.priority DESC, a.created_at DESC")
    IPage<Announcement> searchAnnouncements(Page<Announcement> page, @Param("keyword") String keyword);

    // 根据优先级和状态组合查询 - 使用 XML 转义字符
    @Select({
            "<script>",
            "SELECT a.*, u.user_name as admin_name FROM announcements a ",
            "LEFT JOIN users u ON a.admin_id = u.user_id ",
            "WHERE 1=1 ",
            "<if test='priority != null'>",
            "AND a.priority = #{priority} ",
            "</if>",
            "<if test='status != null'>",
            "<choose>",
            "<when test='status == 1'>AND a.start_time &lt;= NOW() AND a.end_time &gt;= NOW()</when>",
            "<when test='status == 2'>AND a.start_time &gt; NOW()</when>",
            "<when test='status == 3'>AND a.end_time &lt; NOW()</when>",
            "</choose>",
            "</if>",
            "ORDER BY a.priority DESC, a.created_at DESC",
            "</script>"
    })
    IPage<Announcement> selectByPriorityAndStatus(Page<Announcement> page,
                                                  @Param("priority") Integer priority, @Param("status") Integer status);
}