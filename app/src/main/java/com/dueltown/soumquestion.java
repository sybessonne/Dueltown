package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class soumquestion extends AppCompatActivity {

    TextInputLayout questionTIL = null;
    TextInputLayout rep1TIL = null;
    TextInputLayout rep2TIL = null;
    TextInputLayout rep3TIL = null;
    TextInputLayout rep4TIL = null;

    EditText question = null;
    EditText rep1 = null;
    EditText rep2 = null;
    EditText rep3 = null;
    EditText rep4 = null;
    Button soumettre = null;

    Dialog dialog;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.soumquestion);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        creationProgressDialog();

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        questionTIL = findViewById(R.id.questionTextInputLayout);
        rep1TIL = findViewById(R.id.rep1TextInputLayout);
        rep2TIL = findViewById(R.id.rep2TextInputLayout);
        rep3TIL = findViewById(R.id.rep3TextInputLayout);
        rep4TIL = findViewById(R.id.rep4TextInputLayout);

        question = findViewById(R.id.question);
        rep1 = findViewById(R.id.rep1);
        rep2 = findViewById(R.id.rep2);
        rep3 = findViewById(R.id.rep3);
        rep4 = findViewById(R.id.rep4);
        soumettre = findViewById(R.id.soumettre);

        soumettre.setOnClickListener(soumettreListener);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(soumquestion.this, R.raw.bruit_bouton);

        TILSetErrorEnable();
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

    private void TILSetErrorEnable(){
        questionTIL.setErrorEnabled(false);
        rep1TIL.setErrorEnabled(false);
        rep2TIL.setErrorEnabled(false);
        rep3TIL.setErrorEnabled(false);
        rep4TIL.setErrorEnabled(false);
    }

    // pour le bouton soumettre question
    private View.OnClickListener soumettreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quest = question.getText().toString();
            String r1 = rep1.getText().toString();
            String r2 = rep2.getText().toString();
            String r3 = rep3.getText().toString();
            String r4 = rep4.getText().toString();

            TILSetErrorEnable();

            boolean toutBon = true;

            //verifie les differents champs
            if (quest.equals("")) {
                questionTIL.setError(getString(R.string.rentrerQuestion));
                toutBon = false;
            }
            if (r1.equals("")) {
                rep1TIL.setError(getString(R.string.rentrerReponse));
                toutBon = false;
            }
            if (r2.equals("")) {
                rep2TIL.setError(getString(R.string.rentrerReponse));
                toutBon = false;
            }
            if (r3.equals("")) {
                rep3TIL.setError(getString(R.string.rentrerReponse));
                toutBon = false;
            }
            if (r4.equals("")) {
                rep4TIL.setError(getString(R.string.rentrerReponse));
                toutBon = false;
            }
            if (toutBon && !BDD.isConnectedInternet(soumquestion.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            if(toutBon){
                if(sons)
                {
                    sonClick.start();
                }

                envoiQuestion(quest, r1, r2, r3, r4);
            }
        }
    };

    private void envoiQuestion (String question, String r1, String r2, String r3, String r4){
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //recuperation du pseudo du joueur
        SharedPreferences preferences = getSharedPreferences("personne", 0);
        String pseudo = preferences.getString("pseudo", "");

        BDD.script service = retrofit.create(BDD.script.class);
        service.soumquestion("soumQuestion", pseudo, question, r1, r2,r3,r4).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().toString().equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.envoiQuestionSucces, Toast.LENGTH_LONG).show();
                    //suppression de l'activité
                    soumquestion.this.finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.envoiQuestionEchec, Toast.LENGTH_LONG).show();
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

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementEnvoiQuestion);
        builder.setCancelable(false);
        dialog = builder.create();
    }
}
