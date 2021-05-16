package com.example.locationservice;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    TextView t1;
    TextView t2;
    TextView t3;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<ExampleItem> exampleList = new ArrayList <> ();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView= findViewById(R.id.recycleView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter=new ExampleAdapter(exampleList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        findViewById(R.id.buttonStartLocationUpdates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                            , REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    startLocationService();
                }
            }
        });
        findViewById(R.id.buttonStopLocationUpdates).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
                Log.d ("TAG", "klik stop");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationService();
            } else {
                Toast.makeText(this, "Permission denied! ", Toast.LENGTH_SHORT).show();
            }

        }
    }
    public boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        Log.d("TAG","isRunning: " + activityManager);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                Log.d("TAG" , "activityManager: "+ activityManager.getRunningServices(Integer.MAX_VALUE));
                Log.d("TAG", "if location service " + LocationService.class.getName().equals(service.service.getClassName()));                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        Log.d ("TAG" , "service foreground: " + service.foreground);return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
    public void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service started ", Toast.LENGTH_SHORT).show();
        }
    }
    public void stopLocationService() {
        Log.d("TAG", "stop");
        Toast.makeText(this, "jestem w stop " +isLocationServiceRunning() , Toast.LENGTH_SHORT).show();
//        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
//        }

    }
    final String mBroadcastStringAction = "BroadcastID1";
    public IntentFilter filter = new IntentFilter(mBroadcastStringAction);
    public BroadcastReceiver broadcast = new BroadcastReceiver(){
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Toast.makeText(arg0, "Masz nowe dane z GPS", Toast.LENGTH_SHORT).show();
            Log.d("TAG", "msg " +arg1 + " "
                    +arg1.getExtras().getDouble("lon") +" "
                    +arg1.getExtras().getDouble("lat")
            );
            if (arg1.getExtras() != null) {
                double lon = arg1.getExtras().getDouble("lon");
                double lat = arg1.getExtras().getDouble("lat");
                Log.d("TAG", "MainActivity: "+lat +" "+ lon  );
                setData(lat,lon);
            }

        }


    };
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcast, filter);
    }
    @Override
    public void onPause() {
        unregisterReceiver(broadcast);
        // pamiętaj żeby wyrejestrować receivera !
        super.onPause();
    }
   public void setData(double lat, double lon){
        //setContentView(R.layout.activity_main);
        exampleList.add(0, new ExampleItem(R.drawable.ic_android,"Latitude: "+ Double.toString(lat), "Longitude: "+ Double.toString(lon)));
       // mRecyclerView= findViewById(R.id.recycleView);
        //mRecyclerView.setHasFixedSize(true);
        //mLayoutManager = new LinearLayoutManager(this);
        //mAdapter=new ExampleAdapter(exampleList);
        //mRecyclerView.setLayoutManager(mLayoutManager);
        //mRecyclerView.setAdapter(mAdapter);
       mAdapter.notifyDataSetChanged();
       t1 = (TextView) findViewById(R.id.lat);
       t2 = (TextView) findViewById(R.id.lon);
       t3 = (TextView) findViewById(R.id.nbSavedPositions);
       t1.setText(Double.toString(lat));
       t2.setText(Double.toString(lon));
       t3.setText("" + exampleList.size());
    }
}