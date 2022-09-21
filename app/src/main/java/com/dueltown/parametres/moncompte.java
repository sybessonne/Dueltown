package com.dueltown.parametres;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.dueltown.BDD;
import com.dueltown.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class moncompte extends AppCompatActivity {

    //init les elements du layout
    TextInputLayout mdpActuelTIL = null;
    TextInputLayout mdp1TIL = null;
    TextInputLayout mdp2TIL = null;
    TextInputLayout emailTIL = null;

    Button validermdp = null;
    Button valideremail = null;
    Button changerVille = null;
    EditText mdp = null;
    EditText mdpconf = null;
    EditText mdpancien = null;
    EditText email = null;

    Dialog dialogChangeMDP;
    Dialog dialogChangeEmail;

    SharedPreferences preferences;
    String emailPseudo;
    String motdepasse;
    String pseudo;

    int longueurMinMdp = 8;
    int longueurMaxEmail = 50;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moncompte);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        creationProgressDialogChangeEmail();
        creationProgressDialogChangeMDP();

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recupere les elements du layout
        mdpActuelTIL = findViewById(R.id.mdpActuelTextInputLayout);
        mdp1TIL =  findViewById(R.id.mdpTextInputLayout);
        mdp2TIL =  findViewById(R.id.mdpConfTextInputLayout);
        emailTIL = findViewById(R.id.emailTextInputLayout);
        changerVille = findViewById(R.id.changerVille);

        validermdp = findViewById(R.id.validermdp);
        valideremail = findViewById(R.id.valideremail);
        mdp = findViewById(R.id.mdp);
        mdpconf = findViewById(R.id.mdpconf);
        mdpancien = findViewById(R.id.mdpancien);
        email = findViewById(R.id.email);

        //recuperation du pseudo du joueur
        preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");
        emailPseudo = preferences.getString("email", "");
        motdepasse = preferences.getString("mdp", "");

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(moncompte.this, R.raw.bruit_bouton);

        //entrer le pseudo et email
        email.setText(emailPseudo);

        // On attribue un listener adapté aux vues qui en ont besoin
        validermdp.setOnClickListener(validermdpListener);
        valideremail.setOnClickListener(valideremailListener);
        changerVille.setOnClickListener(changerVilleListener);

        TILErrorEnableFalse();
    }

    private void TILErrorEnableFalse(){
        mdpActuelTIL.setErrorEnabled(false);
        emailTIL.setErrorEnabled(false);
        mdp1TIL.setErrorEnabled(false);
        mdp2TIL.setErrorEnabled(false);
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

    // Uniquement pour le bouton defi
    private View.OnClickListener changerVilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(moncompte.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(moncompte.this, changerVille.class);
                startActivity(intent);
            }
        }
    };

    // pour le bouton valider mdp
    private View.OnClickListener validermdpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String mdp1 = mdp.getText().toString();
            String mdp2 = mdpconf.getText().toString();
            String mdp0 = mdpancien.getText().toString();

            TILErrorEnableFalse();

            boolean toutBon = true;

            // On verifie le mot de passe
            if (mdp0.equals("")) {
                mdpActuelTIL.setError(getString(R.string.rentrerMDPActuel));
                toutBon = false;
            }
            else if (!(hash256(mdp0).equals(motdepasse))) {
                mdpActuelTIL.setError(getString(R.string.MDPActuelIncorrect));
                toutBon = false;
            }

            if (mdp1.equals("")){
                mdp1TIL.setError(getString(R.string.rentrerNouveauMDPV2));
                toutBon = false;
            }
            else if (mdp1.length() < longueurMinMdp) {
                mdp1TIL.setError(getString(R.string.MDPTropCourt, longueurMinMdp));
                toutBon = false;
            }

            if (mdp2.equals("")){
                mdp2TIL.setError(getString(R.string.confirmerMDPV2));
                toutBon = false;
            }
            else if (!(mdp1.equals(mdp2))){
                mdp2TIL.setError(getString(R.string.MDPDifferent));
                toutBon = false;
            }

            if (toutBon && !BDD.isConnectedInternet(moncompte.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            if(toutBon)
            {
                if(sons)
                {
                    sonClick.start();
                }

                //cache le clavier
                InputMethodManager imm = (InputMethodManager)moncompte.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

                changeMdp(mdp1);
                mdpancien.setText("");
                mdp.setText("");
                mdpconf.setText("");
            }
        }
    };

    // pour le bouton valider email
    private View.OnClickListener valideremailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String em = email.getText().toString();

            String emailPattern = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}";
            String emailPattern2 = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}[\\s]";

            emailTIL.setErrorEnabled(false);

            if (em.equals(emailPseudo)) {
                emailTIL.setError(getString(R.string.emailInchange));
            }
            else if(!em.equals("") && !(em.matches(emailPattern)) && !(em.matches(emailPattern2))) {
                emailTIL.setError(getString(R.string.pasEmail));
            }
            else if (!em.equals("") && (em.length() > longueurMaxEmail)){
                emailTIL.setError(getString(R.string.emailTropLong));
            }
            else if (!BDD.isConnectedInternet(moncompte.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                changeEmail(em);
            }
        }
    };

    private void changeMdp(String mdp){
        dialogChangeMDP.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String md = hash256(mdp);

        BDD.script service = retrofit.create(BDD.script.class);
        service.mdp("changeMdp", pseudo, motdepasse, md).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if ((response.body().get(0).getErreur().equals("identifiantsIncorrects"))) {
                    Toast.makeText(getApplicationContext(), R.string.pbChangementMPD, Toast.LENGTH_LONG).show();
                }
                else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("mdp", response.body().get(0).getMot_de_passe());
                    editor.apply();
                    motdepasse = response.body().get(0).getMot_de_passe();
                    Toast.makeText(getApplicationContext(), R.string.changementMDPsucces, Toast.LENGTH_LONG).show();
                }

                dialogChangeMDP.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogChangeMDP.dismiss();
            }
        });
    }

    private void changeEmail(String email){
        dialogChangeEmail.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.email("changeEmail", pseudo, motdepasse, email).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("identifiantsIncorrects")) {
                    Toast.makeText(getApplicationContext(), R.string.pbChangementEmail, Toast.LENGTH_LONG).show();
                }
                else {                    
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("email", response.body().get(0).getEmail());
                    editor.apply();
                    Toast.makeText(getApplicationContext(), R.string.changementEmailSucces, Toast.LENGTH_LONG).show();
                    emailPseudo = response.body().get(0).getEmail();
                }

                dialogChangeEmail.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogChangeEmail.dismiss();
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

    public void creationProgressDialogChangeMDP(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementChangementMDP);
        builder.setCancelable(false);
        dialogChangeMDP = builder.create();
    }

    public void creationProgressDialogChangeEmail(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementChangementEmail);
        builder.setCancelable(false);
        dialogChangeEmail = builder.create();
    }
}