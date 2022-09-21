package com.dueltown;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationService extends Service {

    //variable pour dire si une notification est présente
    public static int notificationActive = 0;

    //gestion du temps
    Timer t;
    TimerTask task;
    int time;
    final int tempsEntreAppelServer = 5;

    NetworkInfo network;
    BDD.script service;
    Uri alarmSound;

    Retrofit retrofit;

    SharedPreferences preferencesJoueur;
    SharedPreferences preferencesParametres;;
    SharedPreferences.Editor e;

    String ville;
    String derniereMajDefis;

    boolean son;
    boolean vibreur;
    boolean yesOrNo;

    // Sets an ID for the notification
    int mNotificationId = 001;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;
    Intent resultIntent;
    PendingIntent resultPendingIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferencesJoueur = getSharedPreferences("personne", 0);
        preferencesParametres = getSharedPreferences("parametres", 0);

        ville = preferencesJoueur.getString("ville", "");
        yesOrNo = preferencesParametres.getBoolean("notifications", true);

        //on verifie si il y a internet activé et ville pas vide et qu'on veut les notifs
        if(isConnectedInternet() && !ville.equals("") && yesOrNo){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BDD.URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            loadNouveauDefiNotif();
        }
        else {
            this.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //fonction pour vérifier si la connection internet est activée sur le téléphone
    private boolean isConnectedInternet(){
        network = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (network == null || !network.isConnected()) {
            // Le périphérique n'est pas connecté à Internet
            return false;
        }

        // Le périphérique est connecté à Internet
        return true;
    }

    private void loadNouveauDefiNotif() {
        derniereMajDefis = preferencesParametres.getString("derniereMajDefis", "");

        service = retrofit.create(BDD.script.class);
        service.nouveauDefiNotif("nouveauDefiNotif", ville, derniereMajDefis).enqueue(new Callback<List<BDD>>() {
            @Override
            public void onResponse(Call<List<BDD>> call, Response<List<BDD>> response) {
                int nbVilles = Integer.parseInt(response.body().get(0).getNbVilles());

                if (nbVilles != 0) {
                    creationNotification(response.body().get(1).getVille()); // on prend la  premiere

                    //todo a amelioreer en creant des notification de groupe
                    /*for(int i = 0; i < nbVilles; i++)
                    {
                        creationNotification(response.body().get(i + 1).getVille());
                    }*/

                    notificationActive = 1;
                }

                //on met à jour la date de derniere mise a jour
                e = preferencesParametres.edit();
                e.putString("derniereMajDefis", response.body().get(0).getDerniereMajDefis());
                e.apply();

                time = tempsEntreAppelServer;
                tempsAttente();
            }

            @Override
            public void onFailure(Call<List<BDD>> call, Throwable t) {
                NotificationService.this.stopSelf();
            }
        });
    }

    public void tempsAttente(){
        t = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
                if (time != 0) {
                    time = time - 1;
                }
                else {
                    t.cancel();
                    task.cancel();

                    if(isConnectedInternet()) {
                        preferencesJoueur = getSharedPreferences("personne", 0);
                        preferencesParametres = getSharedPreferences("parametres", 0);

                        ville = preferencesJoueur.getString("ville", "");
                        yesOrNo = preferencesParametres.getBoolean("notifications", true);

                        if(!ville.equals("") && yesOrNo){
                            loadNouveauDefiNotif();
                        }
                        else
                        {
                            NotificationService.this.stopSelf();
                        }
                    }
                    else {
                        NotificationService.this.stopSelf();
                    }
                }
            }
        };
        t.scheduleAtFixedRate(task, 0, 1000);
    }

    public void creationNotification(String nom){
        mBuilder = new NotificationCompat.Builder(this, "CH_ID")
                        .setSmallIcon(R.drawable.ic_notif)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.NotifNouveauDefi, nom))
                        .setLights(Color.GREEN, 2000, 3000);

        son = preferencesParametres.getBoolean("sons", true);
        vibreur = preferencesParametres.getBoolean("vibrations", true);
        if(son){
            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
        }
        if(vibreur){
            mBuilder.setVibrate(new long[] { 0, 1000});
        }

        mBuilder.setAutoCancel(true); // supprime la notif après clique dessus

        resultIntent = new Intent(this, demarrage.class);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}