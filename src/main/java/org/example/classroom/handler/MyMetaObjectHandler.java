package org.example.classroom.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    // 统一使用北京时间（Asia/Shanghai, GMT+8）
    private static final ZoneId BEIJING_ZONE = ZoneId.of("Asia/Shanghai");

    /**
     * 获取当前北京时间
     */
    private LocalDateTime getBeijingTime() {
        return LocalDateTime.now(BEIJING_ZONE);
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", this::getBeijingTime, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updatedAt", this::getBeijingTime, LocalDateTime.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", this::getBeijingTime, LocalDateTime.class);
    }
}
