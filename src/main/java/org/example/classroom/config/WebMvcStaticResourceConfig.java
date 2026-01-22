package org.example.classroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置：将 /user_image/** 映射到服务器本地 user_image 目录
 * 用于存放用户上传的头像等文件
 */
@Configuration
public class WebMvcStaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /user_image/** 映射到持久化存储目录下的 user_image 文件夹
        // 优先使用环境变量配置的持久化存储路径，如果没有则使用项目运行目录
        // 在云服务器上，建议设置 UPLOAD_DIR 环境变量指向持久化存储目录
        String baseDir = System.getenv("UPLOAD_DIR");
        if (baseDir == null || baseDir.trim().isEmpty()) {
            baseDir = System.getProperty("user.dir");
        }
        String uploadPath = baseDir + "/user_image/";

        // 1）后端本地直接访问 /api/user_image/** （context-path=/api）
        registry.addResourceHandler("/user_image/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600); // 设置缓存时间（秒）

        // 2）兼容 Zeabur 反向代理导致的 /api/api/user_image/** → 去掉第一个 /api 后剩下 /api/user_image/**
        //    这里额外映射 /api/user_image/** 到同一物理目录
        registry.addResourceHandler("/api/user_image/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(3600);
    }
}


