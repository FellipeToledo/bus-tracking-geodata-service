package com.azvtech.bus_tracking_geodata_service.config;

import com.azvtech.bus_tracking_geodata_service.websocket.GeoDataWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.standard.StandardWebSocketUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketServerConfig implements WebSocketConfigurer {

    private final GeoDataWebSocketHandler webSocketHandler;

    public WebSocketServerConfig(GeoDataWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/geo-data-updates")
                .setAllowedOrigins("*")
                .setHandshakeHandler(new DefaultHandshakeHandler(
                        new StandardWebSocketUpgradeStrategy()
                ))
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536);  // 64KB
        container.setMaxBinaryMessageBufferSize(65536);
        container.setMaxSessionIdleTimeout(300000L);  // 5 minutos
        container.setAsyncSendTimeout(10000L); // Timeout de envio
        return container;
    }
}
