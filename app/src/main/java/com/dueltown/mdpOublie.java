package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class mdpOublie extends AppCompatActivity {

    InputMethodManager imm; // pour le clavier

    TextInputLayout emailTIL = null;
    TextInputLayout mdpProTIL = null;
    TextInputLayout mdp1TIL = null;
    TextInputLayout mdp2TIL = null;

    TextView adresseSupport = null;
    EditText email = null;
    Button validerEmail = null;
    EditText mdpProvisoire = null;
    EditText mdp1 = null;
    EditText mdp2 = null;
    Button valider = null;
    String emailFinal = null;

    Dialog dialogGenereMDP;
    Dialog dialogModifMDF;

    int longueurMinMdp = 8;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mdpoublie);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recupere les elements du layout
        adresseSupport = findViewById(R.id.adresseSupport);
        emailTIL = findViewById(R.id.emailTextInputLayout);
        mdpProTIL = findViewById(R.id.mdpprovisoireTextInputLayout);
        mdp1TIL = findViewById(R.id.mdp1TextInputLayout);
        mdp2TIL = findViewById(R.id.mdp2TextInputLayout);
        email = findViewById(R.id.email);
        validerEmail = findViewById(R.id.validerEmail);
        mdpProvisoire = findViewById(R.id.mdpprovisoire);
        mdp1 = findViewById(R.id.mdp1);
        mdp2 = findViewById(R.id.mdp2);
        valider = findViewById(R.id.valider);

        //cacher la deuxième partie du layout pour le moment
        mdpProTIL.setVisibility(View.INVISIBLE);
        mdp1TIL.setVisibility(View.INVISIBLE);
        mdp2TIL.setVisibility(View.INVISIBLE);
        mdpProvisoire.setVisibility(View.INVISIBLE);
        mdp1.setVisibility(View.INVISIBLE);
        mdp2.setVisibility(View.INVISIBLE);
        valider.setVisibility(View.INVISIBLE);

        TILErrorEnableFalse(); // met les textinputlayout error a faux

        validerEmail.setOnClickListener(validerEmailListener);
        valider.setOnClickListener(validerListener);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(mdpOublie.this, R.raw.bruit_bouton);

        creationProgressDialogGenereMDP();
        creationProgressDialogModifMDP();

        adresseSupport.setOnClickListener(adresseListener);
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

    // pour ouvrir une application de messagerie  pour mail au support
    private View.OnClickListener adresseListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String email = getString(R.string.adresseSupportDueltown);
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + email));
            intent.putExtra(Intent.EXTRA_EMAIL, email);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Mot de passe oublié sur Dueltown");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    };

    private void TILErrorEnableFalse() {
        emailTIL.setErrorEnabled(false);
        mdpProTIL.setErrorEnabled(false);
        mdp1TIL.setErrorEnabled(false);
        mdp2TIL.setErrorEnabled(false);
    }

    // Uniquement pour le bouton valider l'email
    private View.OnClickListener validerEmailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String em = email.getText().toString();

            String emailPattern = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}";
            String emailPattern2 = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}[\\s]";

            TILErrorEnableFalse();

            if (em.equals("")) {
                emailTIL.setError(getString(R.string.rentrerEmailSoi));
            }
            else if (!(em.matches(emailPattern)) && !(em.matches(emailPattern2))) {
                emailTIL.setError(getString(R.string.pasEmail));
            }
            else if (!BDD.isConnectedInternet(mdpOublie.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                emailTIL.setErrorEnabled(false);
                emailFinal = em; //recuperation de l'email entrée
                envoiNouveauMdp(em);
            }
        }
    };

    private void envoiNouveauMdp(String em) {
        dialogGenereMDP.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.newMdp("nouveauMdp", em).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("emailincorrect")) {
                    emailTIL.setError(getString(R.string.emailinconnu));
                }
                else if (response.body().get(0).getErreur().equals("mdpexistedeja")) {
                    Toast.makeText(getApplicationContext(), R.string.renvoiMDPProvisoire, Toast.LENGTH_LONG).show();
                    //montrer la deuxieme partie du layout
                    mdpProTIL.setVisibility(View.VISIBLE);
                    mdp1TIL.setVisibility(View.VISIBLE);
                    mdp2TIL.setVisibility(View.VISIBLE);
                    mdpProvisoire.setVisibility(View.VISIBLE);
                    mdp1.setVisibility(View.VISIBLE);
                    mdp2.setVisibility(View.VISIBLE);
                    valider.setVisibility(View.VISIBLE);
                }
                //si le mail du mdp provisoire n'a pas été envoyé
                else if (response.body().get(0).getErreur().equals("probleme")) {
                    Toast.makeText(getApplicationContext(), R.string.envoiMDPProvisoireErreur, Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.envoiMDPProvisoire, Toast.LENGTH_LONG).show();

                    //montrer la deuxieme partie du layout
                    mdpProTIL.setVisibility(View.VISIBLE);
                    mdp1TIL.setVisibility(View.VISIBLE);
                    mdp2TIL.setVisibility(View.VISIBLE);
                    mdpProvisoire.setVisibility(View.VISIBLE);
                    mdp1.setVisibility(View.VISIBLE);
                    mdp2.setVisibility(View.VISIBLE);
                    valider.setVisibility(View.VISIBLE);
                }

                //cache le clavier
                imm = (InputMethodManager)mdpOublie.this.getSystemService(Context.INPUT_METHOD_SERVICE);

                dialogGenereMDP.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogGenereMDP.dismiss();
            }
        });
    }

    // Uniquement pour le bouton "inscription"
    private View.OnClickListener validerListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String mp = mdpProvisoire.getText().toString();
            String m1 = mdp1.getText().toString();
            String m2 = mdp2.getText().toString();

            TILErrorEnableFalse();

            boolean toutBon = true;

            if (mp.equals("")) {
                mdpProTIL.setError(getString(R.string.rentrerMDPProvisoire));
                toutBon = false;
            }

            if (m1.equals("")){
                mdp1TIL.setError(getString(R.string.rentrerNouveauMDP));
                toutBon = false;
            }
            //on verifie si le mot de passe est assez long
            else if (m1.length() < longueurMinMdp) {
                mdp1TIL.setError(getString(R.string.MDPTropCourt, longueurMinMdp));
                toutBon = false;
            }
            // On verifie les 2 mots de passe
            if (!(m1.equals(m2))) {
                mdp2TIL.setError(getString(R.string.MDPDifferent));
                toutBon = false;
            }

            if (toutBon && !BDD.isConnectedInternet(mdpOublie.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            if(toutBon){
                if(sons)
                {
                    sonClick.start();
                }

                modifMdp(mp, m1);
            }
        }
    };

    private void modifMdp(String mp, String m1) {
        dialogModifMDF.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.enternewMdp("enternouveauMdp", emailFinal, mp, hash256(m1)).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().toString().equals("mdppincorrect")) {
                    Toast.makeText(getApplicationContext(), R.string.MDPProvisoireIncorrect, Toast.LENGTH_LONG).show();
                    mdpProvisoire.setTextColor(Color.RED);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.changementMDPsucces, Toast.LENGTH_LONG).show();

                    //suppression de l'activité
                    mdpOublie.this.finish();
                }

                dialogModifMDF.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogModifMDF.dismiss();
            }
        });
    }

    private String hash256(String mot) {
        String mdp = mot + getString(R.string.mdpAddSaltyWord);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(mdp.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            //convert the byte to hex format
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }

        return "";
    }

    public void creationProgressDialogGenereMDP(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementGenererMDP);
        builder.setCancelable(false);
        dialogGenereMDP = builder.create();
    }

    public void creationProgressDialogModifMDP(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementModifMDP);
        builder.setCancelable(false);
        dialogModifMDF = builder.create();
    }
}