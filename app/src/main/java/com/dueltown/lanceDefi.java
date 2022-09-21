package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dueltown.affichagesListes.AdapterListePaysSpinner;
import com.dueltown.affichagesListes.AdapterListeVilleSpinner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class lanceDefi extends AppCompatActivity {

    //recuperation des element du layout
    Spinner villes = null;
    Spinner pays = null;
    TextView erreurPays = null;
    TextView erreurVille = null;
    Button lancerDefi = null;

    //recuperation de la ville du joueur
    SharedPreferences preferences;
    String villeJoueur;
    String pseudo;
    String mdp;

    Dialog dialogRecupVille;
    Dialog dialogCreationDefi;

    //liste des villes du pays séléectionné
    ArrayList<String> listeVille = new ArrayList<>();
    //liste qui contient toutes les villes disponibles
    //celles ci sont triées par pays
    ArrayList<ArrayList> listeToutesVilles = new ArrayList<>();
    ArrayAdapter<String> mAdapter;

    //liste des pays
    ArrayList listePays = new ArrayList();
    AdapterListePaysSpinner mAdapterPays;
    int nbPays = 0;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defi);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //on recupere les elements du layout
        villes = findViewById(R.id.listeVilles);
        pays = findViewById(R.id.listepays);
        erreurPays = findViewById(R.id.erreurPays);
        erreurVille = findViewById(R.id.erreurVille);
        lancerDefi = findViewById(R.id.lancerDefi);

        //recuperation de la ville du joueur
        preferences = getSharedPreferences("personne", 0);
        villeJoueur = preferences.getString("ville", "");
        pseudo = preferences.getString("pseudo", "");
        mdp = preferences.getString("mdp", "");

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(lanceDefi.this, R.raw.bruit_bouton);

        //cache les erreurs pour le moment
        erreurPays.setVisibility(View.INVISIBLE);
        erreurVille.setVisibility(View.INVISIBLE);

        //lance le spinner
        mAdapter = new AdapterListeVilleSpinner(this, R.layout.ligne_liste_villes_spinner, listeVille);
        villes.setAdapter(mAdapter);
        villes.setPopupBackgroundResource(R.color.background);

        //lance le spinner
        mAdapterPays = new AdapterListePaysSpinner(this, R.layout.ligne_liste_pays_spinner_choix_ville, listePays);
        pays.setAdapter(mAdapterPays);
        pays.setPopupBackgroundResource(R.color.background);

        //creation du progress dialog
        creationProgressDialogRecupVilles();
        dialogRecupVille.show();
        recupVilles();
        creationProgressDialogCreationDefi();

        // On attribue un listener adapté aux vues qui en ont besoin
        lancerDefi.setOnClickListener(lancerDefiListener);

        //évenement sur le choix d'un pays dans la liste des pays
        pays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(nbPays != 0)
                {
                    listeVille.clear();

                    //si c'est pas le pays vide on recopie la bonne liste
                    listeVille.addAll(listeToutesVilles.get(position));

                    //efface la ville prérentréé
                    erreurPays.setVisibility(View.INVISIBLE);
                    erreurVille.setVisibility(View.INVISIBLE);

                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    //pour la flèche de retour
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

    public void creationProgressDialogRecupVilles(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementDesVilles);
        builder.setCancelable(false);
        dialogRecupVille = builder.create();
    }

    public void creationProgressDialogCreationDefi(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementCreationDefi);
        builder.setCancelable(false);
        dialogCreationDefi = builder.create();
    }

    // Uniquement pour le bouton lancer le defi
    private View.OnClickListener lancerDefiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            erreurPays.setVisibility(View.INVISIBLE);
            erreurVille.setVisibility(View.INVISIBLE);

            String p = pays.getSelectedItem().toString();
            String vi = villes.getSelectedItem().toString();

            if(sons)
            {
                sonClick.start();
            }

            if(p.equals("")){
                erreurPays.setVisibility(View.VISIBLE);
                erreurPays.setText(getString(R.string.choixPays));
            }
            else if (!listePays.contains(p))
            {
                erreurPays.setVisibility(View.VISIBLE);
                erreurPays.setText(getString(R.string.paysPasDisponible));
            }
            else if (vi.equals("")){
                erreurVille.setVisibility(View.VISIBLE);
                erreurVille.setText(getString(R.string.rentrerVille));
            }
            else if (!listeVille.contains(vi))
            {
                erreurVille.setVisibility(View.VISIBLE);
                erreurVille.setText(getString(R.string.villePasDisponible));
            }
            else if(nbPays == 0)
            {
                erreurVille.setVisibility(View.VISIBLE);
                erreurVille.setText(getString(R.string.aucunDefisDisponible));
            }
            else if (!BDD.isConnectedInternet(lanceDefi.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                erreurVille.setVisibility(View.INVISIBLE);
                erreurPays.setVisibility(View.INVISIBLE);

                lancerDefibdd(villeJoueur, vi);
            }
        }
    };

    //fonction qui récupère les villes qui ont au moins un joueur.
    //Cela pour diminuer la triche et améliorer le jeu
    private void recupVilles() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.villesLanceDefi("villesLanceDefi", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.isSuccessful()){
                    //on récupere les villes et pays
                    nbPays = Integer.parseInt(response.body().get(0).getNbPays());

                    //affiche un message si pas de defi disponibles
                    if(nbPays == 0)
                    {
                        listeVille.add(getString(R.string.Aucunevilledisponible));
                        listePays.add(getString(R.string.Aucunpaysdisponible));
                        villes.setEnabled(false);
                        pays.setEnabled(false);
                    }
                    else
                    {
                        for(int i = 1; i <= nbPays; i++)
                        {
                            listePays.add(response.body().get(i).getPays());

                            int nbVilles = Integer.parseInt(response.body().get(i).getNbVilles());
                            listeToutesVilles.add(new ArrayList());

                            for(int j = 0; j < nbVilles; j++)
                            {
                                listeToutesVilles.get(i - 1).add(response.body().get(i).getListeVille().get(j));
                            }
                        }
                    }
                }

                mAdapter.notifyDataSetChanged();
                mAdapterPays.notifyDataSetChanged();

                dialogRecupVille.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogRecupVille.dismiss();
            }
        });
    }

    private void lancerDefibdd(final String ville1, final String ville2) {
        dialogCreationDefi.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.lancedefi("lancedefi", pseudo, ville1, ville2).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("existeDeja")) {
                    erreurVille.setVisibility(View.VISIBLE);
                    erreurVille.setText(getString(R.string.defiExistant));
                }
                else if(response.body().get(0).getErreur().equals("villeInconnue"))
                {
                    Toast.makeText(getApplicationContext(), R.string.villeInconnue, Toast.LENGTH_LONG).show();
                }
                else if(response.body().get(0).getErreur().equals("probleme"))
                {
                    Toast.makeText(getApplicationContext(), R.string.probleme, Toast.LENGTH_LONG).show();
                }
                //on recupere les questions et reponse
                else {
                    //on met a  jour la date de derniere mise a jour des notification pour pas recevoir une notif pour
                    // le defi qu'on vient de créer
                    SharedPreferences prefParametres = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor e = prefParametres.edit();
                    e.putString("derniereMajDefis", response.body().get(0).getDerniereMajDefis());
                    e.apply();

                    SharedPreferences preferencesDefisPasFinis = getSharedPreferences("defisPasFinis", 0);
                    SharedPreferences.Editor editor = preferencesDefisPasFinis.edit();
                    String nomDefi = response.body().get(0).getNomDefi();
                    for (int i = 1; i <= 5; i++)
                    {
                        editor.putString(nomDefi + "question" + i, response.body().get(i).getEnonce());
                        editor.putString(nomDefi + "reponse1" + i , response.body().get(i).getReponse1());
                        editor.putString(nomDefi + "reponse2" + i , response.body().get(i).getReponse2());
                        editor.putString(nomDefi + "reponse3" + i , response.body().get(i).getReponse3());
                        editor.putString(nomDefi + "reponse4" + i , response.body().get(i).getReponse4());
                    }
                    editor.apply();

                    //lancement du layout du jeu des questions
                    Intent intent = new Intent(lanceDefi.this, activityIntermediaireDefis.class);
                    //pour passer le nom du defi dans l'activite des questions
                    intent.putExtra("nomDefi", nomDefi);
                    //suppression de l'activité
                    lanceDefi.this.finish();
                    startActivityForResult(intent,0);
                }
                dialogCreationDefi.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogCreationDefi.dismiss();
            }
        });
    }
}
