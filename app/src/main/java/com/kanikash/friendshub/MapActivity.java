package com.kanikash.friendshub;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.kanikash.friendshub.Fragments.ImageFragment;
import com.kanikash.friendshub.Models.Place;
import com.kanikash.friendshub.Services.UploadImage;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        ClusterManager.OnClusterClickListener,
        ClusterManager.OnClusterItemClickListener{
    private SupportMapFragment mapFragment;
    private Button btn_post ;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private static String sUserId;
    private Location currentLocation;
    private int mostRecentMapUpdate;
    private final Map<String, Marker> mapMarkers = new HashMap<String, Marker>();
    private String selectedPostObjectId;
    private LocationRequest mLocationRequest;
    //private ClusterManager<PinItem> mClusterManager;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;
    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;
    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;
    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;
    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;
    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;
    public static final boolean APPDEBUG = false;
    // Debugging tag for the application
    public static final String APPTAG = "GeoMessage";
    private float radius = 250.0f;

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }

        btn_post = (Button) findViewById(R.id.btn_post);
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showAlertDialogForPoint(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));

                Intent intent = new Intent(MapActivity.this, AddMomentsActivity.class);
                //Intent intent = new Intent(MapActivity.this, PostActivity.class);
                intent.putExtra("Location", currentLocation);
                startActivity(intent);

            }
        });

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

    }

    private void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.d("TAG", "Anonymous login failed.");
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }

    private void startWithCurrentUser() {
        sUserId = ParseUser.getCurrentUser().getObjectId();
    }

    protected void loadMap(GoogleMap googleMap) {


        // Formulate image JSON
        /*String gpsLatitude;
        try {
            ExifInterface exif = new ExifInterface("https://drive.google.com/file/d/0BxYi96IcLKVROUZJbVpsUXlHUmFfZ2NQbTN4X1FNQjVCU0tV/view");
            //ivTestImage.setImageURI("");
            gpsLatitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            gpsLatitude = "abc";
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        map = googleMap;
        if (map != null) {
            // Map is ready
            //Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            //mClusterManager = new ClusterManager<PinItem>(this, map);
            //map.setOnMarkerClickListener(mClusterManager);

            map.setMyLocationEnabled(true);
            //map.setOnMapLongClickListener(this);
            map.setOnMarkerClickListener(this);
            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    //doMapQuery();
                }
            });

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();


            connectClient();
            currentLocation = map.getMyLocation();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();

        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
        connectClient();
    }

    /*
	 * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*
     * Handle results returned to the FragmentActivity by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mGoogleApiClient.connect();
                        break;
                }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            //Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
            //currentLocation = location;
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }

    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getIntent());
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }

    }

    public void onLocationChanged(Location location) {
        currentLocation = location;
        doMapQuery();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        // Get the image path from the image title
        final String imagePath = marker.getTitle().toString();
        // Load the bitmap of this image
        /*Target loadTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                // Show the dialog to display this image
                showImageDialog(bitmap, imagePath);
            }

            @Override
            public void onBitmapFailed(Drawable drawable) {
                Log.d("INFO", "Failed in loading the bitmap");
            }

            @Override
            public void onPrepareLoad(Drawable drawable) {
                Log.d("Info", "preparing to load");
            }
        };*/
        FragmentManager fm = getSupportFragmentManager();
        ImageFragment imgFrag = ImageFragment.newInstance(imagePath);
        imgFrag.show(fm, "img_show");
        //Picasso.with(getBaseContext()).load(imagePath).into(loadTarget);
        return true;
    }

    private void showImageDialog(Bitmap bmp, String imagePath) {
        // Show the image in the dialogFragment

    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        //Toast.makeText(getBaseContext(), "Kanika", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterItem clusterItem) {
        //Toast.makeText(getBaseContext(), "pin clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    private void cleanUpMarkers(Set<String> markersToKeep) {
        for (String objId : new HashSet<String>(mapMarkers.keySet())) {
            if (!markersToKeep.contains(objId)) {
                Marker marker = mapMarkers.get(objId);
                marker.remove();
                mapMarkers.get(objId).remove();
                mapMarkers.remove(objId);
            }
        }
    }

    private void doMapQuery()
    {
        final int myUpdateNumber = ++mostRecentMapUpdate;
//        Location myLoc = (currentLocation == null) ? lastLocation : currentLocation;
        Location myLoc = currentLocation;
        // If location info isn't available, clean up any existing markers
        if (myLoc == null) {
            cleanUpMarkers(new HashSet<String>());
            return;
        }
        final ParseGeoPoint myPoint = geoPointFromLocation(myLoc);
        // Create the map Parse query
        ParseQuery<Place> mapQuery = Place.getQuery();
        // Set up additional query filters
        mapQuery.whereWithinKilometers("location", myPoint, MAX_POST_SEARCH_DISTANCE);
        mapQuery.include("user");
        mapQuery.orderByDescending("createdAt");
        mapQuery.setLimit(MAX_POST_SEARCH_RESULTS);
        // Kick off the query in the background
        mapQuery.findInBackground(new FindCallback<Place>() {
            @Override
            public void done(List<Place> objects, ParseException e) {
                if (e != null) {
                    if (APPDEBUG) {
                        Log.d(APPTAG, "An error occurred while querying for map posts.", e);
                    }
                    return;
                }
        /*
         * Make sure we're processing results from
         * the most recent update, in case there
         * may be more than one in progress.
         */
                if (myUpdateNumber != mostRecentMapUpdate) {
                    return;
                }
                // Posts to show on the map
                Set<String> toKeep = new HashSet<String>();
                // Loop through the results of the search
                for (final Place post : objects) {
                    // Add this post to the list of map pins to keep
                    toKeep.add(post.getObjectId());
                    // Check for an existing marker for this post
                    Marker oldMarker = mapMarkers.get(post.getObjectId());
                    // Set up the map marker's location
                    MarkerOptions markerOpts =
                            new MarkerOptions().position(new LatLng(post.getLocation().getLatitude(), post
                                    .getLocation().getLongitude()));
                    // Set up the marker properties based on if it is within the search radius
                    if (post.getLocation().distanceInKilometersTo(myPoint) > radius * METERS_PER_FEET
                            / METERS_PER_KILOMETER) {
                        // Check for an existing out of range marker
                        if (oldMarker != null) {
                            if (oldMarker.getSnippet() == null) {
                                // Out of range marker already exists, skip adding it
                                continue;
                            } else {
                                // Marker now out of range, needs to be refreshed
                                oldMarker.remove();
                            }
                        }
                        // Display a red marker with a predefined title and no snippet
//                        markerOpts =
//                                markerOpts.title(post.getCaption()).icon(
//                                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//                        // Add a new marker
//                        Marker marker = mapFragment.getMap().addMarker(markerOpts);
//                        mapMarkers.put(post.getObjectId(), marker);
//                        if (post.getObjectId().equals(selectedPostObjectId)) {
//                            marker.showInfoWindow();
//                            selectedPostObjectId = null;
//                        }

                        final String path = post.getPhotoFile().getUrl();
                        ImageSize targetSize = new ImageSize(80, 50);
                        ImageLoader.getInstance().loadImage(path, targetSize, imageLoadingListener(post, path));

                    } else {
                        // Check for an existing in range marker
                        if (oldMarker != null) {
                            if (oldMarker.getSnippet() != null) {
                                // In range marker already exists, skip adding it
                                continue;
                            } else {
                                // Marker now in range, needs to be refreshed
                                oldMarker.remove();
                            }
                        }
                        // Display image marker of size 80*50
                        final String path = post.getPhotoFile().getUrl();
                        Target loadtarget;
                        loadtarget = new Target() {
                            @Override
                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                                LoadImagePicasso(bitmap, post, path);
                            }

                            @Override
                            public void onBitmapFailed(Drawable drawable) {

                            }

                            @Override
                            public void onPrepareLoad(Drawable drawable) {

                            }
                        };
                        Picasso.with(getBaseContext())
                                .load(path)
                                .resize(50, 80)
                                .into(loadtarget);
                    }
                }
                // Clean up old markers.
                cleanUpMarkers(toKeep);
            }
        });
    }

    public void LoadImagePicasso(Bitmap bmp, Place place, String path) {
        BitmapDescriptor defaultMarker = BitmapDescriptorFactory.fromBitmap(bmp);
        MarkerOptions markerOpts = new MarkerOptions().position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()));
        markerOpts = markerOpts.title(path)
                .snippet("Random Snippet")
                .icon(defaultMarker);
        // Add a new marker
        Marker marker = mapFragment.getMap().addMarker(markerOpts);
        mapMarkers.put(place.getObjectId(), marker);
        if (place.getObjectId().equals(selectedPostObjectId)) {
            marker.showInfoWindow();
            selectedPostObjectId = null;
        }
    }

    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int resultCode = intent.getIntExtra("resultCode", RESULT_CANCELED);
            String resultValue = intent.getStringExtra("resultValue");
            Toast.makeText(getBaseContext(), resultValue, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(serviceReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(UploadImage.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(serviceReceiver, filter);
    }
}
