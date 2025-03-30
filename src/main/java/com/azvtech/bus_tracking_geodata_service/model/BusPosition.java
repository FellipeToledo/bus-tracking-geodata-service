package com.azvtech.bus_tracking_geodata_service.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bus_positions")
public class BusPosition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ordem;
    private int velocidade;
    private String linha;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    private LocalDateTime datahora;
    private LocalDateTime datahoraenvio;

    @CreationTimestamp
    private LocalDateTime datahoraservidor;
}
