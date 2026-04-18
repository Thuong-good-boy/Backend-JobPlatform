package com.jobplatform.job_recruitment_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt "Trạm phát sóng"
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mở một cổng tên là /ws để Frontend (React) kết nối vào.
        // setAllowedOriginPatterns("*") để tránh lỗi CORS khi React gọi qua ở môi trường dev.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // Fallback nếu trình duyệt không hỗ trợ WebSocket thuần
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: Dùng để gửi thông báo chung cho nhiều người (như chat group)
        // /queue: Dùng để gửi thông báo riêng cho 1 người cụ thể (1-1)
        registry.enableSimpleBroker("/topic", "/queue");

        // Tiền tố cho các tin nhắn từ Frontend gửi LÊN Backend
        registry.setApplicationDestinationPrefixes("/app");

        // Tiền tố mặc định khi muốn gửi tin nhắn riêng cho 1 user
        registry.setUserDestinationPrefix("/user");
    }
}