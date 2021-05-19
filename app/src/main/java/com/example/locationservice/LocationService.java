package com.example.locationservice;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service {
public String idName;
public String idIndex;
double latitude, longitude, speed;

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();
                speed = locationResult.getLastLocation().getSpeed();
                Log.d("TAG",  "Foreground location: " + latitude + ", " + longitude + ", "+ speed);
                try {
                    saveUserLocation(latitude, longitude, speed,  idIndex, idName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException(" Not jet implemented");
    }
    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivities(getApplicationContext(),
                0,
                new Intent[]{resultIntent},
                PendingIntent.FLAG_UPDATE_CURRENT);
         NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
        Log.d("TAG", "OK Location foreground started");
    }
    public void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
        Log.d("TAG", "jestem w stop");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Log.d("TAG", "jestem w onStart : " + action);
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    idName = (String) intent.getExtras().get("idName");
                    idIndex = (String) intent.getExtras().get("idIndex");
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return START_NOT_STICKY;
    }
    private void saveUserLocation(double latitude, double longitude , double speed ,String idIndex, String idName) throws IOException {
        Log.d("TAG",  "save: " + latitude + ", " + longitude + ", "+ speed);
        Long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        postHttpResponse(time, Double.toString(latitude), Double.toString(longitude), Double.toString(speed), idName, idIndex);
        BroadcastToFront (time, latitude, longitude);
    }
    public void postHttpResponse (Long time, String lat, String longitude, String s , String idName, String idIndex)  {
        String requestUrl = "https://busmapa.ct8.pl/saveToDB.php?time="+ time +
                "&lat="+ lat +
                "&longitude=" + longitude +
                "&s=" +s +
                "&idName=" + idName +
                "&idIndex=" + idIndex
                ;
        Log.d("TAG","http request " + requestUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("RESPONSE", String.valueOf(response));
            }
        }, new Response.ErrorListener() {
            private VolleyError error;

            @Override
            public void onErrorResponse(VolleyError error) {
                this.error = error;
                error.printStackTrace(); //log the error resulting from the request for diagnosis/debugging
            }
        })
                //This is for Headers If You Needed
        {
            @Override
            public Map<String, String> getHeaders () throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json; charset=UTF-8");
                return params;
            }
        };
        // Add the request to the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    };
    private void BroadcastToFront(Long time,  double latitude, double longitude) {
        final String mBroadcastStringAction = "BroadcastID1";
        Log.d("TAG",  "brodcast: " + latitude + ", " + longitude + ", "+ time);
        Intent intent = new Intent();
        intent.setAction(mBroadcastStringAction);
        intent.putExtra("lat", latitude);
        intent.putExtra("lon", longitude);
        intent.putExtra("time", time);
        sendBroadcast(intent);
    }
}
