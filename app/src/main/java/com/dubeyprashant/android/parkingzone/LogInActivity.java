package com.dubeyprashant.android.parkingzone;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;


import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LogInActivity extends AppCompatActivity implements LocationListener {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private String email_id;
    private String TAG = "LoginActivity";
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String knownName;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private boolean isGpsOn = false;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private boolean passiveProvider_enabled = false;
    private Location location; // location
    private Intent main;
    private Profile profile;
    private LocationManager locationManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        callbackManager = CallbackManager.Factory.create();

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken currentToken) {

            }
        };
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                //profile = newProfile;
                nextActivity(newProfile);

            }
        };
        accessTokenTracker.startTracking();
        profileTracker.startTracking();

           /*Registering loginbutton for callback*/
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String userId = loginResult.getAccessToken().getUserId();

                /* here we want to get the user email id*/
                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        getUserEmailId(response.getJSONObject());
                    }
                });
                Bundle parameter = new Bundle();
                parameter.putString("fields", "first_name, last_name, email_id");
                graphRequest.setParameters(parameter);
                graphRequest.executeAsync();


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        nextActivity(profile);
       // Toast.makeText(LogInActivity.this, "on resume called", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
        finish();


    }

    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {


    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        callbackManager.onActivityResult(requestCode, responseCode, intent);

    }

    /* method to check whether the Gps is on or Not
    * @param Intent //to start the nextActivity after checking the Gps service
    * */

    private void checkGPSStatus() {

      /* if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
           ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                   MY_PERMISSIONS_REQUEST_LOCATION);*/
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            passiveProvider_enabled = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(LogInActivity.this);
            dialog.setMessage("GPS not enabled");
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);


                }
            });
            profileTracker.stopTracking();
            accessTokenTracker.stopTracking();
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    /*fetch user data ,check weather location provider  is on or not finally, go to userprofile activity
     *  */
    private void nextActivity(Profile profile) {
        if (profile != null) {
            checkGPSStatus();
            main = new Intent(LogInActivity.this, UserProfileActivity.class);
            main.putExtra("first_name", profile.getFirstName());
            main.putExtra("last_name", profile.getLastName());
            main.putExtra("image_url", profile.getProfilePictureUri(200, 200).toString());
            main.putExtra("email_id", email_id);

            // handler using to provide delay in starting new activity
            final Handler handler = new Handler();
            Toast.makeText(LogInActivity.this, "wait...,starting next Activity", Toast.LENGTH_LONG).show();
            handler.postDelayed(new Runnable() {
                String userAddress;

                @Override
                public void run() {
                   /* if ( ContextCompat.checkSelfPermission( LogInActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(LogInActivity.this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSIONS_REQUEST_LOCATION);*/
                    //userAddress = getAddressDetail(LogInActivity.this,getLocation());
                    startActivity(main);
                }
            }, 4000);

        }
    }


    /* Get user Location when the location service is on
    *
    * */
    private Location getLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            try {

                // if GPS Enabled get location using GPS Services

                if (gps_enabled) {
                    if (location == null) {

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        }
                    }

                }
                // if GPS Enabled get location using GPS Services
                if (network_enabled) {
                    if (location == null) {

                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }
                    }

                }
                Toast.makeText(LogInActivity.this, "loction is===" + location.toString(), Toast.LENGTH_SHORT);
            } catch (Exception e) {
            }

        }
        return location;
    }


    /*returns THE USER Address by using user location*/
    private String getAddressDetail(Context context, Location location) {

        //Set Address
        String completeAddress;
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {


                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d(TAG, "getAddress:  address" + address);
                Log.d(TAG, "getAddress:  city" + city);
                Log.d(TAG, "getAddress:  state" + state);
                Log.d(TAG, "getAddress:  postalCode" + postalCode);
                Log.d(TAG, "getAddress:  knownName" + knownName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return completeAddress = address;
    }

    /*returns THE USER email id*/
    private void getUserEmailId(JSONObject object) {
        try {

            email_id = object.getString("email_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
