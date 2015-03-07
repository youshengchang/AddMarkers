package com.example.gmapsapp;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

//import com.google.android.gms.location.LocationClient;

public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private static final int GPS_ERRORDIALOG_REQUEST = 9001;
	@SuppressWarnings("unused")
	private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9002;
	GoogleMap mMap;

	@SuppressWarnings("unused")
	private static final double SEATTLE_LAT = 47.60621,
	SEATTLE_LNG =-122.33207, 
	SYDNEY_LAT = -33.867487,
	SYDNEY_LNG = 151.20699, 
	NEWYORK_LAT = 40.714353, 
	NEWYORK_LNG = -74.005973;
	private static final float DEFAULTZOOM = 15;
	@SuppressWarnings("unused")
	private static final String LOGTAG = "Maps";
	
	//LocationClient mLocationClient;
    GoogleApiClient mLocationClient;

    Marker marker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (servicesOK()) {
			setContentView(R.layout.activity_map);

			if (initMap()) {
//				mMap.setMyLocationEnabled(true);
				//mLocationClient = new LocationClient(this, this, this);
				//mLocationClient.connect();
                buildGoogleApiClient();
			}
			else {
				Toast.makeText(this, "Map not available!", Toast.LENGTH_SHORT).show();
			}
		}
		else {
			setContentView(R.layout.activity_main);
		}

	}

    protected synchronized void buildGoogleApiClient() {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationClient.connect();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean servicesOK() {
		int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

		if (isAvailable == ConnectionResult.SUCCESS) {
			return true;
		}
		else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvailable, this, GPS_ERRORDIALOG_REQUEST);
			dialog.show();
		}
		else {
			Toast.makeText(this, "Can't connect to Google Play services", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private boolean initMap() {
		if (mMap == null) {
			SupportMapFragment mapFrag =
					(SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
			mMap = mapFrag.getMap();

            if(mMap != null){
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = getLayoutInflater().inflate(R.layout.info_window, null);
                        TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                        TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                        TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);
                        TextView tvSnippet = (TextView) v.findViewById(R.id.tv_snippet);

                        LatLng ll = marker.getPosition();
                        tvLocality.setText(marker.getTitle());
                        tvLat.setText("Latitude: " + ll.latitude);
                        tvLng.setText("Longitude: " + ll.longitude);
                        tvSnippet.setText(marker.getSnippet());

                        return v;
                    }
                });

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng ll) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;
                        try {
                            list = gc.getFromLocation(ll.latitude,ll.longitude,1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);
                        MainActivity.this.setMarker(add.getLocality(), add.getCountryName(),ll.latitude,ll.longitude);
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        String msg = marker.getTitle() + " (" + marker.getPosition().latitude + "," + marker.getPosition().longitude + ")";
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        return false;
                    }
                });

                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;
                        LatLng ll = marker.getPosition();
                        try {
                            list = gc.getFromLocation(ll.latitude,ll.longitude,1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Address add = list.get(0);
                        marker.setTitle(add.getLocality());
                        marker.setSnippet(add.getCountryName());
                        marker.showInfoWindow();
                    }
                });
            }
		}
		return (mMap != null);
	}

	private void gotoLocation(double lat, double lng,
			float zoom) {
		LatLng ll = new LatLng(lat, lng);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
		mMap.moveCamera(update);
	}

	public void geoLocate(View v) throws IOException {

		EditText et = (EditText) findViewById(R.id.editText1);
		String location = et.getText().toString();
		if (location.length() == 0) {
			Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
			return;
		}

		hideSoftKeyboard(v);
		
		Geocoder gc = new Geocoder(this);
		List<Address> list = gc.getFromLocationName(location, 1);
		Address add = list.get(0);
		String locality = add.getLocality();
		Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

		double lat = add.getLatitude();
		double lng = add.getLongitude();

		gotoLocation(lat, lng, DEFAULTZOOM);

        //Log.i(LOGTAG, "country: " + add.getCountryName());
        //Toast.makeText(this, "country: " + add.getCountryName(), Toast.LENGTH_SHORT).show();

        setMarker(locality, add.getCountryName(), lat, lng);

		
	}

    private void setMarker(String locality, String country, double lat, double lng){
        if(marker != null)
            marker.remove();

        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .position(new LatLng(lat,lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker))
                .draggable(true);
               // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        if(country.length() > 0)
            options.snippet(country);
        marker = mMap.addMarker(options);

    }
	private void hideSoftKeyboard(View v) {
		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){

		switch (item.getItemId()) {
		case R.id.mapTypeNone:
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
			break;
		case R.id.mapTypeNormal:
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			break;
		case R.id.mapTypeSatellite:
			mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			break;
		case R.id.mapTypeTerrain:
			mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			break;
		case R.id.mapTypeHybrid:
			mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			break;
		case R.id.gotoCurrentLocation:
            try {
                gotoCurrentLocation();
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();
		MapStateManager mgr = new MapStateManager(this);
		mgr.saveMapState(mMap);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MapStateManager mgr = new MapStateManager(this);
		CameraPosition position = mgr.getSavedCameraPosition();
		if (position != null) {
			CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
			mMap.moveCamera(update);
			//			This is part of the answer to the code challenge
			mMap.setMapType(mgr.getSavedMapType());
		}
		
	}

	protected void gotoCurrentLocation() throws IOException{
		
//		Location currentLocation = mLocationClient.getLastLocation();
//		if (currentLocation == null) {
//			Toast.makeText(this, "Current location isn't available", Toast.LENGTH_SHORT).show();
//		}
//		else {
//			LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//			CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
//			mMap.animateCamera(update);
//		}



        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mLocationClient);
        Geocoder gc = new Geocoder(this);

        List<Address> list = gc.getFromLocation(currentLocation.getLatitude(),currentLocation.getLongitude(), 1);

        Address add = list.get(0);
        String locality = add.getLocality();
        //Log.i(LOGTAG, "country: " + add.getCountryName());

        //Toast.makeText(this, "country: " + add.getCountryName(), Toast.LENGTH_SHORT).show();

        if(currentLocation == null){
            Toast.makeText(this, "The current location is not available", Toast.LENGTH_SHORT).show();
        }else{
            LatLng ll = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, DEFAULTZOOM);
            mMap.animateCamera(update);

            setMarker(locality, add.getCountryName(), currentLocation.getLatitude(),currentLocation.getLongitude());

        }

    }

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onConnected(Bundle arg0) {
//		Toast.makeText(this, "Connected to location service", Toast.LENGTH_SHORT).show();
	}

    @Override
    public void onConnectionSuspended(int i) {

    }



    @Override
    public void onLocationChanged(Location location) {

    }
}
