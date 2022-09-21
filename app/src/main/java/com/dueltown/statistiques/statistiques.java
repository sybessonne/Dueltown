package com.dueltown.statistiques;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import com.dueltown.BDD;
import com.dueltown.R;
import com.dueltown.principal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//rajouter une ligne classement dans le pays du joueur
//todo rajouter un classement des joueurs par ville
public class statistiques extends AppCompatActivity {

    //recuperation des element du layout
    TextView nomJoueur = null;
    TextView villeJoueur = null;
    TextView ptsJoueur = null;
    TextView defisJoues = null;
    TextView moyenne = null;
    TextView rgJville = null;
    TextView rgJFrance = null;
    TextView questsoum = null;
    TextView questval = null;

    SharedPreferences preferences;
    String pseudo;
    String ville;

    Dialog dialog;
    SwipeRefreshLayout swipeRefresh = null;

    Button classementVille = null;
    Button statsVille = null;
    Button classementJoueurVille = null;
    Button classementJoueurFrance = null;
    Button classementSemaine = null;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistiques);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //creation de la progress dialog
        creationProgressDialog();
        dialog.show();

        // On récupère toutes les vues dont on a besoin
        swipeRefresh = findViewById(R.id.swipeRefresh);
        nomJoueur = findViewById(R.id.nomJoueur);
        villeJoueur =  findViewById(R.id.villeJoueur);
        ptsJoueur = findViewById(R.id.pointsJoueur);
        defisJoues = findViewById(R.id.defisjoues);
        moyenne = findViewById(R.id.moyenne);
        rgJville = findViewById(R.id.rangJVille);
        rgJFrance = findViewById(R.id.rangJfrance);
        questsoum = findViewById(R.id.questsoum);
        questval = findViewById(R.id.questval);

        //gestion du swipe refresh layout
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recupStats();
            }
        });

        //couleurs pour le refresh layout
        swipeRefresh.setColorSchemeResources(R.color.couleur1,
                R.color.couleur2,
                R.color.couleur3,
                R.color.couleur4);

        //recuperation du pseudo du joueur
        preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");
        ville = preferences.getString("ville", "");

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(statistiques.this, R.raw.bruit_bouton);

        classementVille = (Button) findViewById(R.id.classementVille);
        //classementJoueurVille = (Button) findViewById(R.id.classementJoueurVille);
        //classementJoueurFrance = (Button) findViewById(R.id.classementJoueurFrance);
        classementSemaine = findViewById(R.id.classementSemaine);
        statsVille = findViewById(R.id.statsVille);

        if(ville.equals(""))
        {
            statsVille.setText(getString(R.string.statsVille, "votre ville"));
        }
        else
        {
            statsVille.setText(getString(R.string.statsVille, ville));
        }

        //recuperation des statistiques
        recupStats();

        classementVille.setOnClickListener(classementVilleListener);
        statsVille.setOnClickListener(statsVilleListener);
        //classementJoueurVille.setOnClickListener(classementJoueurVilleListener);
        //classementJoueurFrance.setOnClickListener(classementJoueurFranceListener);
        classementSemaine.setOnClickListener(classementSemaineListener);
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

    //pour le classement de la semaine
    private View.OnClickListener classementSemaineListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(statistiques.this, classementSemaine.class);
                startActivity(intent);
            }
        }
    };

    //pour le classement de la semaine
    private View.OnClickListener statsVilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(statistiques.this, statsVilleJoueur.class);
                intent.putExtra("ville", ville);
                startActivityForResult(intent, 0);
            }
        }
    };


    // pour le bouton classement ville
    private View.OnClickListener classementVilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }
            	
                Intent intent = new Intent(statistiques.this, classementVille.class);
                startActivity(intent);
            }
        }
    };

    //todo : ajouter cela lors d'une mise a jour de l'app
   /* // pour le bouton classement ville
    private View.OnClickListener statsVilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(statistiques.this, statsVilleJoueur.class);
                startActivity(intent);
            }
        }
    };

    // pour le bouton classement des joueurrs dans la ville
    private View.OnClickListener classementJoueurVilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(statistiques.this, classementJoueursVille.class);
                startActivity(intent);
            }
        }
    };

    // pour le bouton classement des joueurs dans la france
    private View.OnClickListener classementJoueurFranceListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(statistiques.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(statistiques.this, classementJoueursFrance.class);
                startActivity(intent);
            }
        }
    };*/

    private void  recupStats(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.stats("stats", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().toString().equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.echecRecuperationStats, Toast.LENGTH_LONG).show();
                }
                else {
                    //recupere la ville du joeuur
                    String ville = preferences.getString("ville", "");

                    //remplit les textview
                    nomJoueur.setText(pseudo);
                    villeJoueur.setText(ville);
                    ptsJoueur.setText(response.body().get(0).getPtJoueur());
                    defisJoues.setText(response.body().get(0).getNbDefisJouees());
                    moyenne.setText(response.body().get(0).getMoyenneJoueur() + " / 5");
                    rgJville.setText(response.body().get(0).getRangJoueurVille() + " / " + response.body().get(0).getNbMembresVille());
                    rgJFrance.setText(response.body().get(0).getRangJoueurFrance() + " / " + response.body().get(0).getNbMembresFrance());
                    questsoum.setText(response.body().get(0).getQuestsoum());
                    questval.setText(response.body().get(0).getQuestval());
                }

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialog.dismiss();

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }
}
