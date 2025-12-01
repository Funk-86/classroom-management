package org.example.classroom.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.util.Date;

@TableName("user_session")
public class UserSession {

    @TableId(value = "session_id", type = IdType.INPUT)
    private String sessionId;

    @TableField("user_id")
    private String userId;

    @TableField("login_time")
    private Date loginTime;

    @TableField("expire_time")
    private Date expireTime;

    @TableField("last_access_time")
    private Date lastAccessTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    // 构造方法
    public UserSession() {}

    public UserSession(String sessionId, String userId, Date loginTime, Date expireTime) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.loginTime = loginTime;
        this.expireTime = expireTime;
        this.lastAccessTime = loginTime;
    }

    // Getter和Setter
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getLoginTime() { return loginTime; }
    public void setLoginTime(Date loginTime) { this.loginTime = loginTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Date getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(Date lastAccessTime) { this.lastAccessTime = lastAccessTime; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}