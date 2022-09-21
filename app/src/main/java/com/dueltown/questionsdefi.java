package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class questionsdefi extends AppCompatActivity {

    static questionsdefi ActivityQuestionsDefi;

    //recuperation des element du layout
    LinearLayout layoutQuestion = null;
    TextView villeAdverse = null;

    Button q1 = null;
    Button q2 = null;
    Button q3 = null;
    Button q4 = null;
    Button q5 = null;

    TextView question = null;
    ImageView decompte = null;

    Button reponse1 = null;
    Button reponse2 = null;
    Button reponse3 = null;
    Button reponse4 = null;
    ProgressBar progressionBar = null;

    SharedPreferences preferencesAvancement;
    SharedPreferences preferencesDuDefi;

    String nomDefi;
    String majavancementScore;
    int score;
    int numQuestion;
    private static int nbQuestions = 4;

    //pour gerer un seul click
    boolean possibiliteJouer = true;

    //pour dire quand il n'y a plus de questions
    int nbQuestionsTotales = 5;

    //pour les chiffres aleatoires
    Random r = new Random();
    ArrayList<Integer> tab_result = new ArrayList();

    //gestion du temps
    int tempsQuestionMax = 12;
    int tempsQuestionMin = 0;
    Timer t;
    TimerTask task;
    int time;

    //decompte entre question
    int intervalleQuestion = 3; //en secondes
    CountDownTimer decompteTimer;
    boolean transitionAuto;

    Dialog dialog;

    //pour le son
    MediaPlayer sonReponseJuste;
    MediaPlayer sonReponseFausse;
    boolean sons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionsdefi);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityQuestionsDefi = this;

        //recupere le nom du défi courant
        nomDefi =  this.getIntent().getExtras().getString("nomDefi", "");

        //on recupere les elements du layout
        layoutQuestion = findViewById(R.id.layoutQuestion);
        decompte = findViewById(R.id.decompte);
        decompte.setVisibility(View.INVISIBLE);
        villeAdverse =  findViewById(R.id.villeAdverse);

        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);

        question = findViewById(R.id.question);
        reponse1 = findViewById(R.id.reponse1);
        reponse2 = findViewById(R.id.reponse2);
        reponse3 = findViewById(R.id.reponse3);
        reponse4 = findViewById(R.id.reponse4);
        progressionBar = findViewById(R.id.progressionBar);

        String villeadverse [] = nomDefi.split("/");
        villeAdverse.setText(villeadverse[1]);

        //definit les valuers min et max de la progress bar
        progressionBar.setMax(tempsQuestionMax);
        //progressionBar.setMin(tempsQuestionMin); // Cette ligne ne fonctionne pas sur certains téléphones

        //met le tab_result a 0
        tab_result.add(0);
        tab_result.add(0);
        tab_result.add(0);
        tab_result.add(0);

        //on récupère l'avancement des questions du défi et on gère la question a afficher en conséquence
        preferencesDuDefi = getSharedPreferences("defisPasFinis", 0);
        preferencesAvancement = getSharedPreferences("avancementDefi", 0);
        numQuestion = preferencesAvancement.getInt(nomDefi + "avancement", -1);

        if(numQuestion == -1) {
            this.finish();
        }
        else {
            score = preferencesAvancement.getInt(nomDefi + "avancementScore", 0);
            majavancementScore = preferencesAvancement.getString(nomDefi + "avancementScoreQuestions", "22222");
            numQuestion++;
        }

        //recuperation du boolean pour la transition automatique
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        transitionAuto = pref.getBoolean("transitionAuto", true);
        sons = pref.getBoolean("bruitages", true);

        //charge les sons
        if(sons)
        {
            sonReponseJuste = MediaPlayer.create(questionsdefi.this, R.raw.bonne_reponse);
            sonReponseFausse = MediaPlayer.create(questionsdefi.this, R.raw.mauvaise_reponse);
        }

        //si on continu un defi commencé avant, on initialise les boutons indices avec les bonnes couleurs
        initBoutonIndices();

        //initialisation du decompte inter question
        decompteTimer =  new CountDownTimer((intervalleQuestion + 1) * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                switch((int) millisUntilFinished / 1000)
                {
                    case 3:
                        decompte.setImageDrawable(questionsdefi.this.getResources().getDrawable(R.drawable.three));
                        break;
                    case 2:
                        decompte.setImageDrawable(questionsdefi.this.getResources().getDrawable(R.drawable.two));
                        break;
                    case 1:
                        decompte.setImageDrawable(questionsdefi.this.getResources().getDrawable(R.drawable.one));
                        break;
                }
            }
            public void onFinish() {
                questionSuivante();
            }
        };

        //affiche les réponses aleatoirement
        genereReponsesAlea();

        // On attribue un listener adapté aux vues qui en ont besoin
        layoutQuestion.setOnClickListener(layoutListener);
        layoutQuestion.setClickable(false);
        reponse1.setOnClickListener(reponse1Listener);
        reponse2.setOnClickListener(reponse2Listener);
        reponse3.setOnClickListener(reponse3Listener);
        reponse4.setOnClickListener(reponse4Listener);
    }

    //pour supprimer l'activité a partir de la page principale
    public static questionsdefi getInstance() {
        return ActivityQuestionsDefi;
    }

    //click sur le linear layout principal
    private View.OnClickListener layoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            questionSuivante();
        }
    };

    private void questionSuivante()
    {
        decompte.setVisibility(View.INVISIBLE);
        decompteTimer.cancel(); // arrete le decompte

        //arrete le son s'il est en train de se jouer
        if(sons)
        {
            if(sonReponseJuste.isPlaying())
            {
                sonReponseJuste.pause();
                sonReponseJuste.seekTo(0);
            }
            else if(sonReponseFausse.isPlaying())
            {
                sonReponseFausse.pause();
                sonReponseFausse.seekTo(0);
            }
        }

        genereReponsesAlea();

        layoutQuestion.setClickable(false);
        reponse1.setClickable(true);
        reponse2.setClickable(true);
        reponse3.setClickable(true);
        reponse4.setClickable(true);

        reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.ripple_button_question));
        reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.ripple_button_question));
        reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.ripple_button_question));
        reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.ripple_button_question));

        possibiliteJouer = true;
    }

    //pour le bouton reponse 1
    private View.OnClickListener reponse1Listener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(possibiliteJouer){
                possibiliteJouer = false;
                augmenteScore(1);
            }
        }
    };

    //pour le bouton reponse 2
    private View.OnClickListener reponse2Listener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(possibiliteJouer){
                possibiliteJouer = false;
                augmenteScore(2);
            }
        }
    };

    //pour le bouton reponse 3
    private View.OnClickListener reponse3Listener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(possibiliteJouer){
                possibiliteJouer = false;
                augmenteScore(3);
            }
        }
    };

    //pour le bouton reponse 4
    private View.OnClickListener reponse4Listener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(possibiliteJouer){
                possibiliteJouer = false;
                augmenteScore(4);
            }
        }
    };

    private void initBoutonIndices(){
        for(int i = 1; i <= nbQuestionsTotales; i++)
        {
            if(majavancementScore.charAt(i - 1) == '0')
            {
                switch (i){
                    case 1:
                        q1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                        break;
                    case 2:
                        q2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                        break;
                    case 3:
                        q3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                        break;
                    case 4:
                        q4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                        break;
                    case 5:
                        q5.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                        break;
                }
            }
            else if (majavancementScore.charAt(i - 1) == '1')
            {
                switch (i){
                    case 1:
                        q1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                        break;
                    case 2:
                        q2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                        break;
                    case 3:
                        q3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                        break;
                    case 4:
                        q4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                        break;
                    case 5:
                        q5.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                        break;
                }
            }
            if(majavancementScore.charAt(i - 1) == '2')
            {
                break;
            }
        }
    }

    private void genereReponsesAlea() {
        //si on a afficher les 5 questions on envoit le resultat du defi à la base de donnée
        if (numQuestion == 6)
        {
            reponse1.setClickable(false);
            reponse2.setClickable(false);
            reponse3.setClickable(false);
            reponse4.setClickable(false);

            //arrete le decompte
            decompteTimer.cancel();
            decompte.setVisibility(View.INVISIBLE);

            //fermer le timer et envoit des résultats
            t.cancel();
            task.cancel();
            envoiResultatRetrofit();
        }
        else
        {
            //remet tout a 0
            tab_result.set(0, 0);
            tab_result.set(1, 0);
            tab_result.set(2, 0);
            tab_result.set(3, 0);

            //generation des nombre aleatoire pour placer les questions
            int var = 0;
            int nb_a_tirer = 4;
            int nombrealea;

            while (nb_a_tirer != 0)
            {
                nombrealea = 1 + r.nextInt(4);
                if (!tab_result.contains(nombrealea))
                {
                    tab_result.set(var, nombrealea);
                    var++;
                    nb_a_tirer--;
                }
            }

            //on ecrit la premiere question et les reponses
            String quest = preferencesDuDefi.getString(nomDefi + "question" + numQuestion, "");
            String rep1 = preferencesDuDefi.getString(nomDefi + "reponse" + tab_result.get(0) + "" + numQuestion, "");
            String rep2 = preferencesDuDefi.getString(nomDefi + "reponse" + tab_result.get(1) + "" + numQuestion, "");
            String rep3 = preferencesDuDefi.getString(nomDefi + "reponse" + tab_result.get(2) + "" + numQuestion, "");
            String rep4 = preferencesDuDefi.getString(nomDefi + "reponse" + tab_result.get(3) + "" + numQuestion, "");

            //affichage des differents elements
            question.setText(quest);
            reponse1.setText(rep1);
            reponse2.setText(rep2);
            reponse3.setText(rep3);
            reponse4.setText(rep4);

            //mise a jour de l'avancement après chaque création de question pour empecher la triche
            SharedPreferences.Editor editor = preferencesAvancement.edit();
            editor.putInt(nomDefi + "avancement", numQuestion);
            editor.putInt(nomDefi + "avancementScore", score);

            majavancementScore = changeChar(majavancementScore, numQuestion, '0');

            editor.putString(nomDefi + "avancementScoreQuestions", majavancementScore);
            editor.apply();

            numQuestion++;

            time = tempsQuestionMax;
            progressionBar.setProgress(tempsQuestionMin);
            startTimer();
        }
    }

    public String changeChar(String chaine, int idx, char monCharRempl) {
        char[] tab = chaine.toCharArray();
        tab[idx - 1] = monCharRempl;
        return String.valueOf(tab);
    }

    private void augmenteScore (int numBouton){
        //si c'est juste on affiche un vert sur le bouton
        if (tab_result.get(numBouton - 1).equals(1))
        {
            //on joue le son
            if(sons)
            {
                sonReponseJuste.start();
            }

            score++;
            switch(numBouton) {
                case 1:
                    reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
                case 2:
                    reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;

                case 3:
                    reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;

                case 4:
                    reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
            }

            //affichage de la couleur du bouton indice
            couleurBoutonIndice(true);

            //met a jour l'indice de la question dans les avancements en mettant une reponse juste
            SharedPreferences.Editor editor = preferencesAvancement.edit();
            majavancementScore = changeChar(majavancementScore, numQuestion - 1, '1');
            editor.putInt(nomDefi + "avancementScore", score);
            editor.putString(nomDefi + "avancementScoreQuestions", majavancementScore);
            editor.apply();
        }
        else
        {
            //on joue le son
            if(sons)
            {
                sonReponseFausse.start();
            }

            switch(numBouton) {
                case 1:
                    reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                    break;
                case 2:
                    reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                    break;

                case 3:
                    reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                    break;

                case 4:
                    reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                    break;
            }

            //affichage de la bonne réponse
            switch (tab_result.indexOf(1) + 1)
            {
                case 1:
                    reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
                case 2:
                    reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
                case 3:
                    reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
                case 4:
                    reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                    break;
            }

            //affichage de la couleur du bouton indice
            couleurBoutonIndice(false);
        }

        t.cancel();
        task.cancel();

        reponse1.setClickable(false);
        reponse2.setClickable(false);
        reponse3.setClickable(false);
        reponse4.setClickable(false);
        layoutQuestion.setClickable(true);

        //lancement du decompte entre les questions
        decompte.setVisibility(View.VISIBLE);

        if(transitionAuto)
        {
            decompteTimer.start();
        }
    }

    private void envoiResultatRetrofit(){
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

        BDD.script service = retrofit.create(BDD.script.class);
        service.scorefindefi("scorefindefiV2", pseudo, mdp, String.valueOf(score), nomDefi, majavancementScore).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("problemeinconnu")) {
                    Toast.makeText(getApplicationContext(), R.string.echecEnvoiScore, Toast.LENGTH_LONG).show();

                    //retour à l'activité intermediaire pour synchroniser
                    Intent intent = new Intent(questionsdefi.this, activityIntermediaireDefis.class);
                    activityIntermediaireDefis.getInstance().finish(); // supprime l'activité intermediaire en meme temps
                    questionsdefi.this.finish();

                    intent.putExtra("nomDefi", nomDefi);
                    startActivityForResult(intent, 0);
                }
                else {
                    //suppression de la shared preference
                    preferencesAvancement.edit().remove(nomDefi + "avancement").apply();
                    preferencesAvancement.edit().remove(nomDefi + "avancementScore").apply();
                    preferencesAvancement.edit().remove(nomDefi + "avancementScoreQuestions").apply();

                    //retour à l'activité principale
                    Intent intent = new Intent(questionsdefi.this, principal.class);
                    //suppression de l'activité
                    principal.getThread().setRunning(false);
                    principal.getInstance().finish(); //supprime l'activité principale d'avant en meme temps
                    activityIntermediaireDefis.getInstance().finish(); // supprime l'activité intermediaire en meme temps

                    //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                    //et on la ferme dans l apage principale
                    //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                    SharedPreferences preferences = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("activityQuestionDefiOuverte", true);
                    editor.apply();

                    startActivity(intent);
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                //retour à l'activité intermediaire pour synchroniser

                Intent intent = new Intent(questionsdefi.this, activityIntermediaireDefis.class);
                activityIntermediaireDefis.getInstance().finish(); // supprime l'activité intermediaire en meme temps
                questionsdefi.this.finish();

                intent.putExtra("nomDefi", nomDefi);
                startActivityForResult(intent, 0);
                dialog.dismiss();
            }
        });

    }

    //si on appuie sur la touche retour
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(questionsdefi.this, activityIntermediaireDefis.class);
        activityIntermediaireDefis.getInstance().finish(); // supprime l'activité intermediaire en meme temps
        t.cancel();
        task.cancel();
        decompteTimer.cancel(); // arrete le decompte
        questionsdefi.this.finish();

        intent.putExtra("nomDefi", nomDefi);
        startActivityForResult(intent, 0);
    }

    //gestion du temps pour les questions
    public void startTimer(){
        t = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        progressionBar.setProgress(tempsQuestionMax - time);
                        if (time > 0)
                        {
                            time = time - 1;
                        }
                        else {
                            t.cancel();
                            task.cancel();
                            finDuTemps();
                        }
                    }
                });
            }
        };
        t.scheduleAtFixedRate(task, 0, 1000);
    }

    private void finDuTemps(){
        couleurBoutonIndice(false);
        afficheReponseCorrecte();
        reponse1.setClickable(false);
        reponse2.setClickable(false);
        reponse3.setClickable(false);
        reponse4.setClickable(false);
        layoutQuestion.setClickable(true);
        decompte.setVisibility(View.VISIBLE);

        if(sons)
        {
            sonReponseFausse.start();
        }

        if(transitionAuto)
        {
            decompteTimer.start();
        }
    }

    //affiche la reponse correcte en verte et les autres en rouge a la fin du temps
    private void afficheReponseCorrecte(){
        for(int i = 0; i < nbQuestions; i++)
        {
            if(tab_result.get(i).equals(1)){
                switch(i + 1) {
                    case 1:
                        reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                        break;
                    case 2:
                        reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                        break;
                    case 3:
                        reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                        break;

                    case 4:
                        reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_juste_shape));
                        break;
                }
            }
            else {
                switch(i + 1) {
                    case 1:
                        reponse1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                        break;
                    case 2:
                        reponse2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                        break;
                    case 3:
                        reponse3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                        break;
                    case 4:
                        reponse4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.buttonquestion_fausse_shape));
                        break;
                }
            }
        }
    }

    private void couleurBoutonIndice(boolean b){
        if(b == true)
        {
            switch (numQuestion - 1){
                case 1:
                    q1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                    break;
                case 2:
                    q2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                    break;
                case 3:
                    q3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                    break;
                case 4:
                    q4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                    break;
                case 5:
                    q5.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_juste_shape));
                    break;
            }
        }
        else {
            switch (numQuestion - 1) {
                case 1:
                    q1.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                    break;
                case 2:
                    q2.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                    break;
                case 3:
                    q3.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                    break;
                case 4:
                    q4.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                    break;
                case 5:
                    q5.setBackground(questionsdefi.this.getResources().getDrawable(R.drawable.button_indice_faux_shape));
                    break;
            }
        }
    }

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
}

