package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class solutionDefiFini extends AppCompatActivity {

    //recuperation des element du layout
    TextView date = null;
    TextView scoreJoueur = null;
    TextView villegagne = null;
    TextView scoreVilleGagne = null;
    TextView participantGagne = null;
    TextView villeperd = null;
    TextView scoreVillePerd = null;
    TextView participantPerd = null;

    Button q1 = null;
    Button q2 = null;
    Button q3 = null;
    Button q4 = null;
    Button q5 = null;

    Dialog dialog;

    SharedPreferences preferences;

    ImageView imageVictoire = null;
    ImageView imageDefaite = null;

    String nomDefi = "";
    String egalite = "";

    private final int nbQuestionsTotales = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solutiondefifini);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        scoreJoueur = findViewById(R.id.scoreJoueur);
        date = findViewById(R.id.date);
        villegagne = findViewById(R.id.villegagne);
        scoreVilleGagne = findViewById(R.id.scoreVilleGagne);
        participantGagne = findViewById(R.id.participantsGagne);
        villeperd = findViewById(R.id.villeperd);
        scoreVillePerd = findViewById(R.id.scoreVillePerd);
        participantPerd = findViewById(R.id.participantsPerd);
        q1 = findViewById(R.id.q1);
        q2 = findViewById(R.id.q2);
        q3 = findViewById(R.id.q3);
        q4 = findViewById(R.id.q4);
        q5 = findViewById(R.id.q5);

        imageVictoire = findViewById(R.id.imageVictoire);
        imageDefaite =  findViewById(R.id.imageDefaite);

        //recupere si il y a eu égalité
        egalite = this.getIntent().getExtras().getString("egalite", "");

        //affiche les icones pour l'egalité si jamais c'est une égalité
        if(egalite.equals("egalite"))
        {
            imageVictoire.setImageResource(R.drawable.equality);
            imageDefaite.setImageResource(R.drawable.equality);
        }

        //recupere le nom du défi courant
        nomDefi =  this.getIntent().getExtras().getString("nomDefi", "");

        //affiche la date du défi
        String infos[] = nomDefi.split("/");

        //affichage de la date bien comme  il faut
        SimpleDateFormat dateActuelle = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date d2 = new Date();
        try {
            d2 = dateActuelle.parse(String.valueOf(infos[2]));
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

        if(minute < 10){
            date.setText(getString(R.string.dateDefiV3, jour, nomMois[mois - 1], heure, minute));
        }
        else{
            date.setText(getString(R.string.dateDefiV4, jour, nomMois[mois - 1], heure, minute));
        }

        preferences = getSharedPreferences("defisFinis", 0);

        //colorie les boutons suivant l'avancement
        String avancement = preferences.getString(nomDefi + "avancement", "22222");
        initBoutonIndices(avancement);

        //affiche le gagnant et autres...
        int score = Integer.parseInt(preferences.getString(nomDefi + "scoreJoueur","0"));
        if(score != -1)
        {
            scoreJoueur.setText(getString(R.string.scoreJoueur, score));
        }
        else
        {
            //si un problème au niveau de la récuperation on met quand meme un score = 0
            scoreJoueur.setText(getString(R.string.scoreJoueur, 0));
        }

        villegagne.setText(preferences.getString(nomDefi + "villegagne",""));
        scoreVilleGagne.setText(preferences.getString(nomDefi + "nbJvigagne",""));
        participantGagne.setText(preferences.getString(nomDefi + "moyevigagne","") + "/5");

        villeperd.setText(preferences.getString(nomDefi + "villeperd",""));
        scoreVillePerd.setText(preferences.getString(nomDefi + "nbJviperd",""));
        participantPerd.setText(preferences.getString(nomDefi + "moyeviperd","") + "/5");

        //listener sur les boutons indices
        q1.setOnClickListener(q1Click);
        q2.setOnClickListener(q2Click);
        q3.setOnClickListener(q3Click);
        q4.setOnClickListener(q4Click);
        q5.setOnClickListener(q5Click);
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

    private View.OnClickListener q1Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boitedialogue(1);
        }
    };

    private View.OnClickListener q2Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boitedialogue(2);
        }
    };

    private View.OnClickListener q3Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boitedialogue(3);
        }
    };

    private View.OnClickListener q4Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boitedialogue(4);
        }
    };

    private View.OnClickListener q5Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boitedialogue(5);
        }
    };

    ///boite de dialogue qui affiche la reponse juste et les fausse et la quesiton et bouton signaler queestion
    private void boitedialogue(final int indice)
    {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox_solution_question, null);
        dialogBuilder.setView(dialogView);

        ImageView sortie = dialogView.findViewById(R.id.sortie);
        TextView titre = dialogView.findViewById(R.id.titre);
        TextView enonce = dialogView.findViewById(R.id.enonce);
        TextView reponseJuste = dialogView.findViewById(R.id.reponseJuste);
        TextView reponseFausse1 = dialogView.findViewById(R.id.reponseFausse1);
        TextView reponseFausse2 = dialogView.findViewById(R.id.reponseFausse2);
        TextView reponseFausse3 = dialogView.findViewById(R.id.reponseFausse3);
        Button signaler = dialogView.findViewById(R.id.bouton);

        titre.setText(getString(R.string.titreQuestion, indice));
        enonce.setText(preferences.getString(nomDefi + "question" + indice,""));
        reponseJuste.setText(preferences.getString(nomDefi + "reponse1" + indice,""));
        reponseFausse1.setText(preferences.getString(nomDefi + "reponse2" + indice, ""));
        reponseFausse2.setText(preferences.getString(nomDefi + "reponse3" + indice, ""));
        reponseFausse3.setText(preferences.getString(nomDefi + "reponse4" + indice, ""));

        final AlertDialog alertDialog = dialogBuilder.show();

        sortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        signaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                alertDialog.dismiss();
                boiteDialogueSignalement(indice);
            }
        });
    }

    ///boite de dialogue pour signaler une question
    private void boiteDialogueSignalement(final int indice)
    {
        creationProgressDialog();

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox_signalement_question, null);
        dialogBuilder.setView(dialogView);

        ImageView sortie = dialogView.findViewById(R.id.sortie);
        TextView enonce = dialogView.findViewById(R.id.enonce);
        final EditText commentaire = dialogView.findViewById(R.id.commentaire);
        final Button signaler = dialogView.findViewById(R.id.bouton);

        enonce.setText(preferences.getString(nomDefi + "question" + indice,""));

        final AlertDialog alertDialog = dialogBuilder.show();

        sortie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        signaler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                alertDialog.dismiss();
                signalerRetrofit(indice, commentaire.getText().toString());
            }
        });
    }

    private void initBoutonIndices(String avancement){
        for(int i = 1; i <= nbQuestionsTotales; i++)
        {
            if(avancement.charAt(i - 1) == '0')
            {
                switch (i){
                    case 1:
                        q1.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 2:
                        q2.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 3:
                        q3.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 4:
                        q4.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 5:
                        q5.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                }
            }
            else if (avancement.charAt(i - 1) == '1')
            {
                switch (i){
                    case 1:
                        q1.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 2:
                        q2.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 3:
                        q3.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 4:
                        q4.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 5:
                        q5.setBackground(solutionDefiFini.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                }
            }
        }
    }

    //envoi le signalement de question
    private void signalerRetrofit(int indice, String commentaire)
    {
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //recuperation du pseudo
        SharedPreferences preferencesPseudo = getSharedPreferences("personne", 0);
        String pseudo = preferencesPseudo.getString("pseudo", "");

        //recuperation de l'enonce de la question
        String enonce = preferences.getString(nomDefi + "question" + indice,"");

        BDD.script service = retrofit.create(BDD.script.class);
        service.signalerQuestion("signalerQuestion", pseudo, enonce, commentaire).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if(response.body().get(0).getErreur().equals("aucune"))
                {
                    Toast.makeText(getApplicationContext(), R.string.signalementOK, Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.signalementErreur, Toast.LENGTH_LONG).show();
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

    //crée le dialog box de chargement
    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.envoiSignalement);
        builder.setCancelable(false);
        dialog = builder.create();
    }
}
