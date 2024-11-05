package com.arangodb.spring.demo.service;

import com.arangodb.spring.demo.entity.Location;
import com.arangodb.spring.demo.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class LocationService {

    @Autowired
    private LocationRepository repository;

    public List<Location> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public List<Location> findFirst5LocationsNear(Point point) {
        return repository.findByLocationNear(point, PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(GeoResult::getContent) // Récupère le Location de chaque GeoResult
                .collect(Collectors.toList());
    }

    public List<Location> findNext5LocationsNear(Point point) {
        return repository.findByLocationNear(point, PageRequest.of(1, 5))
                .getContent()
                .stream()
                .map(GeoResult::getContent) // Récupère le Location de chaque GeoResult
                .collect(Collectors.toList());
    }

    public List<Location> findLocationsWithinDistance(Point point, double distanceKm) {
        return repository.findByLocationWithin(point, new Distance(distanceKm, Metrics.KILOMETERS))
                .getContent()
                .stream()
                .map(GeoResult::getContent) // Récupère le Location de chaque GeoResult
                .collect(Collectors.toList());
    }

    public Iterable<Location> findLocationsWithinRange(Point point, double minDistanceMeters, double maxDistanceMeters) {
        return repository.findByLocationWithin(point,
                Range.of(Range.Bound.inclusive(minDistanceMeters), Range.Bound.exclusive(maxDistanceMeters)));
    }

    public Iterable<Location> findLocationsWithinPolygon(Polygon polygon) {
        return repository.findByLocationWithin(polygon);
    }
}
