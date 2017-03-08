package com.intelliviz.learngoogleservices;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener // needed to receive continuous updates
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private TextView mLongitudeView;
    private TextView mLatitudeView;
    private TextView mDetectedActivities;
    private Button mRequesetUpdatesButton;
    private Button mRemoveRequesetsUpdatesButton;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongitudeView = (TextView) findViewById(R.id.longitudeText);
        mLatitudeView = (TextView) findViewById(R.id.latitudeText);

        mRemoveRequesetsUpdatesButton = (Button) findViewById(R.id.remove_activity_updates_button);
        mRequesetUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
        mDetectedActivities = (TextView) findViewById(R.id.detectedActivities);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        // Disconnect the client invalidates it
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    // ConnectionCallbacks interface
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        // request updates from Location Services
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation == null) {
            mLongitudeView.setText(String.valueOf(mLastLocation.getLongitude()));
            mLatitudeView.setText(String.valueOf(mLastLocation.getLatitude()));
        }

        // request continuous updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    // OnConnectionFailedListener interface
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        mLongitudeView.setText(Double.toString(location.getLongitude()));
        mLatitudeView.setText(Double.toString(location.getLatitude()));
    }

    public void requestActivityUpdatesButtonHandler(View view) {
    }

    public void removeActivityUpdatesButtonHandler(View view) {
    }
}
