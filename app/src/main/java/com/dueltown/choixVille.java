package com.dueltown;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dueltown.affichagesListes.AdapterListePaysSpinner;
import com.dueltown.affichagesListes.AdapterListeVilleAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class choixVille extends Activity {
    static choixVille ActivityChoixVille;

    SwipeRefreshLayout swipeRefresh = null;
    TextInputLayout villeTIL = null;
    Spinner pays = null;
    TextView erreurPays = null;

    AutoCompleteTextView ville = null;
    EditText sugVille = null;
    Button suggestionEnvoie = null;
    Button inscription = null;

    Dialog dialogRecupVille;
    Dialog dialogInscription;

    String pseudo;
    String email;
    String mdp;

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
        setContentView(R.layout.activity_choix_ville);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityChoixVille = this;

        //creation du progress dialog
        creationProgressDialogRecupVille();
        dialogRecupVille.show();
        creationProgressDialogInscription();

        //recuperation des elements de l'intent
        pseudo = this.getIntent().getExtras().getString("pseudo", "");
        email = this.getIntent().getExtras().getString("email", "");
        mdp = this.getIntent().getExtras().getString("mot_de_passe", "");

        //vérification de la connexion internet
        if (BDD.isConnectedInternet(choixVille.this)) {
            //recuperation des villes
            recupVilles();
        } else {
            Toast.makeText(getApplicationContext(), R.string.inscriptionPasInternet, Toast.LENGTH_LONG).show();
            dialogRecupVille.dismiss();
        }

        swipeRefresh = findViewById(R.id.swipeRefresh);
        villeTIL = findViewById(R.id.villeTextInputLayout);
        ville = findViewById(R.id.ville);
        suggestionEnvoie = findViewById(R.id.suggestionEnvoie);
        pays = findViewById(R.id.listepays);
        erreurPays = findViewById(R.id.erreurPays);
        inscription = findViewById(R.id.inscription);

        //gestion du swipe refresh layout
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listeVille.clear();
                listeToutesVilles.clear();
                listePays.clear();
                listePays.add("");
                recupVilles();
            }
        });

        //couleurs pour le refresh layout
        swipeRefresh.setColorSchemeResources(R.color.couleur1,
                R.color.couleur2,
                R.color.couleur3,
                R.color.couleur4);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(choixVille.this, R.raw.bruit_bouton);

        inscription.setOnClickListener(inscriptionListener);
        suggestionEnvoie.setOnClickListener(suggestionListener);

        //lance l'autocompleteview
        mAdapter = new AdapterListeVilleAutoCompleteTextView(this, R.layout.ligne_liste_villes_autocompletetextview, listeVille);
        ville.setThreshold(1);
        ville.setAdapter(mAdapter);

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

                    mAdapter = new AdapterListeVilleAutoCompleteTextView(choixVille.this, R.layout.ligne_liste_villes_autocompletetextview, listeVille);
                    ville.setThreshold(1);
                    ville.setAdapter(mAdapter);

                    //efface la ville prérentréé
                    ville.setText("");
                    erreurPays.setVisibility(View.INVISIBLE);
                    ville.setEnabled(true);
                    villeTIL.setErrorEnabled(false);
                }
                else
                {
                    //message d'erreur + bloque entré de ville
                    ville.setText("");
                    erreurPays.setVisibility(View.VISIBLE);
                    erreurPays.setText(getString(R.string.choixPays));
                    ville.setEnabled(false);
                    villeTIL.setErrorEnabled(false);
                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        //met les textinputlayout error a faux
        TILErrorEnableFalse();

        //suppression des activités d'avant (inscription, connexion)
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

        //vérification de la fermeture de connexion
        b = p.getBoolean("activityConnexionOuverte", false);
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
    public static choixVille getInstance() {
        return ActivityChoixVille;
    }

    private void TILErrorEnableFalse(){
        villeTIL.setErrorEnabled(false);

        erreurPays.setVisibility(View.INVISIBLE);
    }

    //pour le bouton de suggestion de ville
    private View.OnClickListener suggestionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //on passe le pseudo dans l'intent
            Intent intent = new Intent(choixVille.this, SuggestionVille.class);
            //pour passer le pseudo dans l'intent
            intent.putExtra("pseudo", pseudo);
            startActivityForResult(intent, 0);
        }
    };

    //pour le bouton de suggestion de ville
    private View.OnClickListener inscriptionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String villeChoisi = ville.getText().toString();
            String paysChoisi = pays.getSelectedItem().toString();

            villeTIL.setErrorEnabled(false);
            erreurPays.setVisibility(View.INVISIBLE);

            if(paysChoisi.equals("")){
                erreurPays.setVisibility(View.VISIBLE);
                erreurPays.setText(getString(R.string.choixPays));
            }
            else if (!listePays.contains(paysChoisi))
            {
                erreurPays.setVisibility(View.VISIBLE);
                erreurPays.setText(getString(R.string.paysPasDisponible));
            }
            else if (villeChoisi.equals("")){
                villeTIL.setError(getString(R.string.rentrerVille));
            }
            else if (!listeVille.contains(villeChoisi))
            {
                villeTIL.setError(getString(R.string.villePasDisponible));
            }
            //verifie internet
            else if (!BDD.isConnectedInternet(choixVille.this)){
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            //sinon inscription
            else {
                if(sons)
                {
                    sonClick.start();
                }

                villeTIL.setErrorEnabled(false);
                erreurPays.setVisibility(View.INVISIBLE);

                loadRetrofit(pseudo, email, mdp, villeChoisi, paysChoisi);
            }
        }
    };

    private void loadRetrofit(final String pseudo, String email, String mdp, String ville, String pays) {
        dialogInscription.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.inscription("inscriptionVillePays", pseudo, email, mdp, ville, pays).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if(response.body().get(0).getErreur().equals("aucune"))
                {
                    //ecrit pseudo et mdp dans preference pour eviter de rerentrer cela a la connexion
                    SharedPreferences preferences = getSharedPreferences("personne", 0);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("pseudo", response.body().get(0).getPseudo());
                    editor.putString("mdp", response.body().get(0).getMot_de_passe());
                    editor.putString("ville", response.body().get(0).getVille());
                    editor.putString("pays", response.body().get(0).getPays());
                    editor.putString("email", response.body().get(0).getEmail());
                    editor.apply();

                    //on ne supprime pas cette activité, on enregistre seulement que l'on ne la pas fermé
                    //et on la ferme dans la page inscription
                    //cela empeche d'avoir un blanc entre les changements d'activité et fait moins mal aux yeux
                    SharedPreferences pr = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor ed = pr.edit();
                    ed.putBoolean("activityChoixVilleOuverte", true);
                    ed.apply();

                    Intent intent = new Intent(choixVille.this, principal.class);

                    //lancement du service si celui ci a été arreté
                    Intent serviceIntent = new Intent(choixVille.this, NotificationService.class);
                    choixVille.this.startService(serviceIntent);

                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), getString(R.string.erreurInconnue, getString(R.string.adresseSupportDueltown)), Toast.LENGTH_LONG).show();
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

    private void recupVilles() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String langue = this.getResources().getConfiguration().locale.getDisplayLanguage();

        BDD.script service = retrofit.create(BDD.script.class);
        service.villesPays("villesPays", pseudo, "", langue).enqueue(new Callback<List<BDD>>() {
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

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogRecupVille.dismiss();

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
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

    public void creationProgressDialogInscription(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementInscription);
        builder.setCancelable(false);
        dialogInscription = builder.create();
    }
}
