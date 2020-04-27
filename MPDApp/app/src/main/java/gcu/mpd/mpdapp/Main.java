package gcu.mpd.mpdapp;
/*   Nikolaj Alexander Gilstrøm - S1630425  */

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

public class Main extends FragmentActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Intent i = new Intent(this, Map.class);
        startActivity(i);
    }
}
