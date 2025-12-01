package org.example.classroom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 教室占用冲突检测结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomConflictResult {
    /**
     * 是否存在冲突
     */
    private boolean hasConflict;

    /**
     * 冲突类型
     */
    private ConflictType conflictType;

    /**
     * 冲突信息列表
     */
    private List<ClassroomOccupationInfo> conflicts;

    /**
     * 冲突描述信息
     */
    private String message;

    /**
     * 冲突类型枚举
     */
    public enum ConflictType {
        NONE("无冲突"),
        RESERVATION("预约冲突"),
        COURSE_SCHEDULE("课程安排冲突"),
        ATTENDANCE_SESSION("签到活动冲突"),
        MULTIPLE("多种类型冲突");

        private final String description;

        ConflictType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 创建无冲突结果
     */
    public static ClassroomConflictResult noConflict() {
        return new ClassroomConflictResult(
                false,
                ConflictType.NONE,
                null,
                "教室在该时间段可用"
        );
    }

    /**
     * 创建冲突结果
     */
    public static ClassroomConflictResult conflict(
            ConflictType type,
            List<ClassroomOccupationInfo> conflicts,
            String message
    ) {
        return new ClassroomConflictResult(
                true,
                type,
                conflicts,
                message != null ? message : type.getDescription()
        );
    }
}

