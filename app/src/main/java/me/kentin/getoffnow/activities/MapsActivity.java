package me.kentin.getoffnow.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.novoda.notils.logger.simple.Log;

import java.util.Date;
import java.util.List;

import me.kentin.getoffnow.R;
import me.kentin.getoffnow.Route;
import me.kentin.getoffnow.constants.HiddenConstants;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.EncodedQuery;
import retrofit.http.GET;

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationChangeListener, Callback<List<Route>> {

    private GoogleMap mMap;
    private LatLng mStart;
    private LatLng mEnd;

    private static final String API_ENDPOINT = "https://maps.googleapis.com/maps/api";
    private static final String MODE_TRANSIT = "transit";
    private static final boolean MODE_ALTERNATIVES = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mEnd = latLng;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public void onMyLocationChange(Location location) {
        mStart = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void startSearch(View v) {
        if (!checkPositions())
            return;
        Log.d("Start Search");
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT).setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        Routes routes = restAdapter.create(Routes.class);
        routes.getRoutes(mStart.latitude + "," + mStart.longitude, mEnd.latitude + "," + mEnd.longitude, HiddenConstants.API_KEY, (new Date()).getTime() / 1000,MODE_TRANSIT, MODE_ALTERNATIVES,this);
    }

    private boolean checkPositions() {
        if (mStart == null || (mStart.latitude == 0.0f || mStart.longitude == 0.0f)) {
            Toast.makeText(this, getString(R.string.error_start_pos), Toast.LENGTH_LONG).show();
            return false;
        } else if (mEnd == null || (mEnd.latitude == 0.0f || mEnd.longitude == 0.0f)) {
                Toast.makeText(this, getString(R.string.error_end_pos), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public void success(List<Route> routes, Response response) {

    }

    @Override
    public void failure(RetrofitError retrofitError) {

    }

    interface Routes {
        @GET("/directions/json")
        void getRoutes(@EncodedQuery("origin") String origin, @EncodedQuery("destination") String destination, @EncodedQuery("key") String apiKey, @EncodedQuery("departure_time") long departure_time, @EncodedQuery("mode") String transportMode, @EncodedQuery("alternatives") boolean alternatives, Callback<List<Route>> callback);
    }
}
