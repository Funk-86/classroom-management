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
        System.out.println("=== 查询有效公告 ===");
        System.out.println("页码: " + page + ", 每页大小: " + size);

        // 由于MyBatis-Plus分页插件在处理带JOIN的@Select时有问题，直接手动查询和分页
        // 1. 先查询所有数据，然后获取总数和分页
        QueryWrapper<Announcement> listWrapper = new QueryWrapper<>();
        listWrapper.apply("start_time <= NOW()")
                .apply("end_time >= NOW()")
                .orderByDesc("priority")
                .orderByDesc("created_at");

        System.out.println("开始查询所有公告...");
        List<Announcement> allAnnouncements = baseMapper.selectList(listWrapper);
        long total = allAnnouncements != null ? allAnnouncements.size() : 0;
        System.out.println("查询到所有公告数量: " + total);

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