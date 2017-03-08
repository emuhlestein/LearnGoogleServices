package com.intelliviz.learngoogleservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener // needed to receive continuous updates
{
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private ActivityDetectionBroadcastReceiver mBroadcastReceiver;
    private Location mLastLocation;
    private TextView mLongitudeView;
    private TextView mLatitudeView;
    private TextView mStatusText;
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
        mStatusText = (TextView) findViewById(R.id.detectedActivities);
        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
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

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.BROADCAST_ACTION));
        super.onResume();
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

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public String getActivityString(int detectedActivityType) {
        Resources resources = this.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        public final String TAG = ActivityDetectionBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<DetectedActivity> detectedActivities = intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            StringBuilder sb = new StringBuilder();
            for(DetectedActivity detectedActivity : detectedActivities) {
                int type = detectedActivity.getType();
                int confidence = detectedActivity.getConfidence();
                sb.append(getActivityString(type) + " " + confidence + "%\n");
            }
            mStatusText.setText(sb.toString());
        }
    }
}
