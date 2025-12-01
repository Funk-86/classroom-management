package org.example.classroom.dto;

import java.time.LocalDateTime;

public class AnnouncementRequest {
    private String title;
    private String content;
    private Integer priority; // 0-普通, 1-重要
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getter和Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}