package com.dueltown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.dueltown.affichagesListes.AdapterDefisFinis;
import com.dueltown.affichagesListes.AdapterDefisPasFinis;
import com.dueltown.affichagesListes.DividerItemDecoration;
import com.dueltown.affichagesListes.RecyclerTouchListener;
import com.dueltown.parametres.parametres;
import com.dueltown.parametres.moncompte;
import com.dueltown.statistiques.statistiques;

import org.w3c.dom.Text;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class principal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //pour pouvoir fermer l'activité lors de la déconnexion
    static principal ActivityPrincipal;
    public static threadRecupNotifications threadNotification;
    ActionBarDrawerToggle toggle;

    TextView dtCoinsTextView;

    //recuperation des element du layout
    SwipeRefreshLayout swipeRefresh = null;
    TextView bienvenue = null;
    LinearLayout defi = null;
    TextView aucun1 = null;
    TextView aucun2 = null;

    SharedPreferences preferences;
    String pseudo;

    Dialog dialog;
    boolean actualiseOk = true; // pour attendre avant de actualisr 2 fois de suite
    //boolean pour synchroniser les 2 appels retrofit pour le progress dialog
    boolean finRecup1;
    boolean finRecup2;

    //pour le recycler view
    private RecyclerView listeDefisPasFinis;
    private RecyclerView listeDefisFinis;
    private ArrayList defisPasFinis = new ArrayList<>();
    private ArrayList defisFinis = new ArrayList<>();
    private AdapterDefisPasFinis mAdapter;
    private AdapterDefisFinis mAdapter2;

    //pour le son
    MediaPlayer sonClick;
    boolean sons;

    int numPage = 1; //numero de la page actuelle dans boite de dialogue information du jeu
    int nbPages = 5; //nombre de page dans boite dialogue information jeu

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        ActivityPrincipal = this;

        // On récupère toutes les vues dont on a besoin
        swipeRefresh = findViewById(R.id.swipeRefresh);
        bienvenue = findViewById(R.id.bienvenue);
        defi = findViewById(R.id.defi);
        listeDefisPasFinis = findViewById(R.id.listeDefisPasFinis);
        listeDefisFinis = findViewById(R.id.listeDefisFinis);
        aucun1 = findViewById(R.id.aucun1);
        aucun2 = findViewById(R.id.aucun2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close){

            // Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            // Called when a drawer has settled in a completely closed state.
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        //gestion du swipe refresh layout
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                actualiseOk = false;
                actualiser();
            }
        });

        //couleurs pour le refresh layout
        swipeRefresh.setColorSchemeResources(R.color.couleur1,
                R.color.couleur2,
                R.color.couleur3,
                R.color.couleur4);

        preferences = getSharedPreferences("personne", 0);
        String ville = preferences.getString("ville", "");
        pseudo = preferences.getString("pseudo", "");

        //gestion du navigation view et du pseudo du joueur
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);
        TextView textPseudo = v.findViewById(R.id.pseudo);
        dtCoinsTextView = v.findViewById(R.id.dtCoins);
        textPseudo.setText(pseudo);

        //affiche un message pour dire quel utilisateur est connecté
        bienvenue.setText(ville);

        // On attribue un listener adapté aux vues qui en ont besoin
        defi.setOnClickListener(lanceDefiListener);

        //creation du progress dialog
        creationProgressDialog();
        dialog.show();

        //supprime les notifications presentes
        deleteNotification();

        //charge le son de click
        SharedPreferences pref = getSharedPreferences("parametres", 0);
        sons = pref.getBoolean("bruitages", true);
        sonClick = MediaPlayer.create(principal.this, R.raw.bruit_bouton);

        //affiche les defis pas faits et ceux finis
        finRecup1 = false;
        finRecup2 = false;
        actualiseListes();

        //lancement du thread de recuperation des notifications
        //lancement si c'est la premiere fois
        if(threadNotification == null)
        {
            threadNotification = new threadRecupNotifications();
            threadNotification.setRunning(true);
            threadNotification.start();
        }
        //sinon si le thread est arrete ou en attente, on le relance
        else if(threadNotification.getState().equals(Thread.State.TERMINATED) ||
                threadNotification.getState().equals(Thread.State.TIMED_WAITING))
        {
            threadNotification = new threadRecupNotifications();
            threadNotification.setRunning(true);
            threadNotification.start();
        }

        //lance la boite de dialogue d'information du jeu si c'est la premiere fois
        //qu'on ouvre l'application après une mise a jour importante
        informationJoueur();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } 
        else {
            super.onBackPressed();
            threadNotification.setRunning(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //quand on clic sur la fleche pour actualiser
        if (id == R.id.actualise) {
            if (!BDD.isConnectedInternet(principal.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                if(actualiseOk)
                {
                    actualiseOk = false;
                    actualiser();
                }
            }
            return true;
        }
        else if(id == R.id.parametres)
        {
            Intent intent = new Intent(principal.this, parametres.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.stats) {
            if (!BDD.isConnectedInternet(principal.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(principal.this, statistiques.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.historique) {
            if (!BDD.isConnectedInternet(principal.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(principal.this, historique.class);
                startActivity(intent);
            }
        }
        else if (id == R.id.question) {
            if (!BDD.isConnectedInternet(principal.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            }
            else {
                Intent intent = new Intent(principal.this, soumquestion.class);
                startActivity(intent);
            }
        }
        else if(id == R.id.newVille)
        {
            //on passe le pseudo dans l'intent
            Intent intent = new Intent(principal.this, SuggestionVille.class);
            //pour passer le pseudo dans l'intent
            intent.putExtra("pseudo", pseudo);
            startActivityForResult(intent, 0);
        }
        else if(id == R.id.partager)
        {
            partager();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void partager()
    {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String shareBody = getString(R.string.textePartage);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.sujetPartage));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.titrePartage)));
    }

    // Uniquement pour le bouton defi
    private View.OnClickListener lanceDefiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!BDD.isConnectedInternet(principal.this)) {
                Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();
            } 
            else {
                if(sons)
                {
                    sonClick.start();
                }

                Intent intent = new Intent(principal.this, lanceDefi.class);
                startActivity(intent);
            }
        }
    };

    public void creationProgressDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progressdialog, null);
        builder.setView(dialogView);
        TextView message = dialogView.findViewById(R.id.message);
        message.setText(R.string.chargementDesDefis);
        builder.setCancelable(false);
        dialog = builder.create();
    }

    private void recupDefisPasFinis() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BDD.script service = retrofit.create(BDD.script.class);
        service.listeDefis("recupListeDefis", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if ((response.body().toString()).equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.recuperationDefisImpossible, Toast.LENGTH_LONG).show();
                    aucun1.setVisibility(View.VISIBLE);
                    aucun1.setText(R.string.aucunDefisDisponible);
                }
                else {
                    //recupere les defis et les questions et les reponses
                    int nbDeDefisPasFaits = Integer.parseInt(response.body().get(0).getNbDefiPasFait());
                    int increment; //pour pas faire le calcul à chaque fois

                    TextView txt = findViewById(R.id.defPasFinis);
                    txt.setText(getString(R.string.defisPasFaitsV2, nbDeDefisPasFaits));

                    SharedPreferences preferencesDefisPasFinis = getSharedPreferences("defisPasFinis", 0);
                    SharedPreferences.Editor editor = preferencesDefisPasFinis.edit();

                    for (int i = 1; i <= nbDeDefisPasFaits; i++) {
                        increment = 1 + 6 * (i - 1);
                        String nomDefi = response.body().get(increment).getNomDefi(); //1 et 7
                        defisPasFinis.add(nomDefi);

                        for (int k = 1; k <= 5; k++) {
                            editor.putString(nomDefi + "question" + k, response.body().get(k + increment).getEnonce());
                            editor.putString(nomDefi + "reponse1" + k, response.body().get(k + increment).getReponse1());
                            editor.putString(nomDefi + "reponse2" + k, response.body().get(k + increment).getReponse2());
                            editor.putString(nomDefi + "reponse3" + k, response.body().get(k + increment).getReponse3());
                            editor.putString(nomDefi + "reponse4" + k, response.body().get(k + increment).getReponse4());
                        }
                        editor.apply();
                    }
                    mAdapter.notifyDataSetChanged();

                    //affiche un message pour dire qu'il n'y a aucun défi de disponible
                    if(defisPasFinis.size() != 0){
                        aucun1.setVisibility(View.INVISIBLE);
                        aucun1.setPadding(0,0,0,0);
                    }
                    else {
                        aucun1.setVisibility(View.VISIBLE);
                        aucun1.setText(R.string.defisTermines);

                        //on en profite pour supprimer les avancements des défis
                        SharedPreferences avancementDefiDefi = getSharedPreferences("avancementDefi", 0);
                        preferencesDefisPasFinis.edit().clear().apply();
                        avancementDefiDefi.edit().clear().apply();
                    }

                    //enregistre la date du dernier chargement des defis pour la gestion de notification service
                    SharedPreferences p = getSharedPreferences("parametres", 0);
                    SharedPreferences.Editor e = p.edit();
                    e.putString("derniereMajDefis", response.body().get(0).getDerniereMajDefis());
                    e.apply();

                    //chargement du nombre de dtCoins
                    dtCoinsTextView.setText(response.body().get(0).getDtCoins());

                    //chargement des messages temporaires
                    int nbMessages = Integer.parseInt(response.body().get(0).getNbMessages());
                    for(int i = nbMessages - 1; i >= 0; i--)
                    {
                        creationInformation(response.body().get(0).getListeMessages().get(i));
                    }
                }

                //pour la synchronisation
                finRecup1 = true;
                if(finRecup1 && finRecup2)
                {
                    dialog.dismiss();
                    actualiseOk = true;

                    //efface le swipe refresh si il est en train de tourner
                    if(swipeRefresh.isRefreshing())
                    {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();
                aucun1.setVisibility(View.VISIBLE);
                aucun1.setText(R.string.aucunDefisDisponible);
                aucun2.setVisibility(View.VISIBLE);
                aucun2.setText(R.string.aucunDefisDisponible);

                //pour la synchronisation
                finRecup1 = true;
                if(finRecup1 && finRecup2)
                {
                    dialog.dismiss();
                    actualiseOk = true;

                    //efface le swipe refresh si il est en train de tourner
                    if(swipeRefresh.isRefreshing())
                    {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    //affiche le message temporaire
    private void creationInformation(String message)
    {
        //on split le message
        String msg [] = message.split(":");

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setCancelable(false);
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialogbox_info_jeu, null);
        dialogBuilder.setView(dialogView);

        TextView titre = dialogView.findViewById(R.id.titre);
        final TextView texte = dialogView.findViewById(R.id.texte);
        final TextView page = dialogView.findViewById(R.id.page);
        final Button ok = dialogView.findViewById(R.id.boutonDroit);

        titre.setText(msg[0]);
        texte.setText(getString(R.string.messageTemp, msg[1]));
        page.setVisibility(View.INVISIBLE);
        ok.setText(R.string.ok);

        final AlertDialog alertDialog = dialogBuilder.show();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    private void recupListeDefisFinis() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BDD.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        BDD.script service = retrofit.create(BDD.script.class);
        service.listeDefisFinis("recupListeDefisFinis", pseudo).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                if ((response.body().toString()).equals("[]")) {
                    Toast.makeText(getApplicationContext(), R.string.recuperationDefisImpossible, Toast.LENGTH_LONG).show();
                    aucun2.setText(R.string.aucunDefisDisponible);
                }
                else {
                    //verfie qu'il y a au moint un defi
                    String erreur = response.body().get(0).getErreur();

                    //si un defi alors on affiche sinon on fait rien
                    if (erreur.equals("0")) {
                        //recupere les defis et les questions et les reponses
                        int nbDeDefisFaits = Integer.parseInt(response.body().get(0).getNbDefiFini());
                        int increment; //pour pas calculer à chaque fois

                        TextView txt = findViewById(R.id.defFinis);
                        txt.setText(getString(R.string.defisFaitsV2, nbDeDefisFaits));

                        SharedPreferences preferencesDefisFinis = getSharedPreferences("defisFinis", 0);
                        SharedPreferences.Editor editor = preferencesDefisFinis.edit();

                        for (int i = 1; i <= nbDeDefisFaits; i++)
                        {
                            increment = 1 + 6 * (i - 1);
                            String nomDefi = response.body().get(increment).getNomDefi();

                            //recuperer les informations du defis (nb  joueurs, moyenne, gagnant...)
                            editor.putString(nomDefi + "scoreJoueur", response.body().get(increment).getScoreJoueur());
                            editor.putString(nomDefi + "avancement", response.body().get(increment).getAvancement());
                            editor.putString(nomDefi + "villegagne", response.body().get(increment).getVillegagne());
                            editor.putString(nomDefi + "nbJvigagne", response.body().get(increment).getNbJvigagne());
                            editor.putString(nomDefi + "moyevigagne", response.body().get(increment).getMoyevigagne());
                            editor.putString(nomDefi + "villeperd", response.body().get(increment).getVilleperd());
                            editor.putString(nomDefi + "nbJviperd", response.body().get(increment).getNbJviperd());
                            editor.putString(nomDefi + "moyeviperd", response.body().get(increment).getMoyeviperd());

                            //recupere le resultat du defi, c'est a dire si il y a eu une egalite ou pas
                            String egalite = response.body().get(increment).getEgalite();

                            //ajout a la fin si il y a eu l'egalite, sinon ajout de la ville gagnante
                            if(egalite.equals("egalite")) {
                                defisFinis.add(nomDefi + "/" + egalite);
                            }
                            else {
                                defisFinis.add(nomDefi + "/" + response.body().get(increment).getVillegagne());
                            }

                            //recupere les questions et reponses du defi
                            for (int k = 1; k <= 5; k++) 
                            {
                                editor.putString(nomDefi + "question" + k, response.body().get(increment + k).getEnonce());
                                editor.putString(nomDefi + "reponse1" + k, response.body().get(increment + k).getReponse1());
                                editor.putString(nomDefi + "reponse2" + k, response.body().get(increment + k).getReponse2());
                                editor.putString(nomDefi + "reponse3" + k, response.body().get(increment + k).getReponse3());
                                editor.putString(nomDefi + "reponse4" + k, response.body().get(increment + k).getReponse4());
                            }
                            editor.apply();
                        }
                        mAdapter2.notifyDataSetChanged();
                    }
                }

                //affiche un message pour dire qu'il n'y a aucun défi de disponible
                if(defisFinis.size() != 0){
                    aucun2.setVisibility(View.INVISIBLE);
                    aucun2.setPadding(0,0,0,0);
                }
                else {
                    aucun2.setVisibility(View.VISIBLE);
                    aucun2.setText(R.string.aucunDefiDisponible);

                    SharedPreferences preferencesDefisFinis = getSharedPreferences("defisFinis", 0);
                    preferencesDefisFinis.edit().clear().apply();
                }

                //pour la synchronisation
                finRecup2 = true;
                if(finRecup1 && finRecup2)
                {
                    dialog.dismiss();
                    actualiseOk = true;

                    //efface le swipe refresh si il est en train de tourner
                    if(swipeRefresh.isRefreshing())
                    {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.ConnexionImpossible, Toast.LENGTH_LONG).show();

                //pour la synchronisation
                finRecup2 = true;
                if(finRecup1 && finRecup2)
                {
                    dialog.dismiss();
                    actualiseOk = true;

                    //efface le swipe refresh si il est en train de tourner
                    if(swipeRefresh.isRefreshing())
                    {
                        swipeRefresh.setRefreshing(false);
                    }
                }
            }
        });
    }

    private void actualiseListes() {
        //liste les defis pas encore faits
        mAdapter = new AdapterDefisPasFinis(defisPasFinis, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listeDefisPasFinis.setLayoutManager(mLayoutManager);
        listeDefisPasFinis.setItemAnimator(new DefaultItemAnimator());
        listeDefisPasFinis.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        listeDefisPasFinis.setAdapter(mAdapter);
        listeDefisPasFinis.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), listeDefisPasFinis, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //lancement du layout du jeu des questions
                Intent intent = new Intent(principal.this, activityIntermediaireDefis.class);
                //pour passer le nom du defi dans l'activite des questions
                intent.putExtra("nomDefi", defisPasFinis.get(position).toString());
                startActivityForResult(intent, 0);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        //si cela ne  fonctionne pas c'est que les etatDefisEnCours de la bdd ne correspondent pas avec les defis disponibles de la bdd
        recupDefisPasFinis();

        //liste les defis finis il y a moins de 2 jours
        mAdapter2 = new AdapterDefisFinis(defisFinis, this);
        RecyclerView.LayoutManager mLayoutManager2 = new LinearLayoutManager(getApplicationContext());
        listeDefisFinis.setLayoutManager(mLayoutManager2);
        listeDefisFinis.setItemAnimator(new DefaultItemAnimator());
        listeDefisFinis.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        // set the adapter
        listeDefisFinis.setAdapter(mAdapter2);
        listeDefisFinis.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), listeDefisFinis, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(actualiseOk)
                {
                    Intent intent = new Intent(principal.this, solutionDefiFini.class);
                    //pour enlever le dernier parametre du nom qui servait a savoir qu'elle ville gagne ou l'egalité
                    String infosDefis [] = defisFinis.get(position).toString().split("/");
                    //pour passer le nom du defi dans l'activite des questions ainsi que l'egalite
                    Bundle extras = new Bundle();
                    extras.putString("egalite", infosDefis[3]);
                    extras.putString("nomDefi", infosDefis[0] + "/" + infosDefis[1] + "/" + infosDefis[2]);
                    intent.putExtras(extras);
                    startActivityForResult(intent, 0);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        //si cela ne  fonctionne pas c'est que les etatDefisEnCours de la bdd ne correspondent pas avec les defis disponibles de la bdd
        recupListeDefisFinis();

    }

    public static principal getInstance() {
        return ActivityPrincipal;
    }

    private void deleteNotification() {
        //on enleve les notifications qui sont presentes
        final NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationService.NOTIFICATION_SERVICE);
        //la suppression de la notification se fait grâce à son ID
        notificationManager.cancel(001);
        NotificationService.notificationActive = 0;
    }

    public void actualiser() {
        dialog.show();
        threadNotification.starting();

        if (!BDD.isConnectedInternet(principal.this)) {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), R.string.activerConnexionInternet, Toast.LENGTH_LONG).show();

            //eleve le swipe refresh layout si il est actif
            if(swipeRefresh.isRefreshing())
            {
                swipeRefresh.setRefreshing(false);
            }
        }
        else {
            //vide les arraylist avant de les remplir
            defisPasFinis.clear();
            defisFinis.clear();

            //supprimer les notifications présentes
            deleteNotification();

            //recuperer les defis et les questions
            finRecup1 = false;
            finRecup2 = false;
            recupDefisPasFinis();
            recupListeDefisFinis();
        }
    }

    public static threadRecupNotifications getThread() {
        return threadNotification;
    }

    //thread pour ecouter les notifications
    public class threadRecupNotifications extends Thread
    {
        private boolean running = false;
        private int pause = 2000;

        public void setRunning(boolean run) {
            running = run;

            //pour les parametres du son quand on change le parametre et qu'on revient sur l'activité principale
            if(run)
            {
                SharedPreferences pref = getSharedPreferences("parametres", 0);
                sons = pref.getBoolean("bruitages", true);
            }

            /*
                Lignes pour vérifie l'ouverture des différentes activity d'arrière plan
                C'est fait comme ca pour empecher les ecrans blancs entre activity
                pour empecher de faire mal aux yeux
            */

            SharedPreferences p = getSharedPreferences("parametres", 0);
            boolean b = p.getBoolean("activityQuestionDefiOuverte", false);
            if(b)
            {
                //on remet a faux
                SharedPreferences.Editor editor = p.edit();
                editor.putBoolean("activityQuestionDefiOuverte", false);
                editor.apply();

                questionsdefi.getInstance().finish();
            }

            b = p.getBoolean("activityIntermediaireOuverte", false);
            if(b)
            {
                //on remet a faux
                SharedPreferences.Editor editor = p.edit();
                editor.putBoolean("activityIntermediaireOuverte", false);
                editor.apply();

                activityIntermediaireDefis.getInstance().finish();
            }

            b = p.getBoolean("activityConnexionOuverte", false);
            if(b)
            {
                //on remet a faux
                SharedPreferences.Editor editor = p.edit();
                editor.putBoolean("activityConnexionOuverte", false);
                editor.apply();

                connexion.getInstance().finish();
            }

            b = p.getBoolean("activityInscriptionOuverte", false);
            if(b)
            {
                //on remet a faux
                SharedPreferences.Editor editor = p.edit();
                editor.putBoolean("activityInscriptionOuverte", false);
                editor.apply();

                inscription.getInstance().finish();
            }

            b = p.getBoolean("activityChoixVilleOuverte", false);
            if(b)
            {
                //on remet a faux
                SharedPreferences.Editor editor = p.edit();
                editor.putBoolean("activityChoixVilleOuverte", false);
                editor.apply();

                choixVille.getInstance().finish();
            }
        }

        public void pause(){
            pause = 5000;
        }

        public void starting(){
            pause = 2000;
        }

        @Override
        public void run()
        {
            while (running) {
                if(principal.this.getWindow().getDecorView().getRootView().isShown()) {
                    this.starting();
                    if(NotificationService.notificationActive != 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actualiser();
                            }
                        });
                    }
                }
                else{
                    this.pause();
                }

                try {
                    sleep(pause);
                }
                catch (InterruptedException e) {}
            }
            return;
        }
    }

    //sert à afficher les explications de jeu si celles ci n'ont pas été vu par le joueur
    private void informationJoueur()
    {
        //recuperation de la shared preference stockant le boolean
        //permettant de savoir si il faut afficher les explications du jeu
        final SharedPreferences nouveautes = getSharedPreferences("nouveautes", 0);
        boolean vu = nouveautes.getBoolean("vu" + getString(R.string.numeroVersionMajImportant), false);

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

            titre.setText(getString(R.string.titreInfoJeu, getString(R.string.app_name)));
            texte.setText(getString(R.string.infoJeu1));
            page.setText(getString(R.string.pagesInfoJeu, numPage, nbPages));
            suivant.setText(R.string.suivant);

            final AlertDialog alertDialog = dialogBuilder.show();

            suivant.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(sons)
                    {
                        sonClick.start();
                    }

                    numPage++;

                    page.setText(getString(R.string.pagesInfoJeu, numPage, nbPages));

                    switch (numPage)
                    {
                        case 2:
                            texte.setText(getString(R.string.infoJeu2));
                            break;
                        case 3:
                            texte.setText(getString(R.string.infoJeu3));
                            break;
                        case 4:
                            texte.setText(getString(R.string.infoJeu4));
                            break;
                        case 5:
                            texte.setText(getString(R.string.infoJeu5));
                            suivant.setText(R.string.compris);
                            break;
                        case 6:
                            SharedPreferences.Editor editor = nouveautes.edit();
                            editor.putBoolean("vu" + getString(R.string.numeroVersionMajImportant), true);
                            editor.apply();
                            alertDialog.dismiss();
                            break;
                    }
                }
            });
        }
    }
}
