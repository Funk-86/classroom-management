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

        // 使用QueryWrapper进行查询，确保分页正确
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.le("start_time", LocalDateTime.now())
                .ge("end_time", LocalDateTime.now())
                .orderByDesc("priority")
                .orderByDesc("created_at");

        // 使用selectPage进行分页查询
        IPage<Announcement> result = baseMapper.selectPage(pageParam, queryWrapper);

        // 手动填充admin_name
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            // 收集所有adminId
            List<String> adminIds = result.getRecords().stream()
                    .map(Announcement::getAdminId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询用户信息
            if (!adminIds.isEmpty()) {
                List<User> users = userMapper.selectBatchIds(adminIds);
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