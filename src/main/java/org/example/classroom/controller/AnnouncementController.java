package org.example.classroom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.classroom.entity.Announcement;
import org.example.classroom.service.AnnouncementService;
import org.example.classroom.dto.R;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    // 获取有效公告列表（分页）
    @GetMapping("/active")
    public R getActiveAnnouncements(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Announcement> announcements = announcementService.getActiveAnnouncements(page, size);
        return R.ok().put("data", announcements);
    }

    // 获取最新公告（限制数量）
    @GetMapping("/latest")
    public R getLatestAnnouncements(@RequestParam(defaultValue = "5") Integer limit) {
        List<Announcement> announcements = announcementService.getLatestAnnouncements(limit);
        return R.ok().put("data", announcements);
    }

    // 根据ID获取公告详情
    @GetMapping("/{id}")
    public R getAnnouncementById(@PathVariable String id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement != null) {
            return R.ok().put("data", announcement);
        } else {
            return R.error("公告不存在");
        }
    }

    // 发布新公告
    @PostMapping
    public R publishAnnouncement(@RequestBody Announcement announcement) {
        // 这里应该从认证信息中获取当前用户ID
        // announcement.setAdminId(getCurrentUserId());

        if (announcementService.publishAnnouncement(announcement)) {
            return R.ok("公告发布成功");
        } else {
            return R.error("公告发布失败");
        }
    }

    // 更新公告
    @PutMapping("/{id}")
    public R updateAnnouncement(@PathVariable String id, @RequestBody Announcement announcement) {
        announcement.setAnnouncementId(id);
        if (announcementService.updateAnnouncement(announcement)) {
            return R.ok("公告更新成功");
        } else {
            return R.error("公告更新失败");
        }
    }

    // 删除公告
    @DeleteMapping("/{id}")
    public R deleteAnnouncement(@PathVariable String id) {
        if (announcementService.deleteAnnouncement(id)) {
            return R.ok("公告删除成功");
        } else {
            return R.error("公告删除失败");
        }
    }

    // 根据管理员ID查询公告
    @GetMapping("/admin/{adminId}")
    public R getAnnouncementsByAdmin(
            @PathVariable String adminId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Announcement> announcements = announcementService.getAnnouncementsByAdmin(adminId, page, size);
        return R.ok().put("data", announcements);
    }

    // 清理过期公告
    @DeleteMapping("/cleanup/expired")
    public R cleanupExpiredAnnouncements() {
        if (announcementService.cleanupExpiredAnnouncements()) {
            return R.ok("过期公告清理成功");
        } else {
            return R.error("过期公告清理失败");
        }
    }
    // 根据状态查询公告
    @GetMapping("/priority/{priority}")
    public R getAnnouncementsByPriority(
            @PathVariable Integer priority,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Announcement> announcements = announcementService.getAnnouncementsByPriority(priority, page, size);
        return R.ok().put("data", announcements);
    }

    @GetMapping("/status/{status}")
    public R getAnnouncementsByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 状态说明：1-生效中，2-未生效，3-已过期
        IPage<Announcement> announcements = announcementService.getAnnouncementsByStatus(status, page, size);
        return R.ok().put("data", announcements);
    }
    //新增接口：搜索公告
    @GetMapping("/search")
    public R searchAnnouncements(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Announcement> announcements = announcementService.searchAnnouncements(keyword, page, size);
        return R.ok().put("data", announcements);
    }
    // 组合查询（优先级+状态）
    @GetMapping("/filter")
    public R getAnnouncementsByPriorityAndStatus(
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Announcement> announcements = announcementService.getAnnouncementsByPriorityAndStatus(priority, status, page, size);
        return R.ok().put("data", announcements);
    }

    @GetMapping("/statistics")
    public R getAnnouncementStatistics() {
        long totalCount = announcementService.count();
        long activeCount = announcementService.countActiveAnnouncements();
        long expiredCount = announcementService.getExpiredAnnouncements().size();

        // 统计各优先级公告数量
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("priority", "COUNT(*) as count")
                .groupBy("priority");
        // 这里需要根据实际需求实现具体的统计逻辑

        return R.ok()
                .put("totalCount", totalCount)
                .put("activeCount", activeCount)
                .put("expiredCount", expiredCount);
    }
}