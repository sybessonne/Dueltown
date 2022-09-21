package com.dueltown;

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
import android.widget.Button;
import android.widget.EditText;
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

public class solutionDefiFiniHistorique extends AppCompatActivity {

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
    Dialog dialogChargement;

    ImageView imageVictoire = null;
    ImageView imageDefaite = null;

    String nomDefi;
    String pseudo;

    private final int nbQuestionsTotales = 5;
    private final int nbReponsesParQuestion = 4;

    String enonces[] = new String[nbQuestionsTotales];
    String reponses[][] = new String[nbQuestionsTotales][nbReponsesParQuestion];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solutiondefifini);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        creationProgressDialogChargement();
        dialogChargement.show();

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

        //recupere le nom du défi courant
        nomDefi =  this.getIntent().getExtras().getString("nomDefi", "");
        pseudo = this.getIntent().getExtras().getString("pseudo", "");

        //listener sur les boutons indices
        q1.setOnClickListener(q1Click);
        q2.setOnClickListener(q2Click);
        q3.setOnClickListener(q3Click);
        q4.setOnClickListener(q4Click);
        q5.setOnClickListener(q5Click);

        //appel des elements a la base de donnée
        appelRetrofit();
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
        enonce.setText(enonces[indice - 1]);
        reponseJuste.setText(reponses[indice - 1][0]);
        reponseFausse1.setText(reponses[indice - 1][1]);
        reponseFausse2.setText(reponses[indice - 1][2]);
        reponseFausse3.setText(reponses[indice - 1][3]);

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

        enonce.setText(enonces[indice - 1]);

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
                        q1.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 2:
                        q2.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 3:
                        q3.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 4:
                        q4.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                    case 5:
                        q5.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_faux_shape_v2));
                        break;
                }
            }
            else if (avancement.charAt(i - 1) == '1')
            {
                switch (i){
                    case 1:
                        q1.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 2:
                        q2.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 3:
                        q3.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 4:
                        q4.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                    case 5:
                        q5.setBackground(solutionDefiFiniHistorique.this.getResources().getDrawable(R.drawable.button_indice_juste_shape_v2));
                        break;
                }
            }
        }
    }

    private void appelRetrofit()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //on parse le nom du défi pour recuperer les elements
        String infos[] = nomDefi.split("/");

        BDD.script service = retrofit.create(BDD.script.class);
        service.solutionDefiFini("solutionDefiFini", pseudo, infos[0], infos[1], infos[2]).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if(response.body().get(0).getErreur().equals("aucune"))
                {
                    //affichage de la date bien comme  il faut
                    SimpleDateFormat dateActuelle = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date d2 = new Date();
                    try {
                        d2 = dateActuelle.parse(String.valueOf(response.body().get(0).getDateDefi()));
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

                    int score = Integer.parseInt(response.body().get(0).getScoreJoueur());
                    if(score != -1)
                    {
                        if(score >= 0)
                        {
                            scoreJoueur.setText(getString(R.string.scoreTotalDefi, score));
                        }
                        else
                        {
                            scoreJoueur.setText(getString(R.string.scoreTotalDefiPerdu, score));
                        }
                    }
                    else
                    {
                        //si un problème au niveau de la récuperation on met quand meme un score = 0
                        scoreJoueur.setText(getString(R.string.scoreTotalDefi, 0));
                    }

                    //affiche les icones pour l'egalité si jamais c'est une égalité
                    if(response.body().get(0).getEgalite().equals("egalite"))
                    {
                        imageVictoire.setImageResource(R.drawable.equality);
                        imageDefaite.setImageResource(R.drawable.equality);
                    }

                    //colorie les boutons suivant l'avancement
                    String avancement = response.body().get(0).getAvancement();
                    initBoutonIndices(avancement);

                    villegagne.setText(response.body().get(0).getVillegagne());
                    scoreVilleGagne.setText(response.body().get(0).getNbJvigagne());
                    participantGagne.setText(response.body().get(0).getMoyevigagne() + "/5");

                    villeperd.setText(response.body().get(0).getVilleperd());
                    scoreVillePerd.setText(response.body().get(0).getNbJviperd());
                    participantPerd.setText(response.body().get(0).getMoyeviperd() + "/5");

                    //recuperation des questions
                    for(int i = 0; i < nbQuestionsTotales; i++)
                    {
                        enonces[i] = response.body().get(i + 1).getEnonce();
                        reponses[i][0] = response.body().get(i + 1).getReponse1();
                        reponses[i][1] = response.body().get(i + 1).getReponse2();
                        reponses[i][2] = response.body().get(i + 1).getReponse3();
                        reponses[i][3] = response.body().get(i + 1).getReponse4();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.problemeChargement, Toast.LENGTH_LONG).show();
                    retour();
                }

                dialogChargement.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogChargement.dismiss();
                retour();
            }
        });
    }

    //envoi le signalement de question
    private void signalerRetrofit(int indice, String commentaire)
    {
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.signalerQuestion("signalerQuestion", pseudo, enonces[indice - 1], commentaire).enqueue(new Callback<List<BDD>>() {
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

    //crée le dialog box de chargement
    public void creationProgressDialogChargement(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementStatsDefi);
        builder.setCancelable(false);
        dialogChargement = builder.create();
    }
}