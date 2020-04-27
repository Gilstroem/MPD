package gcu.mpd.mpdapp;
/*   Nikolaj Alexander Gilstrøm - S1630425  */

import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Map extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private List<RoadworkRecord> records;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        // CONTROLS

        // Assign buttons, and set listener
        Button current_button = findViewById(R.id.current_button);
        current_button.setOnClickListener(this);
        Button planned_button = findViewById(R.id.planned_button);
        planned_button.setOnClickListener(this);
        Button incidents_button = findViewById(R.id.incidents_button);
        incidents_button.setOnClickListener(this);

        // Filter (call the filter method when search is submitted)
        EditText filterField = findViewById(R.id.filter);
        filterField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    filter();
                }
                return false;
            }
        });
    }


    /**
     * Runs when the map is ready.
     * Set initial position to Scotland and zoom in.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // enable zoom buttons (?)
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng scotland = new LatLng(56.8169 , -4.1826);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(scotland, 6.0f));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                new AlertDialog.Builder(Map.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle(marker.getTitle())
                        .setMessage(marker.getSnippet())
                        .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        });
    }

    public void updateMarkers(List<RoadworkRecord> records) {
        this.records = records;
        setMarkers(records);
    }

    public void setMarkers(List<RoadworkRecord> records) {
        mMap.clear();
        for ( RoadworkRecord record : records ) {
            if(record.location != null) {

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(record.location);
                markerOptions.title(record.title);
                markerOptions.snippet(record.description + "\n\nMore information: \n" + record.url);
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(getColorByDuration(record.duration)));

                mMap.addMarker(markerOptions);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.current_button:
                new RoadworkRetriever(this, RoadworkType.CURRENT).execute();
                break;
            case R.id.planned_button:
                new RoadworkRetriever(this, RoadworkType.PLANNED).execute();
                break;
            case R.id.incidents_button:
                new RoadworkRetriever(this, RoadworkType.INCIDENT).execute();
                break;
        }
    }

    private void filter() {
        final EditText filter_field = findViewById(R.id.filter);
        String filter_string = filter_field.getText().toString();

        if(!filter_string.equals("") && !filter_string.equals(" ")) {
            List<RoadworkRecord> filteredRecords = new ArrayList<>();

            for(RoadworkRecord record : this.records) {
                if(record.title.contains(filter_string)) {
                    filteredRecords.add(record);
                }
            }

            updateMarkers(filteredRecords);
        } else {
            new RoadworkRetriever(this, RoadworkType.CURRENT).execute();
        }
    }

    private float getColorByDuration(int duration) {
        if(duration > 3) {
            return BitmapDescriptorFactory.HUE_RED;
        } else if(duration > 1) {
            return BitmapDescriptorFactory.HUE_YELLOW;
        } else {
            return BitmapDescriptorFactory.HUE_GREEN;
        }
    }
}