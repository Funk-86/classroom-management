package org.example.classroom.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.classroom.entity.Announcement;
import java.util.List;

public interface AnnouncementService extends IService<Announcement> {
    IPage<Announcement> getActiveAnnouncements(Integer page, Integer size);
    List<Announcement> getLatestAnnouncements(Integer limit);
    Announcement getAnnouncementById(String id);
    boolean publishAnnouncement(Announcement announcement);
    boolean updateAnnouncement(Announcement announcement);
    boolean deleteAnnouncement(String id);
    IPage<Announcement> getAnnouncementsByAdmin(String adminId, Integer page, Integer size);
    List<Announcement> getExpiredAnnouncements();
    boolean cleanupExpiredAnnouncements();
    long countActiveAnnouncements();
    IPage<Announcement> getAnnouncementsByPriority(Integer priority, Integer page, Integer size);
    IPage<Announcement> getAnnouncementsByStatus(Integer status, Integer page, Integer size);
    IPage<Announcement> searchAnnouncements(String keyword, Integer page, Integer size);
    IPage<Announcement> getAnnouncementsByPriorityAndStatus(Integer priority, Integer status, Integer page, Integer size);
}