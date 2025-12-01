package org.example.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.example.classroom.entity.Announcement;
import org.example.classroom.mapper.AnnouncementMapper;
import org.example.classroom.service.AnnouncementService;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement>
        implements AnnouncementService {

    @Override
    public IPage<Announcement> getActiveAnnouncements(Integer page, Integer size) {
        Page<Announcement> pageParam = new Page<>(page, size);
        return baseMapper.selectActiveAnnouncements(pageParam);
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