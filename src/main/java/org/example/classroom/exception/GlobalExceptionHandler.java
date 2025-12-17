package org.example.classroom.exception;

import org.example.classroom.dto.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleValidationException(Exception e) {
        Map<String, String> errors = new HashMap<>();

        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            ex.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }

        log.warn("参数验证失败: {}", errors);
        return R.error(400, "参数验证失败").put("errors", errors);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return R.error(400, e.getMessage());
    }

    /**
     * 处理HTTP方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public R handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("HTTP方法不支持: {}", e.getMethod());
        String supportedMethods = e.getSupportedMethods() != null ? String.join(", ", e.getSupportedMethods()) : "未知";
        return R.error(405, "请求方法 " + e.getMethod() + " 不支持，支持的方法: " + supportedMethods);
    }

    /**
     * 处理数据库主键重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("主键重复异常: {}", e.getMessage());
        String message = e.getMessage();

        // 尝试从异常信息中提取更友好的错误提示
        if (message != null) {
            if (message.contains("classrooms.PRIMARY") || message.contains("classroom_id")) {
                // 提取教室编号
                String classroomId = extractIdFromMessage(message);
                if (classroomId != null) {
                    return R.error(400, "教室编号 " + classroomId + " 已存在，请使用其他编号");
                }
                return R.error(400, "教室编号已存在，请使用其他编号");
            } else if (message.contains("buildings.PRIMARY") || message.contains("building_id")) {
                String buildingId = extractIdFromMessage(message);
                if (buildingId != null) {
                    return R.error(400, "教学楼编号 " + buildingId + " 已存在，请使用其他编号");
                }
                return R.error(400, "教学楼编号已存在，请使用其他编号");
            }
        }

        return R.error(400, "数据已存在，请检查输入信息");
    }

    /**
     * 从异常消息中提取ID
     */
    private String extractIdFromMessage(String message) {
        if (message == null) {
            return null;
        }
        // 尝试匹配 "Duplicate entry 'xxx' for key"
        int start = message.indexOf("'");
        int end = message.indexOf("'", start + 1);
        if (start >= 0 && end > start) {
            return message.substring(start + 1, end);
        }
        return null;
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return R.error("系统内部错误，请联系管理员");
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R handleException(Exception e) {
        log.error("系统异常", e);
        return R.error("系统异常，请联系管理员");
    }
}

