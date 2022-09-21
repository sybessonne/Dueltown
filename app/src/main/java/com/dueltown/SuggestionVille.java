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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dueltown.affichagesListes.AdapterListePaysSpinner;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SuggestionVille extends AppCompatActivity {

    TextInputLayout sugVilleTIL = null;
    EditText sugVille = null;
    Button suggestionEnvoie = null;
    Spinner pays = null;
    TextView erreurPays = null;

    int longueurMaxVille = 40;

    Dialog dialog;
    Dialog dialogPays;

    String pseudo;

    boolean sons;
    MediaPlayer sonClick;

    //liste des pays
    ArrayList listePays = new ArrayList();
    AdapterListePaysSpinner mAdapterPays;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.suggestion_ville);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sugVilleTIL = findViewById(R.id.sugVilleTextInputLayout);
        sugVille = findViewById(R.id.sugVille);
        suggestionEnvoie = findViewById(R.id.suggestionEnvoie);
        pays = findViewById(R.id.listepays);
        erreurPays = findViewById(R.id.erreurPays);

        suggestionEnvoie.setOnClickListener(suggestionEnvoieListener);

        sugVilleTIL.setErrorEnabled(false);
        erreurPays.setVisibility(View.INVISIBLE);

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(SuggestionVille.this, R.raw.bruit_bouton);

        pseudo = this.getIntent().getExtras().getString("pseudo", "");

        creationProgressDialog();
        creationProgressDialogPays();

        //lance le spinner
        listePays.add("");
        mAdapterPays = new AdapterListePaysSpinner(this, R.layout.ligne_liste_pays_spinner_choix_ville, listePays);
        pays.setAdapter(mAdapterPays);
        pays.setPopupBackgroundResource(R.color.background);

        recupPaysRetrofit();

        //évenement sur le choix d'un pays dans la liste des pays
        pays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //si le pays selectionné est different de vide
                if(position != 0)
                {
                    //efface la ville prérentréé
                    sugVille.setText("");
                    erreurPays.setVisibility(View.INVISIBLE);
                    sugVille.setEnabled(true);
                    sugVilleTIL.setErrorEnabled(false);
                }
                else
                {
                    //message d'erreur + bloque entré de ville
                    sugVille.setText("");
                    erreurPays.setVisibility(View.VISIBLE);
                    erreurPays.setText(getString(R.string.choixPays));
                    sugVille.setEnabled(false);
                    sugVilleTIL.setErrorEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
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

    //pour le bouton de suggestion de ville
    private View.OnClickListener suggestionEnvoieListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String suggestion = sugVille.getText().toString();
            String villePattern = "[a-zA-Z0-9-_.àéèâÄäÂêëÊËîïÎÏôöÔÖûüÛÜçù,'/\\s]+";
            String paysChoisi = pays.getSelectedItem().toString();

            sugVilleTIL.setErrorEnabled(false);
            erreurPays.setVisibility(View.INVISIBLE);

            if(paysChoisi.equals("")){
                erreurPays.setVisibility(View.VISIBLE);
                erreurPays.setText(getString(R.string.choixPays));
            }
            else if (suggestion.equals("")) {
                sugVilleTIL.setError(getString(R.string.rentrerSugVilleV2));
            }
            else if (suggestion.length() > longueurMaxVille) {
                sugVilleTIL.setError(getString(R.string.sugVilleTropLong));
            }
            else if(!(suggestion.matches(villePattern))) {
                sugVilleTIL.setError(getString(R.string.sugVilleCarSpeciaux));
            }
            else if (!BDD.isConnectedInternet(SuggestionVille.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            //ecrit ds bdd la nouvelle sugestion de ville
            else {
                if(sons)
                {
                    sonClick.start();
                }

                suggestionNouvelleVille(suggestion, paysChoisi);
            }
        }
    };

    private void suggestionNouvelleVille(String ville, String pays) {
        dialog.show();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.sugville("suggestionnouvelleville", ville, pays, pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("aucune")) {
                    Toast.makeText(getApplicationContext(), R.string.envoiSugVilleSucces, Toast.LENGTH_LONG).show();
                    SuggestionVille.this.finish();
                }
                else
                {
                    sugVilleTIL.setError(getString(R.string.sugVilleExisteDeja));
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

    private void recupPaysRetrofit() {
        dialogPays.show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        String langue = this.getResources().getConfiguration().locale.getDisplayLanguage();

        BDD.script service = retrofit.create(BDD.script.class);
        service.recupPays("recupPays", langue, pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (response.body().get(0).getErreur().equals("aucune"))
                {
                    int nb = Integer.parseInt(response.body().get(0).getNbPays());
                    for(int i = 0; i < nb; i++)
                    {
                        listePays.add(response.body().get(i + 1).getPays());
                    }
                }
                dialogPays.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialogPays.dismiss();
            }
        });
    }

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementEnvoiSuggestionVille);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    public void creationProgressDialogPays(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.recuperationPays);
        builder.setCancelable(false);
        dialogPays = builder.create();
    }
}
