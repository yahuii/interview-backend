package com.guqin.interview.config;

import com.guqin.interview.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            // 注册 socket处理器
            registry.addHandler(chatWebSocketHandler (), "/ws/chat").setAllowedOrigins("*");
        }

        @Bean
        public ServletServerContainerFactoryBean createWebSocketContainer() {
            ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
            // 消息缓冲区大小的大小
            container.setMaxTextMessageBufferSize(8192);
            container.setMaxBinaryMessageBufferSize(8192);
            return container;
        }

        @Bean
        public WebSocketHandler chatWebSocketHandler () {
            return new ChatWebSocketHandler();
        }


}

