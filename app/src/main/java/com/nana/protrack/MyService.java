package com.nana.protrack;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.MarshalHashtable;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;

public class MyService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000 * 60 * 1;
    private static final float LOCATION_DISTANCE = 0.15f;
    String latOnlockChangeStr = null;
    String longOnlockChangeStr = null;
    Boolean errored = false;
    String wsResponse = "";

    public Criteria criteria;
    public String bestProvider;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        public LocationListener(String provider) {
            Log.i(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            Double latOnLockChange = mLastLocation.getLatitude();
            Double longOnLockChange = mLastLocation.getLongitude();

            latOnlockChangeStr = String.valueOf(latOnLockChange);
            longOnlockChangeStr = String.valueOf(longOnLockChange);

            getLocation(latOnlockChangeStr, longOnlockChangeStr);

            Log.i(TAG, "Latitude On Change " + latOnlockChangeStr);
            Log.i(TAG, "Longitude On Change " + longOnlockChangeStr);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(TAG, "onProviderDisabled: " + provider);

            Double latOnLockChange = mLastLocation.getLatitude();
            Double longOnLockChange = mLastLocation.getLongitude();

            latOnlockChangeStr = String.valueOf(latOnLockChange);
            longOnlockChangeStr = String.valueOf(longOnLockChange);

            getLocation(latOnlockChangeStr, longOnlockChangeStr);

            Log.i(TAG, "Latitude onProviderDisabled " + latOnlockChangeStr);
            Log.i(TAG, "Longitude onProviderDisabled " + longOnlockChangeStr);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(TAG, "onProviderEnabled: " + provider);

            Double latOnLockChange = mLastLocation.getLatitude();
            Double longOnLockChange = mLastLocation.getLongitude();

            latOnlockChangeStr = String.valueOf(latOnLockChange);
            longOnlockChangeStr = String.valueOf(longOnLockChange);

            getLocation(latOnlockChangeStr, longOnlockChangeStr);

            Log.i(TAG, "Latitude On onProviderEnabled " + latOnlockChangeStr);
            Log.i(TAG, "Longitude On onProviderEnabled " + longOnlockChangeStr);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(TAG, "onStatusChanged: " + provider);

            Double latOnLockChange = mLastLocation.getLatitude();
            Double longOnLockChange = mLastLocation.getLongitude();

            latOnlockChangeStr = String.valueOf(latOnLockChange);
            longOnlockChangeStr = String.valueOf(longOnLockChange);

            getLocation(latOnlockChangeStr, longOnlockChangeStr);

            Log.i(TAG, "Latitude onStatusChanged " + latOnlockChangeStr);
            Log.i(TAG, "Longitude onStatusChanged " + longOnlockChangeStr);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = mLocationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on");
                /* Location location = this.mLocationManager.getLastKnownLocation("gps"); */
                String latStr = String.valueOf(location.getLatitude());
                String longStr = String.valueOf(location.getLongitude());
                Log.i(TAG, " LATITUDE UP => " + latStr);
                Log.i(TAG, " LONGITUDE UP => " + longStr);
            }

        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(mLocationManager.getBestProvider(criteria, true)).toString();

            //You can still do this if you like, you might get lucky:
            Location location = mLocationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                Log.e("TAG", "GPS is on DOWN --- ");
                /* Location location = this.mLocationManager.getLastKnownLocation("gps"); */
                String latStr = String.valueOf(location.getLatitude());
                String longStr = String.valueOf(location.getLongitude());
                Log.i(TAG, " LATITUDE DOWN => " + latStr);
                Log.i(TAG, " LONGITUDE DOWN => " + longStr);
            }

        } catch (SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void getLocation(String latitude, String longitude) {
        Log.i("On Method Get Location", "Latitude " + latitude + " Longitude " + longitude);
        if (!latitude.isEmpty() && !longitude.isEmpty()) {
            MyService.AsyncCallZkWS task = new MyService.AsyncCallZkWS();
            task.execute();
        }
    }

    private class AsyncCallZkWS extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            /* Call Web Method */
            Log.i("On AsyncCallZk WS", " --------- in up ------------- ");
            wsResponse = WebServices.invokeInsertLocationWS(latOnlockChangeStr, longOnlockChangeStr, "insertlocation");
            Log.i("On AsyncCallZk WS", " --------- in down ------------- ");
            return null;
        }

        @Override
        //Once WebService returns response
        protected void onPostExecute(Void result) {
            if (!errored) {
                //Based on Boolean value returned from WebService
                if (wsResponse != null && wsResponse.equalsIgnoreCase("Success")) {
                    String urlEp = "https://estim.co.id:8085/protrack";

                    Intent intentWebview = new Intent(MyService.this, WebViewActivity.class);
                    //Add your data to bundle
                    Bundle bundle = new Bundle();

                    bundle.putString("remember", "false");
                    bundle.putString("url", urlEp);
                    bundle.putString("wsresponse", wsResponse);

                    //Add the bundle to the intent
                    intentWebview.putExtras(bundle);
                    intentWebview.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentWebview);

                } else {
                    Intent loginIntent = new Intent(MyService.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                }
            }
            errored = false;
        }

        @Override
        //Make Progress Bar visible
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}