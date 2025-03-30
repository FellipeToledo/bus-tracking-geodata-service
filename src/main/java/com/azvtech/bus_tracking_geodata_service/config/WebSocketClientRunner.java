package com.azvtech.bus_tracking_geodata_service.config;

import com.azvtech.bus_tracking_geodata_service.websocket.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class WebSocketClientRunner implements CommandLineRunner {
    @Autowired
    private Client client;

    @Override
    public void run(String... args) throws Exception {
        client.connect();
    }
}
