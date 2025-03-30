package com.azvtech.bus_tracking_geodata_service.websocket;

import com.azvtech.bus_tracking_geodata_service.dto.GpsDataDTO;
import com.azvtech.bus_tracking_geodata_service.service.GeoDataService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class Client extends TextWebSocketHandler {

    private final GeoDataService geoDataService;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Getter
    private final CopyOnWriteArrayList<GpsDataDTO> receivedMessages = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebSocketConnectionManager connectionManager;

    public Client(GeoDataService geoDataService) {
        this.geoDataService = geoDataService;
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Conexão estabelecida com o servidor WebSocket: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        try {
            List<GpsDataDTO> gpsDataList = objectMapper.readValue(
                    message.getPayload(),
                    new TypeReference<>() {}
            );

            log.info("Recebidos {} pontos GPS", gpsDataList.size());

            // Adiciona localização geoespacial
            gpsDataList.forEach(dto -> {
                Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
                dto.setLocation(point);
            });

            receivedMessages.addAll(gpsDataList);

            // Processa, salva no banco e envia para frontend (tudo no GeoDataService)
            geoDataService.processAndSaveGpsData(gpsDataList);

        } catch (Exception e) {
            log.error("Erro ao processar mensagem WebSocket", e);
            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Conexão fechada: {} - Razão: {}", session.getId(), status.getReason());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Erro de transporte na sessão {}: {}", session.getId(), exception.getMessage());
    }

    public void connect() {
        if (connectionManager != null && connectionManager.isRunning()) {
            log.warn("O cliente WebSocket já está conectado");
            return;
        }

        StandardWebSocketClient client = new StandardWebSocketClient();
        connectionManager = new WebSocketConnectionManager(
                client,
                this,
                "ws://localhost:8080/gps-updates"  // URL do servidor de polling
        );

        connectionManager.start();
        log.info("Iniciando conexão WebSocket com o servidor de polling...");
    }

    public void disconnect() {
        if (connectionManager != null && connectionManager.isRunning()) {
            connectionManager.stop();
            log.info("Conexão WebSocket encerrada");
        }
    }

    public boolean isConnected() {
        return connectionManager != null && connectionManager.isRunning();
    }
}
