package com.dueltown.affichagesListes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dueltown.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdapterHistorique extends RecyclerView.Adapter<AdapterHistorique.MyViewHolder> {

    private List villeList;
    private String villeJoueur;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView resultat;
        public TextView date;
        public TextView score;

        public MyViewHolder(View view) {
            super(view);
            resultat = view.findViewById(R.id.resultat);
            date = view.findViewById(R.id.date);
            score = view.findViewById(R.id.score);
        }
    }

    public AdapterHistorique(List villeList, String ville, Context c) {
        this.villeList = villeList;
        this.villeJoueur = ville;
        this.mContext = c;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_historique, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String infos[] = villeList.get(position).toString().split("/");
        /* information
        1er = ville gagnante
        2eme viller perdante
        3eme date
        4eme score
        5eme egalite ou pas
        */

        //pour les résultats du défis
        String villeAdverse;

        if(infos[4].equals("oui")){
            //recuperation de la ville adverse
            if(infos[0].equals(this.villeJoueur)){
                villeAdverse = infos[1];
            }
            else {
                villeAdverse = infos[0];
            }

            holder.resultat.setText(mContext.getString(R.string.egaliteContreV2, villeAdverse));
            holder.resultat.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
        else {
            if(infos[0].equals(villeJoueur)){
                villeAdverse = infos[1];
                holder.resultat.setText(mContext.getString(R.string.victoireContreV2, villeAdverse));
                holder.resultat.setTextColor(ContextCompat.getColor(mContext, R.color.limegreen));
            }
            else {
                villeAdverse = infos[0];
                holder.resultat.setText(mContext.getString(R.string.defaiteContreV2, villeAdverse));
                holder.resultat.setTextColor(ContextCompat.getColor(mContext, R.color.firebrick));
            }
        }

        //pour la date
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(infos[2]);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String minute;
            if(cal.get(Calendar.MINUTE) == 0)
            {
                minute = "";
            }
            else if(cal.get(Calendar.MINUTE) < 10)
            {
                minute = "0" + cal.get(Calendar.MINUTE);
            }
            else
            {
                minute = "" + cal.get(Calendar.MINUTE);
            }

            holder.date.setText(mContext.getString(R.string.creeLe,
                                                            cal.get(Calendar.DAY_OF_MONTH),
                                                            (cal.get(Calendar.MONTH) + 1),
                                                            cal.get(Calendar.YEAR),
                                                            cal.get(Calendar.HOUR_OF_DAY),
                                                            minute));
        } catch (ParseException e) {
            holder.date.setText("");
        }

        //pour les points gagnés
        int score = Integer.parseInt(infos[3]);

        if(score < 0){
            holder.score.setText(mContext.getString(R.string.perteDefi, Math.abs(score)));
        }
        else {
            holder.score.setText(mContext.getString(R.string.gainDefi, score));
        }
    }

    @Override
    public int getItemCount() {
        return villeList.size();
    }
}
