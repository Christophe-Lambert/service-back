package com.mapviewer.model.api;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Coordinate {
    private double lat;
    private double lng;
}