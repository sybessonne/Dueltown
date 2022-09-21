package com.dueltown.statistiques;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dueltown.BDD;
import com.dueltown.R;
import com.dueltown.affichagesListes.AdapterClassementSemaine;
import com.dueltown.affichagesListes.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class classementSemaine extends AppCompatActivity {

    Dialog dialog;
    SwipeRefreshLayout swipeRefresh = null;

    TextView classementJoueur = null;
    TextView pointJoueur = null;
    TextView moyenneJoueur = null;
    TextView vous = null;
    LinearLayout scoreSemaineJoueur = null;

    //pour le recycler view
    private RecyclerView classement;
    private ArrayList listeclassement = new ArrayList<>();
    private AdapterClassementSemaine mAdapter;

    String pseudo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.classementsemaine);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // On récupère toutes les vues dont on a besoin
        classement = findViewById(R.id.classement);
        classementJoueur = findViewById(R.id.classementJoueur);
        pointJoueur = findViewById(R.id.pointsJoueur);
        moyenneJoueur = findViewById(R.id.moyenneJoueur);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        vous = findViewById(R.id.Vous);
        scoreSemaineJoueur = findViewById(R.id.scoreSemaineJoueur);

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

        //recupere le pseudo du joueur
        SharedPreferences preferences = getSharedPreferences("personne", 0);
        pseudo = preferences.getString("pseudo", "");

        //lance l'adpater pour la liste view
        mAdapter = new AdapterClassementSemaine(listeclassement, pseudo);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        classement.setLayoutManager(mLayoutManager);
        classement.setItemAnimator(new DefaultItemAnimator());
        classement.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        classement.setAdapter(mAdapter);

        //affiche les defis pas faits et ceux finis
        recupClassement();

        //lance la boite de dialogue qui dit que le classement a changé
        informationJoueur();
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
        service.classementSemaine("classementSemaine", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if (!(response.isSuccessful())) {
                    Toast.makeText(getApplicationContext(), R.string.echecRecuperationClassementSemaine, Toast.LENGTH_LONG).show();
                }
                else {
                    //recupere le nombre de ville a afficher
                    int nbJoueurs = Integer.parseInt(response.body().get(0).getNbJoueurs());
                    boolean estDansClassement = false;
                    for (int i = 1; i <= nbJoueurs ; i++)
                    {
                        //vérifie si le joueur est dans le classement de la semaine
                        if(response.body().get(i).getNomJoueur().equals(pseudo))
                        {
                            scoreSemaineJoueur.setVisibility(View.GONE);
                            estDansClassement = true;
                        }

                        String joueurs = (i) + "/" + response.body().get(i).getNomJoueur() + "/" +
                                response.body().get(i).getVille() + "/" + response.body().get(i).getPoints() + "/" +
                                response.body().get(i).getMoyenne();

                        listeclassement.add(joueurs);
                    }

                    //s'il il n'est pas dans le classement
                    if(!estDansClassement)
                    {
                        scoreSemaineJoueur.setVisibility(View.VISIBLE);
                        classementJoueur.setText(response.body().get(nbJoueurs + 1).getClassementJoueur());
                        pointJoueur.setText(response.body().get(nbJoueurs + 1).getPointsJoueur());
                        moyenneJoueur.setText(response.body().get(nbJoueurs + 1).getMoyJoueur() + "/5");
                        vous.setText(getString(R.string.vous));
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

    //sert a dire que le classement a changé
    private void informationJoueur()
    {
        //recuperation de la shared preference stockant le boolean
        //permettant de savoir si il faut afficher le changement de classement
        final SharedPreferences nouveautes = getSharedPreferences("nouveautes", 0);
        boolean vu = nouveautes.getBoolean("classementSemaine", false);

        if(!vu) {
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setCancelable(false);
            LayoutInflater inflater = this.getLayoutInflater();

            View dialogView = inflater.inflate(R.layout.dialogbox_info_jeu, null);
            dialogBuilder.setView(dialogView);

            TextView titre = dialogView.findViewById(R.id.titre);
            final TextView texte = dialogView.findViewById(R.id.texte);
            final TextView page = dialogView.findViewById(R.id.page);
            final Button suivant = dialogView.findViewById(R.id.boutonDroit);

            titre.setText(getString(R.string.information));
            texte.setText(getString(R.string.nouveauteClassementSemaine));
            page.setVisibility(View.INVISIBLE);
            suivant.setText(R.string.compris);

            final AlertDialog alertDialog = dialogBuilder.show();

            suivant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //sauvegarde comme quoi le joueur a vu la nouveaute
                    //pour  pas la reafficher
                    SharedPreferences.Editor editor = nouveautes.edit();
                    editor.putBoolean("classementSemaine", true);
                    editor.apply();

                    alertDialog.dismiss();
                }
            });
        }
    }
}

