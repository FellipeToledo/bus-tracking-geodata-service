package com.azvtech.bus_tracking_geodata_service.websocket;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GeoDataWebSocketHandler  extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(GeoDataWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        logger.info("Conexão estabelecida: {}", session.getId());

        try {
            session.sendMessage(new TextMessage("{\"status\":\"CONNECTED\"}"));
        } catch (IOException e) {
            logger.error("Erro ao enviar mensagem de confirmação", e);
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        logger.debug("Mensagem recebida: {}", message.getPayload());
        // Lógica para processar mensagens do cliente, se necessário
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        logger.info("Conexão encerrada: {} - Razão: {}", session.getId(), status.getReason());
    }

    public void broadcast(String message) {
        sessions.removeIf(s -> !s.isOpen());

        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                logger.error("Erro no broadcast para sessão {}", session.getId(), e);
            }
        });
    }
}
