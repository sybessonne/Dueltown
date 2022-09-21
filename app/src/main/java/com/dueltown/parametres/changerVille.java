package com.dueltown.parametres;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.dueltown.BDD;
import com.dueltown.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class changerVille extends AppCompatActivity {

    Button validerville = null;
    TextView nbChangementTexte = null;
    TextView villeActuelle = null;
    Spinner villes = null;
    Spinner pays = null;
    TextView erreurPays = null;
    TextView erreurVille = null;

    Dialog dialogChangeVille;
    Dialog dialogRecupVille;

    SharedPreferences preferences;
    String villePseudo;
    String motdepasse;
    String pseudo;

    int NbChangementVillePossible = 3;

    //liste des villes du pays séléectionné
    ArrayList<String> listeVille = new ArrayList<>();
    //liste qui contient toutes les villes disponibles
    //celles ci sont triées par pays
    ArrayList<ArrayList> listeToutesVilles = new ArrayList<>();
    ArrayAdapter<String> mAdapter;

    //liste des pays
    ArrayList listePays = new ArrayList();
    AdapterListePaysSpinner mAdapterPays;

    boolean sons;
    MediaPlayer sonClick;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changer_ville);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        creationProgressDialogRecupVille();
        dialogRecupVille.show();
        creationProgressDialogChangeVille();

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recupere les elements du layout
        validerville = findViewById(R.id.validerville);
        nbChangementTexte = findViewById(R.id.nbChangementTexte);
        villeActuelle = findViewById(R.id.villeActuelle);
        villes = findViewById(R.id.listeVilles);
        pays = findViewById(R.id.listepays);
        erreurPays = findViewById(R.id.erreurPays);
        erreurVille = findViewById(R.id.erreurVille);

        //recuperation du pseudo du joueur
        preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");
        villePseudo = preferences.getString("ville", "");
        motdepasse = preferences.getString("mdp", "");

        villeActuelle.setText(getString(R.string.villeActuelle, villePseudo));

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(changerVille.this, R.raw.bruit_bouton);

        //affiche le nombre de changement de ville possible
        nbChangementTexte.setText(getString(R.string.nbChangementsVillePossible, NbChangementVillePossible));

        //liste des villes et des pays
        recupVilles();

        //lance le spinner
        mAdapter = new AdapterListeVilleSpinner(this, R.layout.ligne_liste_villes_spinner, listeVille);
        villes.setAdapter(mAdapter);
        villes.setPopupBackgroundResource(R.color.background);

        //lance le spinner
        listePays.add("");
        mAdapterPays = new AdapterListePaysSpinner(this, R.layout.ligne_liste_pays_spinner_choix_ville, listePays);
        pays.setAdapter(mAdapterPays);
        pays.setPopupBackgroundResource(R.color.background);

        //évenement sur le choix d'un pays dans la liste des pays
        pays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                listeVille.clear();

                //si le pays selectionné est different de vide
                if(position != 0)
                {
                    //si c'est pas le pays vide on recopie la bonne liste
                    listeVille.addAll(listeToutesVilles.get(position - 1));

                    //efface la ville prérentréé
                    erreurPays.setVisibility(View.INVISIBLE);
                    erreurVille.setVisibility(View.INVISIBLE);
                }
                else
                {
                    //message d'erreur + bloque entré de ville
                    erreurPays.setVisibility(View.VISIBLE);
                    erreurPays.setText(getString(R.string.choixPays));
                    erreurVille.setVisibility(View.INVISIBLE);
                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // On attribue un listener adapté aux vues qui en ont besoin
        validerville.setOnClickListener(validervilleListener);
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

    // pour le bouton valider ville
    private View.OnClickListener validervilleListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String p = pays.getSelectedItem().toString();
            String vi = "";

            //empeche de recuperer le champ ville qui est vide si le pays n'est pas choisi
            if(!p.equals(""))
            {
                vi = villes.getSelectedItem().toString();
            }

            erreurVille.setVisibility(View.INVISIBLE);
            erreurPays.setVisibility(View.INVISIBLE);

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
                erreurVille.setText(getString(R.string.rentrerVille));
            }
            else if (!listeVille.contains(vi))
            {
                erreurVille.setText(getString(R.string.villePasDisponible));
            }
            else if (!BDD.isConnectedInternet(changerVille.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(sons)
                {
                    sonClick.start();
                }

                erreurVille.setVisibility(View.INVISIBLE);
                erreurPays.setVisibility(View.INVISIBLE);

                changeVille(vi, p);
            }
        }
    };

    private void changeVille(final String ville, String pays){
        dialogChangeVille.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final BDD.script service = retrofit.create(BDD.script.class);
        service.changeville("changeVille", pseudo, motdepasse, ville, pays).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if(response.body().get(0).getErreur().equals("identifiantsIncorrects")) {
                    Toast.makeText(getApplicationContext(), R.string.pbChangementVille, Toast.LENGTH_LONG).show();
                }
                else if (response.body().get(0).getErreur().toString().equals("nonpossible")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.nbChangementVilleDepasse, NbChangementVillePossible), Toast.LENGTH_LONG).show();
                }
                else if(response.body().get(0).getErreur().toString().equals("tropTot")){
                    Toast.makeText(getApplicationContext(), R.string.attenteChangementVille, Toast.LENGTH_LONG).show();
                }
                else {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ville", response.body().get(0).getVille());
                    editor.putString("pays", response.body().get(0).getPays());
                    editor.apply();

                    Toast.makeText(getApplicationContext(), R.string.changementVilleSucces, Toast.LENGTH_LONG).show();
                    //recuperation du nombre de changement de ville
                    int nb = Integer.parseInt(response.body().get(0).getNbChangementVille().toString());
                    if (new Integer(nb).equals(NbChangementVillePossible)) {
                        Toast.makeText(getApplicationContext(), R.string.nbChangementVilleEpuise, Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), getString(R.string.nbChangementsVillePossible, NbChangementVillePossible - nb), Toast.LENGTH_LONG).show();
                    }

                    changerVille.this.finish();
                }
                dialogChangeVille.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogChangeVille.dismiss();
            }
        });
    }

    private void recupVilles() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String langue = this.getResources().getConfiguration().locale.getDisplayLanguage();

        BDD.script service = retrofit.create(BDD.script.class);
        service.villesPays("villesPays", pseudo, villePseudo, langue).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.isSuccessful()){
                    //on récupere les villes et pays
                    int nbPays = Integer.parseInt(response.body().get(0).getNbPays());
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

    public void creationProgressDialogChangeVille(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementChangementVille);
        builder.setCancelable(false);
        dialogChangeVille = builder.create();
    }

    public void creationProgressDialogRecupVille(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargement);
        builder.setCancelable(false);
        dialogRecupVille = builder.create();
    }
}