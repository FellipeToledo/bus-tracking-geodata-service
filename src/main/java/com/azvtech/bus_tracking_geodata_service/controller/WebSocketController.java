package com.azvtech.bus_tracking_geodata_service.controller;

import com.azvtech.bus_tracking_geodata_service.dto.GpsDataDTO;
import com.azvtech.bus_tracking_geodata_service.service.GeoDataService;
import com.azvtech.bus_tracking_geodata_service.websocket.GeoDataWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketController {
    private final GeoDataService geoDataService;
    private final GeoDataWebSocketHandler webSocketHandler;

    @GetMapping("/clients/count")
    public int getConnectedClientsCount() {
        return webSocketHandler.getSessionCount();
    }

    @MessageMapping("/bus-position") // Rota para receber mensagens (/app/bus-position)
    @SendTo("/topic/positions") // Tópico para broadcast das posições
    public String handleBusPosition(GpsDataDTO gpsData) throws Exception {
        // Processa os dados (opcional - pode manter no Client existente)
        geoDataService.processAndSaveGpsData(List.of(gpsData));

        // Converte para GeoJSON e retorna para broadcast
        return geoDataService.convertToGeoJson(List.of(gpsData));
    }
}
