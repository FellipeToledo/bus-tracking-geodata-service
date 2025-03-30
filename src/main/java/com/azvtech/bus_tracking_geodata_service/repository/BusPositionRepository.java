package com.azvtech.bus_tracking_geodata_service.repository;


import com.azvtech.bus_tracking_geodata_service.model.BusPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusPositionRepository extends JpaRepository<BusPosition, Long> {

}
