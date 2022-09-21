package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**************EXPLICATION DES AVANCEMENTS*********/
/*
    LES AVANCEMENTS PERMETTENT DE DIMINUER LA TRICHE EN GARDANT OU LE JOUEUR EN EST DANS LE DEFI
    AVANCEMENTSCOREQUESTION PERMET DE SAVOIR LES REPONSES CORRECTES, INCORRECTS ET PAS REPONDUES
    STOCKE SOUS FORME DE STRING
    DE LA FORME "XXXXX" AVEC X UN NUMERO
    2 = PAS REPONDU
    1 = JUSTE
    0 = FAUX
*/


public class activityIntermediaireDefis extends AppCompatActivity {

    //pour pouvoir fermer l'activité a partir d'une autre
    static activityIntermediaireDefis ActivityIntermediaire;

    TextView defiNom = null;
    TextView dateDefi = null;
    TextView numQuest = null;
    TextView points = null;
    Button jouer = null;

    SharedPreferences preferences;

    Dialog dialog;

    String nomDefi = "";
    int score;
    int nbQuestionRepondues;

    int nbQuestionsMax = 5;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityintermediairedefis);
        ActivityIntermediaire = this;

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        defiNom = findViewById(R.id.defiNom);
        dateDefi = findViewById(R.id.dateDefi);
        numQuest = findViewById(R.id.numQuest);
        points = findViewById(R.id.points);
        jouer = findViewById(R.id.jouer);

        //recupere le nom du défi
        nomDefi =  this.getIntent().getExtras().getString("nomDefi", "");
        String infosDefis [] = nomDefi.split("/");
        defiNom.setText(infosDefis[1]);

        //affichage de la date bien comme  il faut
        SimpleDateFormat dateActuelle = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date d2 = new Date();
        try {
            d2 = dateActuelle.parse(String.valueOf(infosDefis[2]));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateJour = new SimpleDateFormat("dd");
        SimpleDateFormat dateMois = new SimpleDateFormat("MM");
        SimpleDateFormat dateHeure = new SimpleDateFormat("HH");
        SimpleDateFormat dateMinute = new SimpleDateFormat("mm");

        int jour = Integer.parseInt(dateJour.format(d2));
        int mois = Integer.parseInt(dateMois.format(d2));
        int heure = Integer.parseInt(dateHeure.format(d2));
        int minute = Integer.parseInt(dateMinute.format(d2));

        String [] nomMois = {getString(R.string.Janvier), getString(R.string.Février), getString(R.string.Mars),
                getString(R.string.Avril), getString(R.string.Mai),getString(R.string.Juin), getString(R.string.Juillet),
                        getString(R.string.Aout), getString(R.string.Septembre), getString(R.string.Octobre),
                                getString(R.string.Novembre), getString(R.string.Decembre)};

        if(minute < 10)
        {
            dateDefi.setText(getString(R.string.dateDefiV1, jour, nomMois[mois - 1], heure, minute));
        }
        else
        {
            dateDefi.setText(getString(R.string.dateDefiV2, jour, nomMois[mois - 1], heure, minute));
        }

        //recuperation de la shared preference qui contient l'avancement des questions du défis
        preferences = getSharedPreferences("avancementDefi", 0);
        nbQuestionRepondues = preferences.getInt(nomDefi + "avancement", -1);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(activityIntermediaireDefis.this, R.raw.bruit_bouton);

        if(nbQuestionRepondues == -1) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(nomDefi + "avancement", 0);
            editor.putInt(nomDefi + "avancementScore", 0);
            editor.putString(nomDefi + "avancementScoreQuestions", "22222");
            editor.apply();

            //affichage des points et du nombre de question deja répondu
            numQuest.setText(getString(R.string.questionsRepondues, 0));
            points.setText(getString(R.string.points, 0));

            jouer.setText(R.string.jouer);
        }
        else {
            score = preferences.getInt(nomDefi + "avancementScore", 0);

            //affichage des points et du nombre de question deja répondu
            numQuest.setText(getString(R.string.questionsRepondues, nbQuestionRepondues));
            points.setText(getString(R.string.points, score));

            if(nbQuestionRepondues == nbQuestionsMax) {
                jouer.setText(R.string.synchroniser);
            }
            else {
                jouer.setText(R.string.jouer);
            }
        }

        jouer.setOnClickListener(jouerListener);
    }

    //pour la clique sur la flèche retour
    public boolean onOptionsItemSelected(MenuItem item){
        retour();
        return true;
    }

    @Override
    public void onBackPressed() {
        retour();
    }

    private void retour() {
        activityIntermediaireDefis.this.finish();
    }

    // pour le bouton jouer
    private View.OnClickListener jouerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            if(jouer.getText().toString().equals(getString(R.string.synchroniser))) {
                //on synchronise
                envoiResultatRetrofit();
            }
            else {
                //on joue les questions du défis
                Intent intent = new Intent(activityIntermediaireDefis.this, questionsdefi.class);
                //pour passer le nom du defi dans l'activite des questions
                intent.putExtra("nomDefi", nomDefi);
                startActivityForResult(intent, 0);
            }
        }
    };

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementEnvoiResultat);
        builder.setCancelable(false);
        dialog = builder.create();
    }


    private void envoiResultatRetrofit(){
        //creation du progress dialog
        creationProgressDialog();
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //recuperation du pseudo du joueur
        SharedPreferences p = getSharedPreferences("personne", 0);
        String pseudo = p.getString("pseudo", "");
        String mdp = p.getString("mdp", "");

        //recuperation de l'avancement du joueur
        String majavancementScore = preferences.getString(nomDefi + "avancementScoreQuestions", "22222");

        BDD.script service = retrofit.create(BDD.script.class);
        service.scorefindefi("scorefindefiV2", pseudo, mdp, String.valueOf(score), nomDefi, majavancementScore).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("problemeinconnu")) {
                    Toast.makeText(getApplicationContext(), R.string.synchronisationEchec, Toast.LENGTH_LONG).show();
                }
                else {
                    //suppression des avancements
                    preferences.edit().remove(nomDefi + "avancement").apply();
                    preferences.edit().remove(nomDefi + "avancementScore").apply();
                    preferences.edit().remove(nomDefi + "avancementScoreQuestions").apply();

                    //retour à l'activité principale
                    Intent intent = new Intent(activityIntermediaireDefis.this, principal.class);
                    principal.getInstance().finish();
                    principal.getThread().setRunning(false);

                    //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                    //et on la ferme dans l apage principale
                    //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                    SharedPreferences preferences = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("activityIntermediaireOuverte", true);
                    editor.apply();

                    startActivity(intent);
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

    public static  activityIntermediaireDefis getInstance(){
        return ActivityIntermediaire;
    }
}