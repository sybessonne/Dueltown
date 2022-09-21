package com.dueltown.parametres;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dueltown.R;

public class notification extends AppCompatActivity {

    CheckBox son = null;
    CheckBox vibreur = null;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notif);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        son = findViewById(R.id.son);
        vibreur = findViewById(R.id.vibreur);

        preferences = getSharedPreferences("parametres", 0);
        editor = preferences.edit();

        //ecoute l'appuie sur les cases son et vibreur
        son.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("sons", son.isChecked());
                editor.apply();
            }
        });

        vibreur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("vibrations", vibreur.isChecked());
                editor.apply();
            }
        });
    }

    //pour afficher le switch dans la toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notification_switch, menu);
        // Get the action view used in your toggleservice item
        final MenuItem toggleservice = menu.findItem(R.id.notification_switch);
        final Switch actionView = (Switch) toggleservice.getActionView();

        //si l'utilisateur ne veut pas de notifications
        boolean notif = preferences.getBoolean("notifications", true);
        boolean so = preferences.getBoolean("sons", true);
        boolean vib = preferences.getBoolean("vibrations", true);

        //verification pour cocher ou decocher
        son.setChecked(so);
        vibreur.setChecked(vib);

        actionView.setChecked(notif);
        son.setEnabled(notif);
        vibreur.setEnabled(notif);

        //quand on appuie sur le switch
        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                son.setEnabled(isChecked);
                vibreur.setEnabled(isChecked);
                editor.putBoolean("notifications", isChecked);
                editor.apply();
            }
        });
        return true;
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
