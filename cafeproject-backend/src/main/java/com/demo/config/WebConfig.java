package com.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// WebConfig 클래스: CORS 설정을 위한 Spring 설정 클래스
@Configuration // 이 클래스가 Spring 설정 클래스임을 나타냄
public class WebConfig implements WebMvcConfigurer {

    // CORS 매핑을 추가하는 메서드
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 경로에 대해 CORS 설정 적용
        registry.addMapping("/**") // 모든 경로 CORS 정책 적용
                .allowedOrigins("http://localhost:3000") // 허용할 출처 설정 (React 프론트엔드)
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 자격 증명(쿠키) 허용
    }
}

