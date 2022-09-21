package com.dueltown.statistiques;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.dueltown.BDD;
import com.dueltown.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//todo afficher le pays de la ville

public class statsVilleJoueur extends AppCompatActivity {

    Dialog dialog;

    //recuperation des element du layout
    TextView ville = null;
    TextView nbJoueurs = null;
    TextView nbDefisJoues = null;
    TextView victoires = null;
    TextView defaites = null;
    TextView egalites = null;
    TextView villeGagnePlus = null;
    TextView nbGagnePlus = null;
    TextView villePerdPlus = null;
    TextView nbPerdPlus = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statsvillejoueur);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        ville = findViewById(R.id.ville);
        nbJoueurs = findViewById(R.id.nbJoueurs);
        nbDefisJoues = findViewById(R.id.nbDefisJoues);
        victoires = findViewById(R.id.victoires);
        defaites = findViewById(R.id.defaites);
        egalites = findViewById(R.id.egalites);
        villeGagnePlus = findViewById(R.id.villeGagnePlus);
        nbGagnePlus = findViewById(R.id.nbGagnePlus);
        villePerdPlus = findViewById(R.id.villePerdPlus);
        nbPerdPlus = findViewById(R.id.nbPerdPlus);

        ville.setText(this.getIntent().getExtras().getString("ville", getString(R.string.villeInconnuev2)));

        //creation du progress dialog
        creationProgressDialog();
        dialog.show();

        //recuperation des stattistiques
        recupStats();
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

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementStats);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    private void  recupStats(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //recuperation du pseudo du joueur
        SharedPreferences preferences = getSharedPreferences("personne", 0);
        String villeJoueur = preferences.getString("ville", "");

        BDD.script service = retrofit.create(BDD.script.class);
        service.statsvillejoueur("statsVilleJoueur", villeJoueur).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().toString().equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.echecRecuperationStats, Toast.LENGTH_LONG).show();
                }
                else {
                    nbJoueurs.setText(response.body().get(0).getNbJoueurs());
                    nbDefisJoues.setText(response.body().get(0).getNbDefisJoues());
                    victoires.setText(response.body().get(0).getNbVictoires());
                    defaites.setText(response.body().get(0).getNbDefaites());
                    egalites.setText(response.body().get(0).getNbEgalite());
                    villeGagnePlus.setText(response.body().get(0).getVilleGagnePlus());
                    nbGagnePlus.setText(response.body().get(0).getNbGagnePlus());
                    villePerdPlus.setText(response.body().get(0).getVillePerdPlus());
                    nbPerdPlus.setText(response.body().get(0).getNbPerdPlus());
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
}
