package com.dueltown.statistiques;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dueltown.BDD;
import com.dueltown.affichagesListes.AdapterClassementJoueursVille;
import com.dueltown.affichagesListes.DividerItemDecoration;
import com.dueltown.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class classementJoueursVille extends AppCompatActivity {

    Dialog dialog;

    //pour le recycler view
    private RecyclerView classement;
    private ArrayList listeclassement = new ArrayList<>();
    private AdapterClassementJoueursVille mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classementjoueursville);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        classement = findViewById(R.id.classement);

        //creation du progress dialog
        creationProgressDialog();
        dialog.show();

        //affiche les defis pas faits et ceux finis
        recupClassement();
    }

    //pour la clique sur la flèche retour
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

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementClassement);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    private void recupClassement() {
        //lance l'adpater pour la liste view
        mAdapter = new AdapterClassementJoueursVille(listeclassement);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        classement.setLayoutManager(mLayoutManager);
        classement.setItemAnimator(new DefaultItemAnimator());
        classement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        classement.setAdapter(mAdapter);

        //recuperation du classement
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SharedPreferences preferences = getSharedPreferences("personne", 0);
        String villeJoueur = preferences.getString("ville", "");

        BDD.script service = retrofit.create(BDD.script.class);
        service.classementjoueursville("classementJoueurVille", villeJoueur).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (!(response.isSuccessful())) {
                    Toast.makeText(getApplicationContext(), R.string.echecRecuperationClassementJoueurs, Toast.LENGTH_LONG).show();
                }
                else {
                    //recupere le nombre de ville a afficher
                    int nbJoueurs = Integer.parseInt(response.body().get(0).getNbJoueurs());
                    for (int i = 1; i <= nbJoueurs ; i++)
                    {
                        String joueurs = (i) + "/" + response.body().get(i).getNomJoueur() + "/" +
                                response.body().get(i).getPoints();

                        listeclassement.add(joueurs);
                    }
                }

                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
}

