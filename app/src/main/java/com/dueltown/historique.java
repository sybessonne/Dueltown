package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.dueltown.affichagesListes.AdapterHistorique;
import com.dueltown.affichagesListes.DividerItemDecoration;
import com.dueltown.affichagesListes.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class historique extends AppCompatActivity {

    Dialog dialog;
    SwipeRefreshLayout swipeRefresh = null;

    //pour le recycler view
    private RecyclerView classement;
    TextView vide = null;

    private ArrayList listeclassement = new ArrayList<>();
    private AdapterHistorique mAdapter;

    SharedPreferences preferences;
    String pseudo;
    String ville;

    boolean actualise = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historique);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        creationProgressDialog();
        dialog.show();

        // On récupère toutes les vues dont on a besoin
        classement = findViewById(R.id.historiqueDefis);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        vide = findViewById(R.id.vide);

        vide.setVisibility(View.INVISIBLE);

        //recuperation du pseudo du joueur et de la ville
        preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");
        ville = preferences.getString("ville", "");

        //lance l'adapter pour la liste view
        mAdapter = new AdapterHistorique(listeclassement, ville, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        classement.setLayoutManager(mLayoutManager);
        classement.setItemAnimator(new DefaultItemAnimator());
        classement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        classement.setAdapter(mAdapter);
        classement.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), classement, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (!BDD.isConnectedInternet(historique.this)) {
                    Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(!actualise)
                    {
                        //lancement du layout du jeu des questions
                        Intent intent = new Intent(historique.this, solutionDefiFiniHistorique.class);

                        //pour passer le nom du defi
                        //on le reparse dans l'activité d'apres
                        intent.putExtra("nomDefi", listeclassement.get(position).toString());
                        intent.putExtra("pseudo", pseudo);
                        startActivityForResult(intent, 0);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        actualise = true;
        loadHistoric();

        //gestion du swipe refresh layout
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualise = true;
                listeclassement.clear();
                loadHistoric();
            }
        });

        //couleurs pour le refresh layout
        swipeRefresh.setColorSchemeResources(R.color.couleur1,
                R.color.couleur2,
                R.color.couleur3,
                R.color.couleur4);
    }

    //pour la fleche de retour
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

    private void loadHistoric(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.historique("historique", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if(response.isSuccessful())
                {
                    int nbDefis = Integer.parseInt(response.body().get(0).getNbDefis());
                    for(int i = 1; i <= nbDefis; i++)
                    {
                        listeclassement.add(
                                response.body().get(i).getNomDefi() + "/" +
                                response.body().get(i).getScoreJoueur() + "/" +
                                response.body().get(i).getEgalite()
                        );
                    }

                    if(listeclassement.isEmpty())
                    {
                        classement.setVisibility(View.GONE);
                        vide.setVisibility(View.VISIBLE);
                    }
                }

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }

                mAdapter.notifyDataSetChanged();
                dialog.dismiss();
                actualise = false;
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                classement.setVisibility(View.GONE);
                vide.setVisibility(View.VISIBLE);

                //efface le swipe refresh si il est en train de tourner
                if(swipeRefresh.isRefreshing())
                {
                    swipeRefresh.setRefreshing(false);
                }
                actualise = false;
            }
        });
    }

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementHistorique);
        builder.setCancelable(false);
        dialog = builder.create();
    }
}
