/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.spring.demo.entity;

import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.GeoIndexed;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author
 */
@Document("locations")
public class Location {

    @Id
    private String id;

    private final String name;

    private final LocalDateTime createdAt;

    @GeoIndexed(geoJson = true)
    private final Point location;

    public Location(final String name, LocalDateTime createdAt, final Point location) {
        super();
        this.name = name;
        this.createdAt = createdAt;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Point getLocation() {
        return location;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Location{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", createdAt=" + createdAt +  // Affichage du timestamp
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location1 = (Location) o;
        return Objects.equals(id, location1.id) &&
                Objects.equals(name, location1.name) &&
                Objects.equals(location, location1.location) &&
                Objects.equals(createdAt, location1.createdAt);  // Comparaison du timestamp
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, location, createdAt);
    }
}
