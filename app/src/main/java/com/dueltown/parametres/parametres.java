package com.dueltown.parametres;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dueltown.BDD;
import com.dueltown.R;
import com.dueltown.connexion;
import com.dueltown.principal;

public class parametres extends AppCompatActivity{

    static parametres ActivityParametre;

    //initialise les element du layout
    Button compte = null;
    Button param = null;
    Button notif = null;
    Button deconnexion = null;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityParametre = this;

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        compte = findViewById(R.id.compte);
        param = findViewById(R.id.param);
        notif = findViewById(R.id.notif);
        deconnexion = findViewById(R.id.deconnexion);

        // On attribue un listener adapté aux vues qui en ont besoin
        compte.setOnClickListener(compteListener);
        param.setOnClickListener(paramListener);
        notif.setOnClickListener(notifListener);
        deconnexion.setOnClickListener(deconnexionListener);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(parametres.this, R.raw.bruit_bouton);
    }

    public static parametres getInstance() {
        return ActivityParametre;
    }

    @Override
    public void onBackPressed() {
        retour();
    }
    private void retour() {
        principal.getThread().setRunning(true);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_toolbar_parametres, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //quand on clic sur la fleche pour actualiser
        if (id == R.id.APropos) {
            Intent intent = new Intent(parametres.this, aPropos.class);
            startActivity(intent);
            return true;
        }
        else
        {
            retour();
        }

        return super.onOptionsItemSelected(item);
    }


    // pour le bouton mon compte
    private View.OnClickListener compteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(parametres.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(parametres.this, moncompte.class);
                startActivity(intent);
            }
        }
    };

    // pour le bouton parametre de l'appli
    private View.OnClickListener paramListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(parametres.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(parametres.this, paramAppli.class);
                startActivity(intent);
            }
        }
    };

    // pour le bouton notifications
    private View.OnClickListener notifListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            Intent intent = new Intent(parametres.this, notification.class);
            startActivity(intent);
        }
    };

    // Uniquement pour le bouton deconnexion
    private View.OnClickListener deconnexionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            confirmation();
        }
    };

    //boite de dialogue de confirmation de déconnexion
    public void confirmation() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox, null);
        dialogBuilder.setView(dialogView);

        TextView texte = dialogView.findViewById(R.id.texte);
        Button oui = dialogView.findViewById(R.id.boutonGauche);
        Button non = dialogView.findViewById(R.id.boutonDroit);

        texte.setText(R.string.confirmationDeconnexion);
        oui.setText(R.string.oui);
        non.setText(R.string.non);

        final AlertDialog alertDialog = dialogBuilder.show();

        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sons)
                {
                    sonClick.start();
                }

                alertDialog.dismiss();
                //suppression des preferences
                SharedPreferences personne = getSharedPreferences("personne", 0);
                SharedPreferences defisPasFinis = getSharedPreferences("defisPasFinis", 0);
                SharedPreferences defisFinis = getSharedPreferences("defisFinis", 0);
                SharedPreferences parametres = getSharedPreferences("parametres", 0);
                SharedPreferences avancementDefiDefi = getSharedPreferences("avancementDefi", 0);
                SharedPreferences nouveautes= getSharedPreferences("nouveautes", 0);
                personne.edit().clear().apply();
                defisPasFinis.edit().clear().apply();
                defisFinis.edit().clear().apply();
                parametres.edit().clear().apply();
                avancementDefiDefi.edit().clear().apply();
                nouveautes.edit().clear().apply();

                //retour a la page de connexion
                Intent intent = new Intent(parametres.this, connexion.class);
                parametres.this.finish();
                principal.getThread().setRunning(false);
                principal.getInstance().finish(); //supprime l'activité principale en meme temps
                startActivity(intent);
            }
        });

        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sons)
                {
                    sonClick.start();
                }

                alertDialog.dismiss();
            }
        });
    }
}


