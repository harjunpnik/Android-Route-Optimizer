package com.example.routeoptimizer;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable {

    private String name;
    private Marker startingPoint;
    private Marker destinationPoint;
    private ArrayList<Marker> waypoints;
    private String polyline;
    private int distance;
    private int time;


    public Route(String name,Marker startingPoint, Marker destinationPoint, ArrayList<Marker> waypoints, String polyline, int distance, int time){
        this.name = name;
        this.startingPoint = startingPoint;
        this.destinationPoint = destinationPoint;
        this.waypoints = waypoints;
        this.polyline = polyline;
        this.distance = distance;
        this.time = time;
    }

    public String getName ()
    {
        return name;
    }
    //public void setName(String name)
    {
        this.name = name;
    }

    public Marker getStartingPoint ()
    {
        return startingPoint;
    }
    //public void setStartingPoint(Marker startingPoint)
    {
        this.startingPoint = startingPoint;
    }

    public Marker getdestinationPoint ()
    {
        return destinationPoint;
    }
    //public void setdestinationPoint(Marker destinationPoint) { this.destinationPoint = destinationPoint; }

    public ArrayList<Marker> getWaypoints ()
    {
        return waypoints;
    }
    //public void setWaypoints(ArrayList<Marker> waypoints)
    {
        this.waypoints = waypoints;
    }

    public String getPolyline(){ return polyline; }

    public int getDistance ()
    {
        return distance;
    }
    //public void setDistance(int distance)
    {
        this.distance = distance;
    }

    public int getTime ()
    {
        return time;
    }
    //public void setTime(int time)
    {
        this.time = time;
    }

}
