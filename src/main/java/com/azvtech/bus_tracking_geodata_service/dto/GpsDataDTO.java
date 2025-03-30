package com.azvtech.bus_tracking_geodata_service.dto;

import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
public class GpsDataDTO {
    private String ordem;
    private double latitude;
    private double  longitude;
    private LocalDateTime datahora;
    private int velocidade;
    private String linha;
    private LocalDateTime datahoraenvio;
    private LocalDateTime datahoraservidor;
    private Point location; // Campo geoespacial
}
