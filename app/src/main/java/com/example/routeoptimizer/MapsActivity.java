package com.example.routeoptimizer;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    EditText startAddress;
    EditText destinationAddress;
    EditText waypointAddress;
    EditText routeName;
    TextView durationText;
    TextView distanceText;

    private Marker startingPointMarker;
    private Marker destinationPointMarker;
    private ArrayList<Marker> waypointMarkers = new ArrayList<>();
    private int distanceInMeters;
    private int timeInSeconds;
    private String polyline;

    private String googleApiKey = <YOUR_GOOGLE_API_KEY> ;
    private LatLng startingZoompoint;

    ArrayList<String>  waypointNames=new ArrayList<>();
    ArrayAdapter<String> waypointAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //  Sets variables by id
        startAddress = findViewById(R.id.startAddressTextInput);
        destinationAddress = findViewById(R.id.destinationAddressTextInput);
        waypointAddress = findViewById(R.id.waypointAddressTextInput);
        durationText = findViewById(R.id.durationTextView);
        distanceText = findViewById(R.id.distanceTextView);
        routeName = findViewById(R.id.routeNameTextInput);
        //  Sets starting zoom point to Helsinki
        startingZoompoint = new LatLng(60.192059, 24.945831);

        //  Takes intent and checks if any intent was sent
        Intent intent = getIntent();
        String loadedRouteName = intent.getStringExtra("route name");

        if(loadedRouteName != null){
            //  Loads routes if we clicked on a existing route
            HashMap<String, Route> routes = FileIO.loadAccounts(this);
            //  Sets starting variables
            routeName.setText(loadedRouteName);
            startingPointMarker = routes.get(loadedRouteName).getStartingPoint();
            destinationPointMarker = routes.get(loadedRouteName).getdestinationPoint();
            waypointMarkers = routes.get(loadedRouteName).getWaypoints();
            startAddress.setText(startingPointMarker.getName());
            destinationAddress.setText(destinationPointMarker.getName());

            //  Sets polyline to the saved polyline
            polyline = routes.get(loadedRouteName).getPolyline();
            //  Sets duration and distance
            timeInSeconds = routes.get(loadedRouteName).getTime();
            distanceInMeters = routes.get(loadedRouteName).getDistance();
            setDurationAndDistance();
            //  Loops through waypoints and saves it to the waypoinyNames list
            waypointNames = new ArrayList<>();
            for(Marker waypoint: routes.get(loadedRouteName).getWaypoints()){
                waypointNames.add(waypoint.getName());
            }
            //  Updates Waypoint ListView
            updateWaypointList();
            //  Updates starting zoom point to starting point
            startingZoompoint = startingPointMarker.getLatLng();
        }
    }

    //  Back button that takes you to the main page
    public void onBack(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    // Saves Route to file
    public void onSaveRoute(View v){
        HashMap<String, Route> routes = FileIO.loadAccounts(this);
        String name = routeName.getText().toString();
        Route tempRoute = new Route(name,startingPointMarker,destinationPointMarker,waypointMarkers,polyline, distanceInMeters, timeInSeconds);
        routes.put(name,tempRoute);
        FileIO.saveAccounts(routes,this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

     // When map is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Helsinki and move the camera
        //LatLng tempMarker = new LatLng(60.192059, 24.945831);
        //mMap.addMarker(new MarkerOptions().position(tempMarker).title("Start by adding start and destination address"));
        CameraPosition cameraMovement = CameraPosition.builder()
                .target(startingZoompoint)
                .zoom(10)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraMovement), 3500, null);
        //when ready it draws everything on the map
        drawOnMap();
    }

    //  Updates the ListView of waypoints
    public void updateWaypointList(){
        //  Loop through list and add order number to the front before showing it in the list view
        ArrayList<String> orderedWaypointNames = new ArrayList<>();
        for(int i = 0; i < waypointNames.size(); i++){
            orderedWaypointNames.add(i,  i+1  + ". " + waypointNames.get(i));
        }
        waypointAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, orderedWaypointNames);
        final ListView listView =(ListView) findViewById(R.id.listView);
        listView.setAdapter(waypointAdapter);
    }

    //  Draws on the map the markers and path in the right order
    public void drawOnMap(){
        //  CLEARS THE MARKERS AND REDRAWS THEM IN THE RIGHT ORDER
        mMap.clear();

        //  ADDS STARTING POINT MARKER IF THERE IS ONE
        if(startingPointMarker != null){
            LatLng startingPointLatLng = startingPointMarker.getLatLng();
            MarkerOptions startMarker = new MarkerOptions().position(startingPointLatLng).title("Start: " + startingPointMarker.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(startMarker);
        }

        //  ADDS DESTINATION POINT MARKER IF THERE IS ONE
        if(destinationPointMarker != null){
            LatLng DestinationPointLatLng = destinationPointMarker.getLatLng();
            MarkerOptions destinationMarker = new MarkerOptions().position(DestinationPointLatLng).title("Destination: " + destinationPointMarker.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(destinationMarker);
        }

        //  ADDS WAYPOINT MARKERS IF THERE ARE ANY
        if(waypointMarkers != null){
            int i = 1;
            for(Marker waypoint: waypointMarkers){
                LatLng waypointLatLng = waypoint.getLatLng();
                MarkerOptions waypointMarker = new MarkerOptions().position(waypointLatLng).title(i + ". " + waypoint.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                mMap.addMarker(waypointMarker);
                i++;
            }

        }

        //  DRAWS PATH IF THERE IS ONE SAVED
        if(polyline != null){
            List <LatLng> decodedPolyline = PolyUtil.decode(polyline);
            PolylineOptions polyLines = new PolylineOptions();
            polyLines.addAll(decodedPolyline);
            polyLines.width(10f);
            polyLines.color(Color.RED);
            mMap.addPolyline(polyLines);
        }
    }

    // This function calls the google directions api, orders waypoints and sets variables according to the new route.
    public void getDirections(){
        // if there is not a starting and destination point this wont run
        if(startingPointMarker != null && destinationPointMarker != null){
            try {
                // Formats string to fit query
                String waypointFormated = "";
                for(String wayPoint : waypointNames){
                    waypointFormated += wayPoint + "|";
                }

                //  Makes query
                JSONObject jsobj = new JSONObject(getDataFromApi("https://maps.googleapis.com/maps/api/directions/json?origin=" + startingPointMarker.getName()  + "&destination=" + destinationPointMarker.getName() + "&waypoints=optimize:true|" + waypointFormated + "&key="));

                JSONArray jsonWaypoints = jsobj.getJSONArray("routes").getJSONObject(0).getJSONArray("waypoint_order");

                //  Orders Waypoints in the shortest driving distance
                ArrayList<String>  tempWaypoints=new ArrayList<>();
                ArrayList<Marker> tempWaypointMarkers = new ArrayList<>();
                for(int i=0; i<jsonWaypoints.length(); i++){
                    int order = (int)jsonWaypoints.get(i);
                    tempWaypoints.add(waypointMarkers.get(order).getName());
                    tempWaypointMarkers.add(waypointMarkers.get(order));
                }
                waypointNames = tempWaypoints;
                waypointMarkers = tempWaypointMarkers;


                JSONArray jsonLegs = jsobj.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
                //  Calculates total distance in meters from all the "legs"
                int distance = 0;
                for(int i=0; i<jsonLegs.length(); i++){
                    distance += jsonLegs.getJSONObject(i).getJSONObject("distance").getDouble("value");
                }
                distanceInMeters = distance;

                //  Calculates total time in seconds from all the "legs"
                int duration = 0;
                for(int i=0; i<jsonLegs.length(); i++){
                    duration += jsonLegs.getJSONObject(i).getJSONObject("duration").getDouble("value");
                }
                timeInSeconds = duration;

                //  Sets the duration and distance text
                setDurationAndDistance();

                //  Saves the "polyline" (the path to draw)
                polyline = jsobj.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
            }
            catch (JSONException e) {
               Toast.makeText(getApplicationContext(), "Couldnt read directions data", Toast.LENGTH_LONG).show();
            }
        }
    }

    //  OnClick of set start it saves Marker and calls getDirections(), drawOnMap()
    public void onSetStart(View v){
        //  Set startingPointMarker
        startingPointMarker = getMarkerFromAddress(startAddress.getText().toString());
        getDirections();
        drawOnMap();

    }

    //  OnClick of set destination it saves Marker and calls getDirections(), drawOnMap()
    public void onSetDest(View v){
        //  Set destinationPointMarker
        destinationPointMarker = getMarkerFromAddress(destinationAddress.getText().toString());
        getDirections();
        drawOnMap();

    }

    //  OnClick of add waypoint it saves Marker and calls getDirections(), drawOnMap() and updateWaypointList()
    public void onAddWaypoint(View v){
        // If there are 10 or more waypoints, we will not allow any more to be saved.
        if(waypointNames.size() > 9){
            Toast.makeText(getApplicationContext(), "Max amount of waypoints is 10", Toast.LENGTH_LONG).show();
        }else{
            Marker temporaryMarker = getMarkerFromAddress(waypointAddress.getText().toString());
            waypointMarkers.add( temporaryMarker);
            waypointNames.add(temporaryMarker.getName());
            getDirections();
            drawOnMap();
            updateWaypointList();
        }
    }

    //  Sets duration and Distance Text
    public void setDurationAndDistance(){
        int overflowMinutes = timeInSeconds %3600;
        int hours = timeInSeconds / 3600;
        int minutes = overflowMinutes / 60;
        int seconds = overflowMinutes % 60;

        durationText.setText(hours + "h " + minutes + "m " + seconds + "s");
        distanceText.setText((distanceInMeters/1000) + "km");
    }

    //  Makes request to API and converts address to latitude, longitude and saves it as a Marker class
    public Marker getMarkerFromAddress(String searchAdress){
        Marker markerToSend = null;

        try {
            //  Makes query
            JSONObject jsobj = new JSONObject(getDataFromApi("https://maps.googleapis.com/maps/api/geocode/json?address=" + searchAdress  +"&key=" ));

            String address = jsobj.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            double lat = jsobj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            double lng = jsobj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");

            markerToSend = new Marker(address,lat,lng);
        }
        // Throw Exception
        catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Coulnd't add point on Map, Please write another.", Toast.LENGTH_LONG).show();
        }

        return markerToSend;
    }

    //  Executes Api query
    public String getDataFromApi(String query){
        String JSONresponse = new String();

        try {
            JSONresponse = new HTTPGet().execute(query + googleApiKey ).get();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }

        if(JSONresponse != null)
            return JSONresponse;
        else
            Toast.makeText(getApplicationContext(), "Couldnt find data", Toast.LENGTH_LONG).show();

        return null;
    }
}
