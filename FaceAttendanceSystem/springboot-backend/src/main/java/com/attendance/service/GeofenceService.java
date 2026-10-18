package com.attendance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeofenceService {

    @Value("${geofence.campus-latitude}")
    private double campusLat;

    @Value("${geofence.campus-longitude}")
    private double campusLng;

    @Value("${geofence.allowed-radius-meters}")
    private double allowedRadiusMeters;

    @Value("${geofence.enabled}")
    private boolean enabled;

    /**
     * Returns true if the given coordinates are within the allowed radius
     * of campus, or if geofencing is disabled, or if coordinates weren't
     * provided at all (fails open — a camera without GPS shouldn't block
     * attendance entirely; decide if that's the right default for your
     * deployment before relying on this).
     */
    public boolean isWithinCampus(Double latitude, Double longitude) {
        if (!enabled || latitude == null || longitude == null) {
            return true;
        }
        double distanceMeters = haversineMeters(campusLat, campusLng, latitude, longitude);
        return distanceMeters <= allowedRadiusMeters;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusMeters = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }
}
