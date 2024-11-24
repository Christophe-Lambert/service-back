package com.arangodb.spring.demo.controller;

import com.arangodb.spring.demo.entity.Location;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class LocationWebSocketController {

    // Cette méthode sera utilisée pour diffuser les nouvelles positions
    @SendTo("/topic/locations")
    public Location broadcastLocation(Location location) {
        return location; // Diffuse la position
    }
}
