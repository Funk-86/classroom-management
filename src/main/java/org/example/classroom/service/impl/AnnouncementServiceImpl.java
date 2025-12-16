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

        // 直接使用mapper方法查询（包含JOIN和admin_name）
        Page<Announcement> pageParam = new Page<>(page, size);
        IPage<Announcement> result = baseMapper.selectActiveAnnouncements(pageParam);

        System.out.println("查询结果总数: " + result.getTotal());
        System.out.println("查询结果记录数: " + (result.getRecords() != null ? result.getRecords().size() : 0));

        // 如果分页插件返回的总数为0但应该有数据，手动查询总数
        if (result.getTotal() == 0 && (result.getRecords() == null || result.getRecords().isEmpty())) {
            System.out.println("分页查询返回总数为0，手动查询总数...");
            QueryWrapper<Announcement> countWrapper = new QueryWrapper<>();
            countWrapper.apply("start_time <= NOW()")
                    .apply("end_time >= NOW()");
            long manualTotal = baseMapper.selectCount(countWrapper);
            System.out.println("手动查询总数: " + manualTotal);

            if (manualTotal > 0) {
                // 如果手动查询有数据，说明分页插件有问题，直接查询所有数据然后手动分页
                System.out.println("分页插件可能有问题，直接查询所有数据...");
                List<Announcement> allAnnouncements = baseMapper.selectList(new QueryWrapper<Announcement>()
                        .apply("start_time <= NOW()")
                        .apply("end_time >= NOW()")
                        .orderByDesc("priority")
                        .orderByDesc("created_at"));

                System.out.println("查询到所有公告数量: " + (allAnnouncements != null ? allAnnouncements.size() : 0));

                // 手动填充admin_name
                if (allAnnouncements != null && !allAnnouncements.isEmpty()) {
                    List<String> adminIds = allAnnouncements.stream()
                            .map(Announcement::getAdminId)
                            .filter(id -> id != null && !id.isEmpty())
                            .distinct()
                            .collect(Collectors.toList());

                    if (!adminIds.isEmpty()) {
                        List<User> users = userMapper.selectBatchIds(adminIds);
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

                    // 手动分页
                    int start = (page - 1) * size;
                    int end = Math.min(start + size, allAnnouncements.size());
                    List<Announcement> pagedList = start < allAnnouncements.size()
                            ? allAnnouncements.subList(start, end)
                            : java.util.Collections.emptyList();

                    result.setTotal(manualTotal);
                    result.setRecords(pagedList);
                    System.out.println("手动分页后记录数: " + pagedList.size());
                }
            }
        }

        // admin_name已经在SQL中通过JOIN查询填充，但如果手动查询则需要手动填充
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            System.out.println("查询成功，记录数: " + result.getRecords().size());
            result.getRecords().forEach(announcement -> {
                System.out.println("公告ID: " + announcement.getAnnouncementId() +
                        ", 标题: " + announcement.getTitle() +
                        ", 管理员: " + announcement.getAdminName());
            });
        } else {
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