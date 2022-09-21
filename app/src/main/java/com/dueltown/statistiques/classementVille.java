package com.dueltown.statistiques;

import android.app.AlertDialog;
import android.app.Dialog;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.dueltown.affichagesListes.AdapterClassementVilles;
import com.dueltown.affichagesListes.DividerItemDecoration;
import com.dueltown.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class classementVille extends AppCompatActivity {

    Dialog dialog;
    SwipeRefreshLayout swipeRefresh = null;

    //pour le recycler view
    private RecyclerView classement;
    private ArrayList listeclassement = new ArrayList<>();
    private AdapterClassementVilles mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classementville);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        classement = findViewById(R.id.classement);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        //gestion du swipe refresh layout
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listeclassement.clear();
                recupClassement();
            }
        });

        //couleurs pour le refresh layout
        swipeRefresh.setColorSchemeResources(R.color.couleur1,
                R.color.couleur2,
                R.color.couleur3,
                R.color.couleur4);

        //creation du progress dialog
        creationProgressDialog();
        dialog.show();

        //lance l'adpater pour la liste view
        mAdapter = new AdapterClassementVilles(listeclassement);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        classement.setLayoutManager(mLayoutManager);
        classement.setItemAnimator(new DefaultItemAnimator());
        classement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        classement.setAdapter(mAdapter);

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
        //recuperation du classement
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.classementville("classementVille").enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (!(response.isSuccessful())) {
                    Toast.makeText(getApplicationContext(), R.string.echecRecuperationClassementVille, Toast.LENGTH_LONG).show();
                }
                else {
                    //recupere le nombre de ville a afficher
                    int nbVillesDansClassement = Integer.parseInt(response.body().get(0).getNbVilles());
                    for (int i = 1; i <= nbVillesDansClassement ; i++)
                    {
                        String ville = (i) + "/" + response.body().get(i).getNomVille() + "/" +
                                response.body().get(i).getVictoires() + "/" +
                                response.body().get(i).getDefaites();

                        listeclassement.add(ville);
                    }
                }

                mAdapter.notifyDataSetChanged();

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialog.dismiss();

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }
}

