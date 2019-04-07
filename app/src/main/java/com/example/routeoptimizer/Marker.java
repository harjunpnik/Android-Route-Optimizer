package com.example.routeoptimizer;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

//  Marker class for Waypoints, starting and destination point
public class Marker implements Serializable {

    private String name;
    private double lat;
    private double lng;

    public Marker(String name, double lat, double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;

    }

    public String getName () { return name; }

    public double getLat () { return lat; }

    public double getLng () { return lng; }

    public LatLng getLatLng(){ return new LatLng(lat,lng); }
}
