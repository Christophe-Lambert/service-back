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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class LocationService {

    @Autowired
    private LocationRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final double PARIS_LAT = 48.8566;  // Latitude de Paris
    private static final double PARIS_LON = 2.3522;  // Longitude de Paris
    private static final double MAX_RADIUS_KM = 50.0; // Rayon de 50 km autour de Paris
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Méthode réactive qui démarre l'exécution de `addPositions` sans bloquer l'appelant.
     * Elle renvoie immédiatement une réponse tout en lançant le processus en arrière-plan.
     *
     * @param name Le nom de la location
     * @return Un Mono vide pour indiquer que l'opération est lancée
     */
    public Mono<Void> startAddPositions(String name) {
        // Démarre l'exécution de addPositions de manière asynchrone
        return Mono.fromRunnable(() -> addPositions(name));
    }

    /**
     * Méthode asynchrone qui génère des positions toutes les 3 secondes pendant 30 secondes.
     * Cette méthode est maintenant exécutée de manière non-bloquante.
     *
     * @param name Le nom de la location
     */
    public void addPositions(String name) {
        // Exécution dans un thread séparé pour ne pas bloquer le processus principal
        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) { // 10 fois pour 30 secondes (toutes les 3 secondes)
                double[] newCoords = getRandomCoordinates(PARIS_LAT, PARIS_LON, MAX_RADIUS_KM);
                double newLat = newCoords[0];
                double newLon = newCoords[1];
                Location location = new Location(name, LocalDateTime.now(), new Point(newLon, newLat));

                // Enregistrer dans la base de données
                repository.save(location);

                // Envoyer au topic WebSocket
                messagingTemplate.convertAndSend("/topic/locations", location);

                // Attendre 3 secondes avant de générer la prochaine position
                try {
                    Thread.sleep(3000);  // 3000 ms = 3 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    /**
     * Génère des coordonnées aléatoires dans un rayon spécifié autour d'un point donné.
     *
     * @param centerLat Latitude du centre
     * @param centerLon Longitude du centre
     * @param radiusKm Rayon autour du centre (en kilomètres)
     * @return Un tableau de doubles où le premier élément est la latitude et le deuxième élément est la longitude
     */
    public double[] getRandomCoordinates(double centerLat, double centerLon, double radiusKm) {
        Random random = new Random();

        // Rayon en degrés (approximatif)
        double radiusDeg = radiusKm / 111.32; // 1° de latitude = environ 111.32 km

        // Générer des déplacements aléatoires en latitude et longitude
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusDeg * Math.sqrt(u); // Rayon en latitude
        double t = 2 * Math.PI * v; // Angle en radians
        double x = w * Math.cos(t); // Déplacement en latitude
        double y = w * Math.sin(t); // Déplacement en longitude

        // Calculer les nouvelles coordonnées
        double newLat = centerLat + x;
        double newLon = centerLon + (y / Math.cos(Math.toRadians(centerLat))); // Correction pour la longitude

        return new double[]{newLat, newLon};
    }

    public List<Location> findAll() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void addPosition(String name) {
        repository.save(new Location(name, LocalDateTime.now(), new Point(-6.815096, 55.167801)));
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
