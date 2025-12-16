package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.classroom.entity.Announcement;
import org.example.classroom.entity.User;
import org.example.classroom.mapper.AnnouncementMapper;
import org.example.classroom.mapper.UserMapper;
import org.example.classroom.service.AnnouncementService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<Announcement> getActiveAnnouncements(Integer page, Integer size) {
        System.out.println("=== 查询有效公告 ===");
        System.out.println("页码: " + page + ", 每页大小: " + size);

        // 由于MyBatis-Plus分页插件在处理带JOIN的@Select时有问题，直接手动查询和分页
        // 1. 先查询所有公告（不限制时间），看看数据库中的数据
        System.out.println("开始查询所有公告（不限制时间）...");
        List<Announcement> allAnnouncementsInDb = baseMapper.selectList(null);
        System.out.println("数据库中总公告数: " + (allAnnouncementsInDb != null ? allAnnouncementsInDb.size() : 0));

        // 输出所有公告的时间信息用于调试
        if (allAnnouncementsInDb != null && !allAnnouncementsInDb.isEmpty()) {
            System.out.println("=== 数据库中的公告时间信息 ===");
            allAnnouncementsInDb.forEach(a -> {
                System.out.println("公告ID: " + a.getAnnouncementId() +
                        ", 标题: " + a.getTitle() +
                        ", 开始时间: " + a.getStartTime() +
                        ", 结束时间: " + a.getEndTime());
            });
        }

        // 2. 使用Java代码过滤有效公告（避免SQL时间比较问题）
        // 使用东八区时间，确保时间正确
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        LocalDateTime now = ZonedDateTime.now(shanghaiZone).toLocalDateTime();
        System.out.println("当前时间（东八区）: " + now);

        List<Announcement> allAnnouncements = allAnnouncementsInDb != null
                ? allAnnouncementsInDb.stream()
                .filter(a -> {
                    if (a.getStartTime() == null || a.getEndTime() == null) {
                        return false;
                    }
                    boolean isValid = !now.isBefore(a.getStartTime()) && !now.isAfter(a.getEndTime());
                    if (!isValid) {
                        System.out.println("公告 " + a.getAnnouncementId() + " 不在有效期内: " +
                                "开始=" + a.getStartTime() + ", 结束=" + a.getEndTime() + ", 当前=" + now);
                    }
                    return isValid;
                })
                .sorted((a1, a2) -> {
                    // 按优先级降序，然后按创建时间降序
                    int priorityCompare = Integer.compare(
                            a2.getPriority() != null ? a2.getPriority() : 0,
                            a1.getPriority() != null ? a1.getPriority() : 0
                    );
                    if (priorityCompare != 0) {
                        return priorityCompare;
                    }
                    if (a1.getCreatedAt() == null || a2.getCreatedAt() == null) {
                        return 0;
                    }
                    return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                })
                .collect(Collectors.toList())
                : java.util.Collections.emptyList();

        long total = allAnnouncements != null ? allAnnouncements.size() : 0;
        System.out.println("过滤后的有效公告数量: " + total);

        // 2. 创建分页结果对象
        Page<Announcement> result = new Page<>(page, size);
        result.setTotal(total);

        // 3. 如果总数为0，直接返回空结果
        if (total == 0) {
            result.setRecords(java.util.Collections.emptyList());
            System.out.println("总数为0，返回空结果");
            return result;
        }

        // 4. 手动填充admin_name（因为selectList不支持JOIN）
        if (allAnnouncements != null && !allAnnouncements.isEmpty()) {
            List<String> adminIds = allAnnouncements.stream()
                    .map(Announcement::getAdminId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("需要查询的adminId数量: " + adminIds.size());

            if (!adminIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(adminIds);
                System.out.println("查询到的用户数量: " + users.size());

                Map<String, String> adminNameMap = users.stream()
                        .collect(Collectors.toMap(
                                User::getUserId,
                                User::getUserName,
                                (v1, v2) -> v1
                        ));

                allAnnouncements.forEach(announcement -> {
                    if (announcement.getAdminId() != null) {
                        String adminName = adminNameMap.get(announcement.getAdminId());
                        announcement.setAdminName(adminName != null ? adminName : "未知");
                    }
                });
            }

            // 6. 手动分页
            int start = (page - 1) * size;
            int end = Math.min(start + size, allAnnouncements.size());
            List<Announcement> pagedList = start < allAnnouncements.size()
                    ? allAnnouncements.subList(start, end)
                    : java.util.Collections.emptyList();

            result.setRecords(pagedList);
            System.out.println("手动分页后记录数: " + pagedList.size());

            // 输出详细信息
            pagedList.forEach(announcement -> {
                System.out.println("公告ID: " + announcement.getAnnouncementId() +
                        ", 标题: " + announcement.getTitle() +
                        ", 管理员: " + announcement.getAdminName());
            });
        } else {
            result.setRecords(java.util.Collections.emptyList());
            System.out.println("查询结果为空");
        }

        return result;
    }

    @Override
    public List<Announcement> getLatestAnnouncements(Integer limit) {
        return baseMapper.selectLatestAnnouncements(limit);
    }

    @Override
    public Announcement getAnnouncementById(String id) {
        return baseMapper.selectByIdWithAdminName(id);
    }

    @Override
    public boolean publishAnnouncement(Announcement announcement) {
        return save(announcement);
    }

    @Override
    public boolean updateAnnouncement(Announcement announcement) {
        return updateById(announcement);
    }

    @Override
    public boolean deleteAnnouncement(String id) {
        return removeById(id);
    }

    @Override
    public IPage<Announcement> getAnnouncementsByAdmin(String adminId, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.selectByAdminId(pageParam, adminId);
    }

    @Override
    public List<Announcement> getExpiredAnnouncements() {
        return baseMapper.selectExpiredAnnouncements();
    }

    @Override
    public boolean cleanupExpiredAnnouncements() {
        List<Announcement> expired = getExpiredAnnouncements();
        if (!expired.isEmpty()) {
            return removeByIds(expired.stream()
                    .map(Announcement::getAnnouncementId)
                    .toList());
        }
        return true;
    }

    @Override
    public long countActiveAnnouncements() {
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("end_time", LocalDateTime.now()) // 结束时间大于当前时间
                .le("start_time", LocalDateTime.now()); // 开始时间小于当前时间
        return count(queryWrapper);
    }

    @Override
    public IPage<Announcement> getAnnouncementsByPriority(Integer priority, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.selectByPriority(pageParam, priority);
    }

    @Override
    public IPage<Announcement> getAnnouncementsByStatus(Integer status, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.selectByStatus(pageParam, status);
    }

    @Override
    public IPage<Announcement> searchAnnouncements(String keyword, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.searchAnnouncements(pageParam, keyword);
    }

    @Override
    public IPage<Announcement> getAnnouncementsByPriorityAndStatus(Integer priority, Integer status, Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.selectByPriorityAndStatus(pageParam, priority, status);
    }
}