package com.azvtech.bus_tracking_geodata_service.service;

import com.azvtech.bus_tracking_geodata_service.dto.GpsDataDTO;
import com.azvtech.bus_tracking_geodata_service.model.BusPosition;
import com.azvtech.bus_tracking_geodata_service.repository.BusPositionRepository;
import com.azvtech.bus_tracking_geodata_service.websocket.GeoDataWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoDataService {
    private static final Logger logger = LoggerFactory.getLogger(GeoDataService.class);
    private final BusPositionRepository repository;
    private final GeoDataWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Processa e salva dados GPS, depois envia para frontend via WebSocket
     */
    public void processAndSaveGpsData(List<GpsDataDTO> gpsDataList) {
        try {
            logger.info("Processing {} GPS data points", gpsDataList.size());

            // 1. Converter e salvar no banco
            List<BusPosition> positions = convertToBusPositions(gpsDataList);
            //repository.saveAll(positions);
            //log.info("Saved {} positions to database", positions.size());

            // 2. Converter para GeoJSON
            String geoJson = convertToGeoJson(gpsDataList);


            // 3. Enviar para frontend via WebSocket
            webSocketHandler.broadcast(geoJson);


            logger.info("Broadcasted complete dataset to {} clients",
                    webSocketHandler.getSessionCount());
        } catch (Exception e) {
            logger.error("Error processing GPS data", e);
        }
    }

    /**
     * Converte lista de DTOs para entidades BusPosition com pontos geográficos
     */
    private List<BusPosition> convertToBusPositions(List<GpsDataDTO> gpsDataList) {
        return gpsDataList.stream()
                .map(this::convertToBusPosition)
                .collect(Collectors.toList());
    }

    /**
     * Converte um DTO para entidade BusPosition
     */
    private BusPosition convertToBusPosition(GpsDataDTO dto) {
        BusPosition position = new BusPosition();
        position.setOrdem(dto.getOrdem());
        position.setVelocidade(dto.getVelocidade());
        position.setLinha(dto.getLinha());
        position.setDatahora(dto.getDatahora());
        position.setDatahoraenvio(dto.getDatahoraenvio());

        // Criar ponto geoespacial
        Point point = geometryFactory.createPoint(new Coordinate(dto.getLongitude(), dto.getLatitude()));
        position.setLocation(point);

        return position;
    }

    /**
     * Converte dados GPS para formato GeoJSON
     */
    public String convertToGeoJson(List<GpsDataDTO> gpsDataList) throws JsonProcessingException {
        String features = gpsDataList.stream()
                .map(dto -> {
                    try {
                        return objectMapper.writeValueAsString(createGeoJsonFeature(dto));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error creating GeoJSON feature", e);
                    }
                })
                .collect(Collectors.joining(","));

        return String.format("{\"type\":\"FeatureCollection\",\"features\":[%s]}", features);
    }

    /**
     * Cria um objeto Feature do GeoJSON para um ponto GPS
     */
    private GeoJsonFeature createGeoJsonFeature(GpsDataDTO dto) {
        GeoJsonFeature feature = new GeoJsonFeature();
        feature.setType("Feature");

        GeoJsonGeometry geometry = new GeoJsonGeometry();
        geometry.setType("Point");
        geometry.setCoordinates(new double[]{dto.getLongitude(), dto.getLatitude()});
        feature.setGeometry(geometry);

        GeoJsonProperties properties = new GeoJsonProperties();
        properties.setOrdem(dto.getOrdem());
        properties.setVelocidade(dto.getVelocidade());
        properties.setLinha(dto.getLinha());
        properties.setDatahora(dto.getDatahora());
        feature.setProperties(properties);

        return feature;
    }

    // Classes internas para estrutura GeoJSON
    @Data
    private static class GeoJsonFeature {
        private String type;
        private GeoJsonGeometry geometry;
        private GeoJsonProperties properties;
    }

    @Data
    private static class GeoJsonGeometry {
        private String type;
        private double[] coordinates;
    }

    @Data
    private static class GeoJsonProperties {
        private String ordem;
        private int velocidade;
        private String linha;
        private LocalDateTime datahora;
    }
}
