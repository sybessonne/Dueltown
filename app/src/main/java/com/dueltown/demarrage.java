package com.dueltown;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class demarrage extends Activity {

    SharedPreferences preferences;
    String pseudo;
    String mdp;

    Dialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        creationProgressDialog();
        dialog.show();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");
        mdp = preferences.getString("mdp", "");

        if(pseudo.equals(""))
        {
            Intent intent = new Intent(demarrage.this, connexion.class);
            //supprimer l'activite demarrage
            demarrage.this.finish();
            startActivity(intent);
            dialog.dismiss();
        }
        else{
            //verifie la connexion internet
            if(!(BDD.isConnectedInternet(demarrage.this))) {

                //on en profite pour liberer de la place dans les shared preferences comme ca c'est fait
                SharedPreferences defisPasFinis = getSharedPreferences("defisPasFinis", 0);
                SharedPreferences defisFinis = getSharedPreferences("defisFinis", 0);
                defisPasFinis.edit().clear().apply();
                defisFinis.edit().clear().apply();

                BoiteDialogue();
            }
            else {
                //verifie si le pseudo et mot de passe sont deja entré dans les shared preferences si oui alle directement dans activity_principal
                VerifPreferences();
            }
        }
    }

    private void loadRetrofitConnexion(String param, String pseudo, String mdp) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.connexion(param, pseudo, mdp).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if ((response.body().toString()).equals("[]")) {
                    Intent intent = new Intent(demarrage.this, connexion.class);
                    //supprimer l'activite demarrage
                    demarrage.this.finish();
                    startActivity(intent);
                }
                else {
                    String v = response.body().get(0).getVille();
                    String p = response.body().get(0).getPays();
                    String ps =  response.body().get(0).getPseudo();
                    String e =  response.body().get(0).getEmail();
                    String m = response.body().get(0).getMot_de_passe();

                	//si le joueur n'a pas de pays ou pas de ville
                	if(v.equals("") || p.equals(""))
                	{
                        //on passe à l'activité suivante pour choisir la ville
                        Intent intent = new Intent(demarrage.this, choixVille.class);
                        //pour passer le nom du defi dans l'activite des questions
                        intent.putExtra("pseudo", ps);
                        intent.putExtra("email", e);
                        intent.putExtra("mot_de_passe", m);

                        demarrage.this.finish();
                        startActivityForResult(intent, 0);
                	}
                	else
                	{
                        //ecrit pseudo et mdp dans preference pour eviter de rerentrer cela a la connexion
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("ville", v);
                        editor.putString("pays", p);
                        editor.putString("email", e);
                        editor.apply();

	                    //verification si c'est la derniere mise a jour
	                    //recupere la version actuel de l'application
	                    int versionApp;
	                    PackageInfo pInfo = null;

	                    try{
	                        pInfo = getPackageManager().getPackageInfo("com.dueltown", PackageManager.GET_META_DATA);
	                    } catch (PackageManager.NameNotFoundException ee) {
	                        pInfo = null;
	                    }
	                    if(pInfo != null) {
	                        versionApp = pInfo.versionCode;
	                    }
	                    else {
	                        versionApp = -1;
	                    }

	                    //comparaison avec la derniere version disponible
	                    int derniereVersionApp = response.body().get(0).getVersionApp();

	                    if(versionApp != -1 && versionApp < derniereVersionApp)
	                    {
	                        boiteDialogueMAJ();
	                    }
	                    else
	                    {
	                        //lancement du service si celui ci a été arreté
	                        Intent serviceIntent = new Intent(demarrage.this, NotificationService.class);
	                        demarrage.this.startService(serviceIntent);

	                        //lance l'activité principale
	                        Intent intent = new Intent(demarrage.this, principal.class);

	                        //supprimer l'activite demarrage
	                        startActivity(intent);
	                        demarrage.this.finish();
	                    }
                	}                    
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(demarrage.this, connexion.class);
                //supprimer l'activite demarrage
                demarrage.this.finish();
                startActivity(intent);

                dialog.dismiss();
            }
        });
    }

    //verifie les donnee des shared preferences
    private void VerifPreferences () {
        if(!dialog.isShowing()){
            dialog.show();
        }

        //pour etre sur que ce soit les bonnes données si celles ci existent
        if (pseudo != "") {
            loadRetrofitConnexion("connexion", pseudo, mdp);
        }
        else {
            Intent intent = new Intent(demarrage.this, connexion.class);
            //supprimer l'activite demarrage
            demarrage.this.finish();
            startActivity(intent);
            dialog.dismiss();
        }
    }

    //boite de dialogue si internet n'est pas activé
    public void BoiteDialogue () {
        dialog.dismiss();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox, null);
        dialogBuilder.setView(dialogView);

        TextView texte = dialogView.findViewById(R.id.texte);
        Button recommencer = dialogView.findViewById(R.id.boutonGauche);
        Button quitter = dialogView.findViewById(R.id.boutonDroit);

        texte.setText(R.string.demarrageNoInternet);
        quitter.setText(R.string.quitter);
        recommencer.setText(R.string.reessayer);

        final AlertDialog alertDialog = dialogBuilder.show();

        quitter.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               alertDialog.dismiss();
               demarrage.this.finish();
           }
        });

        recommencer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                if(!(BDD.isConnectedInternet(demarrage.this))) {
                    BoiteDialogue();
                }
                else {
                    //verifie si le pseudo et mot de passe sont deja entré dans les shared preferences si oui alle directement dans activity_principal
                    VerifPreferences();
                }
            }
        });
    }

    //boite de dialogue si ce n'est pas la derniere mise a jour
    public void boiteDialogueMAJ () {
        dialog.dismiss();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox_maj, null);
        dialogBuilder.setView(dialogView);

        TextView texte = dialogView.findViewById(R.id.texte);
        Button non = dialogView.findViewById(R.id.boutonGauche);
        Button oui = dialogView.findViewById(R.id.boutonDroit);

        texte.setText(R.string.majAppli);

        final AlertDialog alertDialog = dialogBuilder.show();

        non.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                //lancement du service si celui ci a été arreté
                Intent serviceIntent = new Intent(demarrage.this, NotificationService.class);
                demarrage.this.startService(serviceIntent);

                //lance l'activité principale
                Intent intent = new Intent(demarrage.this, principal.class);

                //supprimer l'activite demarrage
                startActivity(intent);
                demarrage.this.finish();
            }
        });

        oui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                demarrage.this.finish();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=com.dueltown"));
                startActivity(intent);
            }
        });
    }

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargement);
        builder.setCancelable(false);
        dialog = builder.create();
    }
}

