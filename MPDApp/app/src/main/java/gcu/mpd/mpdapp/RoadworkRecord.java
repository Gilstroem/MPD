package gcu.mpd.mpdapp;
/*   Nikolaj Alexander Gilstrøm - S1630425  */

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class RoadworkRecord {
    public String title;
    public String description;
    public LatLng location;
    public String url;
    public int duration;

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isValid() {
        if(title != null && description != null && location != null && url != null) {
            return true;
        } else {
            return false;
        }
    }

    @NonNull
    @Override
    public String toString() {
        if(location != null) {
            return title + ", " + description + ", " + location.toString() + ", " + url;
        } else {
            return title + ", " + description + ", " + "NO LOCATION" + ", " + url;
        }
    }
}
