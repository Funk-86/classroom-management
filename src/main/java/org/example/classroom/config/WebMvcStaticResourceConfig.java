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
        // 将 /user_image/** 映射到项目运行目录下的 user_image 文件夹
        // 如有需要，可将 "file:user_image/" 换成绝对路径，例如 "file:/opt/classroom/user_image/"
        registry.addResourceHandler("/user_image/**")
                .addResourceLocations("file:user_image/");
    }
}


