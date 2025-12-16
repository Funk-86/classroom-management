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
        Page<Announcement> pageParam = new Page<>(page, size);

        System.out.println("=== 查询有效公告 ===");
        System.out.println("页码: " + page + ", 每页大小: " + size);

        // 使用QueryWrapper进行查询，使用SQL函数NOW()避免时区问题
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply("start_time <= NOW()")
                .apply("end_time >= NOW()")
                .orderByDesc("priority")
                .orderByDesc("created_at");

        System.out.println("查询条件: start_time <= NOW() AND end_time >= NOW()");

        // 使用selectPage进行分页查询
        IPage<Announcement> result = baseMapper.selectPage(pageParam, queryWrapper);

        System.out.println("查询结果总数: " + result.getTotal());
        System.out.println("查询结果记录数: " + (result.getRecords() != null ? result.getRecords().size() : 0));

        // 手动填充admin_name
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            System.out.println("开始填充adminName，记录数: " + result.getRecords().size());

            // 收集所有adminId
            List<String> adminIds = result.getRecords().stream()
                    .map(Announcement::getAdminId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            System.out.println("需要查询的adminId数量: " + adminIds.size());

            // 批量查询用户信息
            if (!adminIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(adminIds);
                System.out.println("查询到的用户数量: " + users.size());

                Map<String, String> adminNameMap = users.stream()
                        .collect(Collectors.toMap(
                                User::getUserId,
                                User::getUserName,
                                (v1, v2) -> v1
                        ));

                // 填充adminName
                result.getRecords().forEach(announcement -> {
                    if (announcement.getAdminId() != null) {
                        String adminName = adminNameMap.get(announcement.getAdminId());
                        announcement.setAdminName(adminName != null ? adminName : "未知");
                    }
                });
            }
        } else {
            System.out.println("查询结果为空，records为null或empty");
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