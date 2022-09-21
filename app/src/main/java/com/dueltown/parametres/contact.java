package com.dueltown.parametres;

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

import com.dueltown.BDD;
import com.dueltown.R;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class contact extends AppCompatActivity {

    TextInputLayout objetTIL = null;
    TextInputLayout txtTIL = null;
    EditText objet = null;
    EditText texte = null;
    Button envoyer = null;

    Dialog dialog;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        objetTIL = findViewById(R.id.objetTextInputLayout);
        txtTIL = findViewById(R.id.txtTextInputLayout);
        objet = findViewById(R.id.objet);
        texte = findViewById(R.id.texte);
        envoyer = findViewById(R.id.envoyer);

        envoyer.setOnClickListener(envoyerListener);

        //met les textInputlayout error a faux
        objetTIL.setErrorEnabled(false);
        txtTIL.setErrorEnabled(false);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(contact.this, R.raw.bruit_bouton);

        creationProgressDialog();
    }

    //pour la fleche de retour
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

    // pour le bouton envoyer
    private View.OnClickListener envoyerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String obj = objet.getText().toString();
            String txt = texte.getText().toString();

            objetTIL.setErrorEnabled(false);
            txtTIL.setErrorEnabled(false);

            boolean toutBon = true;

            if(obj.equals(""))
            {
                objetTIL.setError(getString(R.string.rentrerObjet));
                toutBon = false;
            }

            if (txt.equals(""))
            {
                txtTIL.setError(getString(R.string.rentrerTexte));
                toutBon = false;
            }

            if(toutBon && !BDD.isConnectedInternet(contact.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            if(toutBon)
            {
                if(sons)
                {
                    sonClick.start();
                }

                envoiContact();
            }
        }
    };

    private void envoiContact(){
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //recuperation du pseudo du joueur
        SharedPreferences preferences = getSharedPreferences("personne", 0);
        String pseudo = preferences.getString("pseudo", "");
        String email = preferences.getString("email", "");

        String obj = objet.getText().toString();
        String txt = texte.getText().toString();

        BDD.script service = retrofit.create(BDD.script.class);
        service.envoicontact("contact", pseudo, email, obj, txt).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().toString().equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.contactEnvoiSucces, Toast.LENGTH_LONG).show();
                    //retour à l'activité principale
                    contact.this.finish();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), R.string.contactEnvoiEchec, Toast.LENGTH_LONG).show();
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
        message.setText(R.string.chargementEnvoiContact);
        builder.setCancelable(false);
        dialog = builder.create();
    }
}
