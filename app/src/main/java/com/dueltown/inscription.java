package com.dueltown;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class inscription extends Activity {

    static inscription ActivityInscription;

    InputMethodManager imm; // pour le clavier

    //initialise les element du layout
    TextInputLayout pseudoTIL = null;
    TextInputLayout emailTIL = null;
    TextInputLayout mdp1TIL = null;
    TextInputLayout mdp2TIL = null;
    ImageButton infoButton = null;

    EditText pseudo = null;
    EditText mdp1 = null;
    EditText mdp2 = null;
    EditText email = null;

    Button connexion = null;
    Button inscription = null;

    Dialog dialogInscription;

    int longueurMinMdp = 8;
    int longueurMaxEmail = 50;
    int longueurMinPseudo = 2;
    int longueurMaxPseudo = 15;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityInscription = this;

        creationProgressDialogInscription();

        // On récupère toutes les vues dont on a besoin
        pseudoTIL = findViewById(R.id.pseudoTextInputLayout);
        emailTIL = findViewById(R.id.emailTextInputLayout);
        mdp1TIL = findViewById(R.id.mdp1TextInputLayout);
        mdp2TIL = findViewById(R.id.mdp2TextInputLayout);
        infoButton = findViewById(R.id.infoEmail);

        connexion = findViewById(R.id.connexion);
        inscription = findViewById(R.id.suivant);
        pseudo = findViewById(R.id.pseudo);
        mdp1 = findViewById(R.id.mdp1);
        mdp2 = findViewById(R.id.mdp2);
        email = findViewById(R.id.email);

        //cache le clavier
        imm = (InputMethodManager)inscription.this.getSystemService(Context.INPUT_METHOD_SERVICE);

        //clique sur l'image info email
        infoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //cache la clavier et affiche la boite de dialogue
                imm = (InputMethodManager)inscription.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                boiteDialogueInfoEmail();
            }
        });

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(inscription.this, R.raw.bruit_bouton);

        // On attribue un listener adapté aux vues qui en ont besoin
        connexion.setOnClickListener(connexionListener);
        inscription.setOnClickListener(inscriptionListener);

        //met les textinputlayout error a faux
        TILErrorEnableFalse();

        //vérification de la fermeture de connexion
        SharedPreferences p = getSharedPreferences("parametres", 0);
        boolean b = p.getBoolean("activityConnexionOuverte", false);
        if(b)
        {
            //on remet a faux
            SharedPreferences.Editor editor = p.edit();
            editor.putBoolean("activityConnexionOuverte", false);
            editor.apply();

            com.dueltown.connexion.getInstance().finish();
        }
    }

    //pour supprimer l'activité a partir de la page principale
    public static inscription getInstance() {
        return ActivityInscription;
    }

    private void TILErrorEnableFalse(){
        pseudoTIL.setErrorEnabled(false);
        emailTIL.setErrorEnabled(false);
        mdp1TIL.setErrorEnabled(false);
        mdp2TIL.setErrorEnabled(false);
    }

    // Uniquement pour le bouton "inscription"
    private OnClickListener inscriptionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //cache le clavier
            InputMethodManager imm = (InputMethodManager)inscription.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);

            //on recupere les donnee du formulaire dans des string
            String m1 = mdp1.getText().toString();
            String m2 = mdp2.getText().toString();
            String e = email.getText().toString();
            String p = pseudo.getText().toString();

            //la regex pour verifier email
            String emailPattern = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}";
            String emailPattern2 = "[a-zA-Z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]+@[a-z0-9._-àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,]{2,}\\.[a-z]{2,4}[\\s]";
            String pseudoPattern = "[a-zA-Z0-9-_àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù]+";

            TILErrorEnableFalse(); // remet tout à juste (enleve les erreurs)

            boolean toutBon = true;

            //on verifie si un champ est vide
            if (p.equals("")) {
                pseudoTIL.setError(getString(R.string.rentrerPseudo));
                toutBon = false;
            }
            //verifie si le pseudo a moins de 2 caractères
            else if (p.length() < longueurMinPseudo) {
                pseudoTIL.setError(getString(R.string.pseudoTropCourt, longueurMinPseudo));
                toutBon = false;
            }
            //verifie si le pseudo contient des caractères spéciaux
            else if(!(p.matches(pseudoPattern))) {
                pseudoTIL.setError(getString(R.string.pseudoCarSpecials));
                toutBon = false;
            }
            //verifie si le pseudo est trop long
            else if (p.length() > longueurMaxPseudo){
                pseudoTIL.setError(getString(R.string.pseudoTropLong, longueurMaxPseudo));
                toutBon = false;
            }

            //si l'email n'est pas vide
            if (!e.equals("") && !(e.matches(emailPattern)) && !(e.matches(emailPattern2))) {
                emailTIL.setError(getString(R.string.pasEmail));
                toutBon = false;
            }
            else if (!e.equals("") && e.length() > longueurMaxEmail){
                emailTIL.setError(getString(R.string.emailTropLong));
                toutBon = false;
            }

            if (m1.equals("")) {
                mdp1TIL.setError(getString(R.string.rentrerMDP));
                toutBon = false;
            }
            //on verifie si le mot de passe est assez long
            else if (m1.length() < longueurMinMdp) {
                mdp1TIL.setError(getString(R.string.MDPTropCourt, longueurMinMdp));
                toutBon = false;
            }

            // On verifie les 2 mots de passe
            if(m2.equals(""))
            {
                mdp2TIL.setError(getString(R.string.confirmerMDPV2));
                toutBon = false;
            }
            else if (!(m1.equals(m2))) {
                mdp2TIL.setError(getString(R.string.MDPDifferent));
                toutBon = false;
            }

            if (toutBon && !BDD.isConnectedInternet(inscription.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                toutBon = false;
            }

            //si tout est bon on se connecte a la base de donnee
            if(toutBon) {
                if(sons)
                {
                    sonClick.start();
                }

                TILErrorEnableFalse();
                retrofitQuestion(p, e, hash256(m1));
            }
        }
    };

    // Uniquement pour le bouton "connexion"
    private OnClickListener connexionListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(sons)
            {
                sonClick.start();
            }

            //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
            //et on la ferme dans la page connexion
            //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
            SharedPreferences preferences = getSharedPreferences("parametres", 0);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("activityInscriptionOuverte", true);
            editor.apply();

            Intent intent = new Intent(inscription.this, connexion.class);
            startActivity(intent);
        }
    };

    public void retrofitQuestion(final String p, final String e, final String mdp)
    {
        dialogInscription.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.inscription("inscription", p, e, mdp, "", "").enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("pseudoExisteDeja")) {
                    pseudoTIL.setError(getString(R.string.pseudoExistant));
                }
                else if (response.body().get(0).getErreur().equals("emailExisteDeja")) {
                    emailTIL.setError(getString(R.string.emailExistant));
                }
                else if (response.body().get(0).getErreur().equals("pseudoInapproprie")) {
                    pseudoTIL.setError(getString(R.string.pseudoInapproprie));
                }
                else {
                    //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                    //et on la ferme dans la page choix ville
                    //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                    SharedPreferences preferences = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("activityInscriptionOuverte", true);
                    editor.apply();

                    //on passe à l'activité suivante pour choisir la ville
                    Intent intent = new Intent(inscription.this, choixVille.class);
                    //pour passer le nom du defi dans l'activite des questions
                    intent.putExtra("pseudo", p);
                    intent.putExtra("email", e);
                    intent.putExtra("mot_de_passe", mdp);
                    startActivityForResult(intent, 0);
                }

                dialogInscription.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogInscription.dismiss();
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

    public void creationProgressDialogInscription(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.inscription);
        builder.setCancelable(false);
        dialogInscription = builder.create();
    }

    //boite de dialogue de confirmation de déconnexion
    public void boiteDialogueInfoEmail() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox, null);
        dialogBuilder.setView(dialogView);

        TextView texte = dialogView.findViewById(R.id.texte);
        Button oui = dialogView.findViewById(R.id.boutonGauche);
        Button compris = dialogView.findViewById(R.id.boutonDroit);

        texte.setText(R.string.infosEmail);
        oui.setVisibility(View.INVISIBLE);
        compris.setText(R.string.compris);

        final AlertDialog alertDialog = dialogBuilder.show();

        compris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sons)
                {
                    sonClick.start();
                }

                alertDialog.dismiss();
            }
        });
    }
}