package com.dueltown;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class connexion extends Activity {

    static connexion ActivityConnexion;

    //initialise les elements du layout
    TextInputLayout pseudoTIL = null;
    TextInputLayout mdpTIL = null;
    EditText pseudo = null;
    EditText mdp = null;
    Button connexion = null;
    Button inscription = null;
    TextView mdpOublie = null;

    int compteurFaux = 0;

    Dialog dialog;intent

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityConnexion = this;

        // On récupère toutes les vues dont on a besoin
        pseudoTIL = findViewById(R.id.pseudoTextInputLayout);
        mdpTIL = findViewById(R.id.mdpTextInputLayout);
        connexion = findViewById(R.id.connexion);
        inscription = findViewById(R.id.inscription);
        pseudo = findViewById(R.id.pseudo);
        mdp = findViewById(R.id.mdp);
        mdpOublie = findViewById(R.id.mdpoublie);

        // On attribue un listener adapté aux vues qui en ont besoin
        connexion.setOnClickListener(envoyerListener);
        inscription.setOnClickListener(inscriptionListener);
        mdpOublie.setOnClickListener(mdpOublieListener);

        //met les textInputlayout error a faux
        pseudoTIL.setErrorEnabled(false);
        mdpTIL.setErrorEnabled(false);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(connexion.this, R.raw.bruit_bouton);

        //creation du progress dialog
        creationProgressDialog();

        //vérification de la fermeture de connexion
        SharedPreferences p = getSharedPreferences("parametres", 0);
        boolean b = p.getBoolean("activityInscriptionOuverte", false);
        if(b)
        {
            //on remet a faux
            SharedPreferences.Editor editor = p.edit();
            editor.putBoolean("activityInscriptionOuverte", false);
            editor.apply();

            com.dueltown.inscription.getInstance().finish();
        }
    }

    //pour supprimer l'activité a partir de la page principale
    public static connexion getInstance() {
        return ActivityConnexion;
    }

    // Uniquement pour le bouton "connexion"
    private OnClickListener envoyerListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //on recupere les donnees du formulaire
            String p = pseudo.getText().toString();
            String m = mdp.getText().toString();

            pseudoTIL.setErrorEnabled(false);
            mdpTIL.setErrorEnabled(false);

            boolean toutBon = true;

            // On verifie le pseudo
            if (p.equals("")) {
                pseudoTIL.setError(getString(R.string.rentrerPseudo));
                toutBon = false;
            }

            // On verifie le mdp
            if (m.equals("")) {
                mdpTIL.setError(getString(R.string.rentrerMDP));
                toutBon = false;
            }

            if (toutBon && !BDD.isConnectedInternet(connexion.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            if (toutBon)
            {
                pseudoTIL.setErrorEnabled(false);
                mdpTIL.setErrorEnabled(false);

                //verifie le compteur d'essaie
                if(compteurFaux >= 3)
                {
                    Toast.makeText(getApplicationContext(), R.string.nbEssaiAtteint, Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(sons)
                    {
                        sonClick.start();
                    }

                    loadRetrofitConnexion("connexion", p, m);
                }
            }
        }
    };

    // Uniquement pour le bouton "inscription"
    private OnClickListener inscriptionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
            //et on la ferme dans la page inscription
            //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
            SharedPreferences preferences = getSharedPreferences("parametres", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("activityConnexionOuverte", true);
            editor.apply();

            Intent intent = new Intent(connexion.this, inscription.class);
            startActivity(intent);
        }
    };

    // Uniquement pour le texview mdp oublie
    private OnClickListener mdpOublieListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            Intent intent = new Intent(connexion.this, mdpOublie.class);
            startActivity(intent);
        }
    };

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementConnexion);
        builder.setCancelable(false);
        dialog = builder.create();
    }


    private void loadRetrofitConnexion(String param, String pseudo, String mdp) {
        dialog.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String md = hash256(mdp);

        BDD.script service = retrofit.create(BDD.script.class);
        service.connexion(param, pseudo, md).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if ((response.body().toString()).equals("[]")) {
                    pseudoTIL.setError(getString(R.string.PseudoMDPincorrect));
                    mdpTIL.setError(getString(R.string.PseudoMDPincorrect));
                    compteurFaux++;
                }
                else {
                    String v = response.body().get(0).getVille();
                    String p = response.body().get(0).getPays();
                    String ps =  response.body().get(0).getPseudo();
                    String e =  response.body().get(0).getEmail();
                    String m = response.body().get(0).getMot_de_passe();

                    if(v.equals("") || p.equals(""))
                    {
                        //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                        //et on la ferme dans la page choix ville
                        //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                        SharedPreferences preferences = getSharedPreferences("parametres", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("activityConnexionOuverte", true);
                        editor.apply();

                        //on passe à l'activité suivante pour choisir la ville
                        Intent intent = new Intent(connexion.this, choixVille.class);
                        //pour passer le nom du defi dans l'activite des questions
                        intent.putExtra("pseudo", ps);
                        intent.putExtra("email", e);
                        intent.putExtra("mot_de_passe", m);
                        startActivityForResult(intent, 0);
                    }
                    else
                    {
                        //ecrit pseudo et mdp dans preference pour eviter de rerentrer cela a la connexion
                        SharedPreferences preferences = getSharedPreferences("personne", 0);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("pseudo", ps);
                        editor.putString("mdp", m);
                        editor.putString("ville", v);
                        editor.putString("pays", p);
                        editor.putString("email", e);
                        editor.apply();

                        //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                        //et on la ferme dans la page inscription
                        //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                        SharedPreferences pr = getSharedPreferences("parametres", 0);
                        SharedPreferences.Editor ed = pr.edit();
                        ed.putBoolean("activityConnexionOuverte", true);
                        ed.apply();

                        Intent intent = new Intent(connexion.this, principal.class);

                        //lancement du service si celui ci a été arreté
                        Intent serviceIntent = new Intent(connexion.this, NotificationService.class);
                        connexion.this.startService(serviceIntent);

                        startActivity(intent);
                    }
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
}

