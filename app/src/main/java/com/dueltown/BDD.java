package com.dueltown;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Field;

public class BDD{

    /* ******************** IMPORTANT *************************
        Aide mémoire Au moment du développement et des test : faire en @GET
        au moment du déploiment : mettre en @POST
     */

    public static final String URL = "https://dueltown.alwaysdata.net/"; // en local host : http://192.168.1.23/
    private static final String DOSSIER_URL = "requetes_isjknd54v6def/script2.php"; // en local hosst : dueltown/script.php

    //tous les scripts pour se connecter a la base de donnee et envoi de donnee se trouvent ci après
    public interface script {
        //recupere les statistiques du joueurs
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> stats(@Field("parametre") String field1,
                              @Field("pseudo") String field2);

        //recupere les statistiques du joueurs
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> statsvillejoueur(@Field("parametre") String field1,
                                         @Field("ville") String field2);

        //recupere le classement des villes
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> classementville(@Field("parametre") String field1);

        //recupere le classement des joueurs dans la ville
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> classementjoueursville(@Field("parametre") String field1,
                                               @Field("ville") String field2);

        //recupere le classement des joueurs dans la france
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> classementjoueursfrance(@Field("parametre") String field1);

        //recupere le classement de la semaine
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> classementSemaine(@Field("parametre") String field1,
                                          @Field("pseudo") String field2);

        //recupere l'historique des défis
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> historique(@Field("parametre") String field1,
                                   @Field("pseudo") String field2);

        //connexion d'un joueur
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> connexion(@Field("parametre") String field1,
                                @Field("pseudo") String field2,
                                @Field("mdp") String field4);

        //envoi le message du joueur a la bdd
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> envoicontact(@Field("parametre") String field1,
                                     @Field("pseudo") String field2,
                                     @Field("email") String field3,
                                     @Field("objet") String field4,
                                     @Field("texte") String field5);

        //inscription d'un nouveau membre
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> inscription(@Field("parametre") String field1,
                                    @Field("pseudo") String field2,
                                    @Field("email") String field3,
                                    @Field("mdp") String field4,
                                    @Field("ville") String field5,
                                    @Field("pays") String field6);

        //recupere la liste des villes
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> villesPays(@Field("parametre") String field1,
                                   @Field("pseudo") String field2,
                                   @Field("ville") String field3,
                                   @Field("langue") String field4);

        //recupere la liste des villes qui ont au moins un joueur
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> villesLanceDefi(@Field("parametre") String field1,
                                        @Field("pseudo") String field2);

        //envoi la suggestion de ville
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> sugville(@Field("parametre") String field1,
                                 @Field("sugville") String field2,
                                 @Field("sugpays") String field3,
                                 @Field("pseudo") String field4);

        //recupere les pays disponibles
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> recupPays(@Field("parametre") String field1,
                                  @Field("langue") String field2,
                                  @Field("pseudo") String field3);

        //pour creer un nouveau defi
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> lancedefi(@Field("parametre") String field1,
                                  @Field("pseudo") String field2,
                                  @Field("ville1") String field3,
                                  @Field("ville2") String field4);

        //pour le mot de passe oublié
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> newMdp(@Field("parametre") String field1,
                               @Field("email") String field2);

        //pour enregistrer le nouveau mot de passe
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> enternewMdp(@Field("parametre") String field1,
                                    @Field("email") String field2,
                                    @Field("mdpprovisoire") String field3,
                                    @Field("mdp") String field4);

        //pour changer de mot de passe
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> mdp(@Field("parametre") String field1,
                            @Field("pseudo") String field2,
                            @Field("ancienmdp") String field3,
                            @Field("nouveaumdp") String field4);

        //pour changer l'email
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> email(@Field("parametre") String field1,
                              @Field("pseudo") String field2,
                              @Field("mdp") String field3,
                              @Field("nouveauemail") String field4);

        //pour changer la ville du joueur
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> changeville(@Field("parametre") String field1,
                                    @Field("pseudo") String field2,
                                    @Field("mdp") String field3,
                                    @Field("nouveauville") String field4,
                                    @Field("nouveaupays") String field5);

        //recupere les nouveau defi pour afficher une mise a jour
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> nouveauDefiNotif(@Field("parametre") String field1,
                                         @Field("ville") String field2,
                                         @Field("date") String field3);

        //recupere la liste des defis pas fait
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> listeDefis(@Field("parametre") String field1,
                                   @Field("pseudo") String field2);

        //recupere la liste des defis finis
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> listeDefisFinis(@Field("parametre") String field1,
                                        @Field("pseudo") String field2);

        //pour envoyer le score du defi
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> scorefindefi(@Field("parametre") String field1,
                                     @Field("pseudo") String field2,
                                     @Field("mdp") String field3,
                                     @Field("score") String field4,
                                     @Field("nomdefi") String field5,
                                     @Field("avancement") String field6);

        //pour soumettre une nouvelle question
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> soumquestion(@Field("parametre") String field1,
                                     @Field("pseudo") String field2,
                                     @Field("enonce") String field3,
                                     @Field("reponse1") String field4,
                                     @Field("reponse2") String field5,
                                     @Field("reponse3") String field6,
                                     @Field("reponse4") String field7);

        //pour signaler une question
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> signalerQuestion(@Field("parametre") String field1,
                                         @Field("pseudo") String field2,
                                         @Field("enonce") String field3,
                                         @Field("commentaire") String field4);

        //pour recuperer les statistiques d'un defi qui est dans l'historique
        @FormUrlEncoded
        @POST(DOSSIER_URL)
        Call<List<BDD>> solutionDefiFini(@Field("parametre") String field1,
                                         @Field("pseudo") String field2,
                                         @Field("ville1") String field3,
                                         @Field("ville2") String field4,
                                         @Field("dateDefi") String field5);
    }

    //fonction pour vérifier si la connection internet est activée sur le téléphone
    public static boolean isConnectedInternet(Activity activity)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            NetworkInfo.State networkState = networkInfo.getState();
            if (networkState.compareTo(NetworkInfo.State.CONNECTED) == 0){
                return true;
            }
            else return false;
        }
        else return false;
    }

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("pseudo")
    @Expose
    private String pseudo;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("mot_de_passe")
    @Expose
    private String mot_de_passe;
    @SerializedName("ville")
    @Expose
    private String ville;
    @SerializedName("pays")
    @Expose
    private String pays;
    @SerializedName("versionApp")
    @Expose
    private int versionApp;
    @SerializedName("dtCoins")
    @Expose
    private String dtCoins;
    @SerializedName("nbMessages")
    @Expose
    private String nbMessages;
    @SerializedName("nombreVilles")
    @Expose
    private String nombreVilles;
    @SerializedName("nbPays")
    @Expose
    private String nbPays;
    @SerializedName("nb_defiPasFait")
    @Expose
    private String nbDefiPasFait;
    @SerializedName("nb_defiFini")
    @Expose
    private String nbDefiFini;
    @SerializedName("nomDefi")
    @Expose
    private String nomDefi;
    @SerializedName("dateDefi")
    @Expose
    private String dateDefi;
    @SerializedName("enonce")
    @Expose
    private String enonce;
    @SerializedName("reponse1")
    @Expose
    private String reponse1;
    @SerializedName("reponse2")
    @Expose
    private String reponse2;
    @SerializedName("reponse3")
    @Expose
    private String reponse3;
    @SerializedName("reponse4")
    @Expose
    private String reponse4;
    @SerializedName("erreur")
    @Expose
    private String erreur;
    @SerializedName("NbChangementVille")
    @Expose
    private String NbChangementVille;

    public String getId() {
        return id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public String getEmail() {
        return email;
    }

    public String getMot_de_passe() {
        return mot_de_passe;
    }

    public String getVille() {
        return ville;
    }

    public String getPays() {
        return pays;
    }

    public int getVersionApp() {
        return versionApp;
    }

    public String getDtCoins() {return dtCoins; }
    public String getNbMessages() {return nbMessages; }

    public String getNombreVilles() {return nombreVilles; }
    public String getNbPays() {return nbPays; }
    public String getNbDefiPasFait() { return nbDefiPasFait; }
    public String getNbDefiFini() { return nbDefiFini; }

    public String getNomDefi() { return nomDefi;}

    public String getDateDefi() { return dateDefi;}

    public String getEnonce() {
        return enonce;
    }

    public String getReponse1() {
        return reponse1;
    }

    public String getReponse2() {
        return reponse2;
    }

    public String getReponse3() {        return reponse3;    }

    public String getReponse4() {
        return reponse4;
    }

    public String getErreur() {
        return erreur;
    }

    public String getNbChangementVille() {
        return NbChangementVille;
    }

    @Override
    public String toString() {
        return "{\"id\"='" + id + "'" +", \"pseudo\"='" + pseudo + ",\"ville\"='" + ville + "'}";
    }

    public BDD (String nomDefi) {
        this.nomDefi = nomDefi;
    }

    @SerializedName("listeVille")
    @Expose
    private List<String> listeVille = null;
    @SerializedName("listeMessages")
    @Expose
    private List<String> listeMessages = null;

    public List<String> getListeVille() {
        return listeVille;
    }

    public List<String> getListeMessages() {
        return listeMessages;
    }

    //Pour la liste des defis finis recuperer les informations du défis
    @SerializedName("scoreJoueur")
    @Expose
    private String scoreJoueur;
    @SerializedName("egalite")
    @Expose
    private String egalite;
    @SerializedName("villegagne")
    @Expose
    private String villegagne;
    @SerializedName("nbJvigagne")
    @Expose
    private String nbJvigagne;
    @SerializedName("moyevigagne")
    @Expose
    private String moyevigagne;
    @SerializedName("villeperd")
    @Expose
    private String villeperd;
    @SerializedName("nbJviperd")
    @Expose
    private String nbJviperd;
    @SerializedName("moyeviperd")
    @Expose
    private String moyeviperd;
    @SerializedName("avancement")
    @Expose
    private String avancement;

    public String getScoreJoueur() { return scoreJoueur;}

    public String getEgalite() {  return egalite; }

    public String getVillegagne() {  return villegagne; }

    public String getNbJvigagne() { return nbJvigagne; }

    public String getMoyevigagne() { return moyevigagne; }

    public String getVilleperd() { return villeperd; }

    public String getNbJviperd() { return nbJviperd; }

    public String getMoyeviperd() { return moyeviperd; }

    public String getAvancement() { return avancement; }

    //pour la page des statistiques
    @SerializedName("PtJoueur")
    @Expose
    private String ptJoueur;
    @SerializedName("nbDefisJouees")
    @Expose
    private String nbDefisJouees;
    @SerializedName("MoyenneJoueur")
    @Expose
    private String moyenneJoueur;
    @SerializedName("moyenneJoueur")
    @Expose
    private String moyJoueur;
    @SerializedName("classementJoueur")
    @Expose
    private String classementJoueur;
    @SerializedName("pointsJoueur")
    @Expose
    private String pointsJoueur;
    @SerializedName("classementVilleJoueur")
    @Expose
    private String classementVilleJoueur;
    @SerializedName("RangJoueurVille")
    @Expose
    private String rangJoueurVille;
    @SerializedName("NbMembresVille")
    @Expose
    private String nbMembresVille;
    @SerializedName("RangJoueurFrance")
    @Expose
    private String rangJoueurFrance;
    @SerializedName("NbMembresFrance")
    @Expose
    private String nbMembresFrance;
    @SerializedName("questsoum")
    @Expose
    private String questsoum;
    @SerializedName("questval")
    @Expose
    private String questval;

    public String getPtJoueur() {return ptJoueur;}

    public String getNbDefisJouees() {return nbDefisJouees;}

    public String getMoyenneJoueur() {return moyenneJoueur;}

    public String getMoyJoueur() {return moyJoueur;}

    public String getPointsJoueur() {return pointsJoueur;}

    public String getClassementJoueur() {return classementJoueur;}

    public String getRangJoueurVille() {return rangJoueurVille;}

    public String getNbMembresVille() {return nbMembresVille;}

    public String getRangJoueurFrance() {return rangJoueurFrance;}

    public String getNbMembresFrance() { return nbMembresFrance;}

    public String getQuestsoum() {return questsoum;}

    public String getQuestval() {  return questval;  }

    @SerializedName("nomVille")
    @Expose
    private String nomVille;

    public String getNomVille() {return nomVille; }

    @SerializedName("moyenne")
    @Expose
    private String moyenne;

    public String getMoyenne() {return moyenne; }

    @SerializedName("pointSemaine")
    @Expose
    private String pointSemaine;

    public String getPointSemaine() {return pointSemaine; }

    //classement des villes
    @SerializedName("nbVilles")
    @Expose
    private String nbVilles;
    @SerializedName("victoires")
    @Expose
    private String victoires;
    @SerializedName("defaites")
    @Expose
    private String defaites;

    public String getNbVilles() {return nbVilles;}

    public String getVictoires() {return victoires;}

    public String getDefaites() {return defaites;}

    //classement des joueurs dans la ville
    @SerializedName("nbJoueurs")
    @Expose
    private String nbJoueurs;
    @SerializedName("nomJoueur")
    @Expose
    private String nomJoueur;
    @SerializedName("points")
    @Expose
    private String points;
    @SerializedName("nbPortee")
    @Expose
    private int nbPortee;
    @SerializedName("nom")
    @Expose
    private String nom;

    public int getNbPortee() { return nbPortee;}
    public String getNom() { return nom;}

    public String getNbJoueurs() {return nbJoueurs;}

    public String getNomJoueur() {return nomJoueur;}

    public String getPoints() {return points;}

    //statistiques de la ville du joueur
    @SerializedName("nbVictoires")
    @Expose
    private String nbVictoires;
    @SerializedName("nbDefaites")
    @Expose
    private String nbDefaites;
    @SerializedName("nbEgalite")
    @Expose
    private String nbEgalite;
    @SerializedName("nbDefisJoues")
    @Expose
    private String nbDefisJoues;
    @SerializedName("villeGagnePlus")
    @Expose
    private String villeGagnePlus;
    @SerializedName("nbGagnePlus")
    @Expose
    private String nbGagnePlus;
    @SerializedName("villePerdPlus")
    @Expose
    private String villePerdPlus;
    @SerializedName("nbPerdPlus")
    @Expose
    private String nbPerdPlus;

    public String getNbVictoires() { return nbVictoires;}

    public String getNbDefaites() {return nbDefaites;}

    public String getNbEgalite() { return nbEgalite;}

    public String getNbDefisJoues() {return nbDefisJoues;}

    public String getVilleGagnePlus() {return villeGagnePlus;}

    public String getNbGagnePlus() {return nbGagnePlus;}

    public String getVillePerdPlus() {return villePerdPlus;}

    public String getNbPerdPlus() {return nbPerdPlus;}

    //pour historique
    @SerializedName("nbDefis")
    @Expose
    private String nbDefis;

    public String getNbDefis() {return nbDefis;}

    //pour notification service
    @SerializedName("DerniereMajDefis")
    @Expose
    private String DerniereMajDefis;

    public String getDerniereMajDefis() {return DerniereMajDefis;}


}


