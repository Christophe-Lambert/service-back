package com.arangodb.spring.demo.controller;

import com.arangodb.spring.demo.entity.Location;
import com.arangodb.spring.demo.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping("/all")
    public List<Location> getAllLocations() {
        return locationService.findAll();
    }

    @GetMapping("/near")
    public List<Location> getFirst5LocationsNear(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return locationService.findFirst5LocationsNear(new Point(longitude, latitude));
    }

    @GetMapping("/near/next")
    public List<Location> getNext5LocationsNear(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return locationService.findNext5LocationsNear(new Point(longitude, latitude));
    }

    @GetMapping("/within")
    public List<Location> getLocationsWithinDistance(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double distanceKm) {
        return locationService.findLocationsWithinDistance(new Point(longitude, latitude), distanceKm);
    }

    @GetMapping("/range")
    public Iterable<Location> getLocationsWithinRange(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double minDistanceMeters,
            @RequestParam double maxDistanceMeters) {
        return locationService.findLocationsWithinRange(new Point(longitude, latitude), minDistanceMeters, maxDistanceMeters);
    }

    @PostMapping("/polygon")
    public Iterable<Location> getLocationsWithinPolygon(@RequestBody List<Point> points) {
        Polygon polygon = new Polygon(points);
        return locationService.findLocationsWithinPolygon(polygon);
    }
}
