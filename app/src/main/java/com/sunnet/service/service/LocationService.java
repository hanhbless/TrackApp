package com.sunnet.service.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sunnet.service.db.DatabaseHelper;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.log.Log;
import com.sunnet.service.task.request.RequestHelper;
import com.sunnet.service.task.vo.LocationPref;
import com.sunnet.service.util.SharedPreferencesUtility;
import com.sunnet.service.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class LocationService extends Service implements OnLocationUpdatedListener, OnGeofencingTransitionListener {
    private final long MIN_TIME_UPDATE = 2 * 60 * 1000; // 2 minutes
    private final float MIN_DISTANCE_UPDATE = 0; // 100m
    private String locationProvider = LocationManager.NETWORK_PROVIDER; // Or use LocationManager.GPS_PROVIDER

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {

    }

    @Override
    public void onLocationUpdated(Location location) {
        //makeUseOfNewLocation(location);
        Log.d("New Location: " + location.toString());
        LocationEntity entityNew = DatabaseHelper.genLocationEntity(location);
        getAddress(location, entityNew);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(LocationService.class.getName() + " onCreate");
        LocationGooglePlayServicesProvider provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);
        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.location(provider).start(this);
    }

    private void makeUseOfNewLocation(Location newLocation) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String jsonLocation = SharedPreferencesUtility.getInstance().getString(SharedPreferencesUtility.LOCATION_LAST, null);
        Location lastLocation = null;
        if (!Utils.isEmptyString(jsonLocation)) {
            try {
                LocationPref locationPref = new Gson().fromJson(jsonLocation, LocationPref.class);
                lastLocation = locationPref.getLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Location lastKnownLocation = lastLocation;
        Log.d("New Location: " + newLocation.toString());
        if (lastKnownLocation != null)
            Log.d("Last Location: " + lastKnownLocation.toString());
        if (isBetterLocation(newLocation, lastKnownLocation)) {
            LocationEntity entityNew = DatabaseHelper.genLocationEntity(newLocation);
            getAddress(newLocation, entityNew);
            if (Log.IS_DEBUG) {
                Toast.makeText(this, "New location! " + newLocation.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Get address of location
     */
    private void getAddress(Location newLocation, LocationEntity entity) {
        String TAG = LocationService.class.getName();
        String errorMessage = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(
                    Double.parseDouble(entity.getLatitude()),
                    Double.parseDouble(entity.getLongitude()),
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not_available";
            android.util.Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid lat long used";
            android.util.Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + entity.getLatitude() +
                    ", Longitude = " + entity.getLongitude(), illegalArgumentException);
        }

        //-- Store new location
        String jsonLocation = new Gson().toJson(new LocationPref(newLocation));
        SharedPreferencesUtility.getInstance().putString(
                SharedPreferencesUtility.LOCATION_LAST, jsonLocation);

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address found";
                android.util.Log.e(TAG, errorMessage);
            }
            entity.setAddress("not get address");
            entity.setPlace("not get place");
            entity.encrypt();
            DatabaseHelper.createLocation(entity);

            List<LocationEntity> entityList = new ArrayList<>();
            entityList.add(entity);
            RequestHelper.updateLocationToServer(entityList);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }

            android.util.Log.i(TAG, "address found");
            String addressStr = TextUtils.join(System.getProperty("line.separator"), addressFragments);
            entity.setAddress("(1) " + addressStr);
            entity.setPlace("(1) " + (addressFragments.size() > 0 ? addressFragments.get(0) : addressStr));
            entity.encrypt();
            DatabaseHelper.createLocation(entity);

            List<LocationEntity> entityList = new ArrayList<>();
            entityList.add(entity);
            RequestHelper.updateLocationToServer(entityList);
        }
    }

}
