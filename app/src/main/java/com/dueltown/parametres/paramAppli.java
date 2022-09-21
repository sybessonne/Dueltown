package com.dueltown.parametres;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dueltown.R;

public class paramAppli extends AppCompatActivity {

    Switch transition = null;
    Switch bruitages = null;

    SharedPreferences preferences = null;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paramappli);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        transition = findViewById(R.id.transition);
        bruitages = findViewById(R.id.bruitages);

        preferences = getSharedPreferences("parametres", 0);
        boolean transitionAuto = preferences.getBoolean("transitionAuto", true);
        boolean bruits = preferences.getBoolean("bruitages", true);

        transition.setChecked(transitionAuto); //met le parametre comme il faut
        bruitages.setChecked(bruits);

        editor = preferences.edit();

        //quand on appuie sur le switch
        //a chaque clique on change la valeur de transtion auto
        transition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("transitionAuto", isChecked);
                editor.apply();
            }
        });

        //a chaque clique on change la valeur de bruitage
        bruitages.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("bruitages", isChecked);
                editor.apply();
            }
        });
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

