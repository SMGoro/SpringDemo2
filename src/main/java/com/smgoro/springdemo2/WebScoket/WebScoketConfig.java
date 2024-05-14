package com.smgoro.springdemo2.WebScoket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration // 配置类
@EnableWebSocket
public class WebScoketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("加载消息处理器");
        // paths -- 路径
        registry.addHandler(new WebScoketPushHandler(), "/WebScoketServer")
                .addInterceptors(new WebScoketInterceptor())
                .setAllowedOrigins("*");
    }
}
