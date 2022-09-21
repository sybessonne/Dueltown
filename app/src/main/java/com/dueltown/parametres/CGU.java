package com.dueltown.parametres;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.dueltown.R;

public class CGU extends AppCompatActivity {

    TextView texte1 = null;
    TextView texte2 = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conditions_generales_utilisation);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        texte1 = findViewById(R.id.texte1);
        texte2 = findViewById(R.id.texte2);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        texte1.setText(getString(R.string.conditionUtilisationS1, getString(R.string.app_name)));
        texte2.setText(getString(R.string.conditionUtilisationS2, getString(R.string.app_name)));
    }

    //pour l'appuie sur la touche de retour
    public boolean onOptionsItemSelected(MenuItem item){
        retour();
        return true;
    }

    @Override
    public void onBackPressed() {
        retour();
    }
    private void retour() {
        this.finish();
    }
}
