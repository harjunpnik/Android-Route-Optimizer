package com.example.routeoptimizer;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showRoutes();
    }

    // Shows all the saved Routes in the listView and also makes the clickable
    public void showRoutes(){

        HashMap<String, Route> routes = FileIO.loadAccounts(this);

        String[] menuValues = new String[routes.size()];

        int i = 0;
        for(Route route : routes.values())
        {
            menuValues[i] = route.getName();
            i++;
        }

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, menuValues);

        final ListView lv = findViewById(R.id.routeListView);
        lv.setAdapter(itemsAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                String message = (String) lv.getItemAtPosition(i);
                intent.putExtra("route name", message);

                startActivity(intent);
                finish();
            }
        });

    }


    // OnClick of New Route button change view to MapsActivity
    public void onNewRoute(View v){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
        finish();
    }
}
