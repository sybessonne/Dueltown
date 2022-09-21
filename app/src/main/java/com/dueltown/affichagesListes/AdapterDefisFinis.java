package com.dueltown.affichagesListes;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.dueltown.R;

public class AdapterDefisFinis extends RecyclerView.Adapter<AdapterDefisFinis.MyViewHolder> {

    private List defisList;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView vicOrDef;
        public TextView nomVille;
        public TextView date;

        public MyViewHolder(View view) {
            super(view);
            vicOrDef = view.findViewById(R.id.vicOrDef);
            nomVille = view.findViewById(R.id.nomVille);
            date = view.findViewById(R.id.date);
        }
    }

    public AdapterDefisFinis(List defisList, Context c) {
        this.mContext = c;
        this.defisList = defisList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_defis_finis, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String nomVilleAdverse [] = defisList.get(position).toString().split("/");

        holder.date.setText(nomVilleAdverse[2]);

        //affiche victoire ou defaite et le nom de la ville adverse ou egalite si il y a eu egalite
        if(nomVilleAdverse[3].equals("egalite"))
        {
            holder.vicOrDef.setText(R.string.egaliteContre);
            holder.vicOrDef.setTextColor(ContextCompat.getColor(mContext, R.color.black));
        }
        else if (nomVilleAdverse[0].equals(nomVilleAdverse[3])) {
            holder.vicOrDef.setText(R.string.victoireContre);
            holder.vicOrDef.setTextColor(ContextCompat.getColor(mContext, R.color.limegreen));
        }
        else {
            holder.vicOrDef.setText(R.string.defaiteContre);
            holder.vicOrDef.setTextColor(ContextCompat.getColor(mContext, R.color.firebrick));
        }

        holder.nomVille.setText(nomVilleAdverse[1]);

        //converti les dates et les soustrait pour montrer le temps qu'il reste pour faire le défi
        Date now = new Date();
        SimpleDateFormat dateActuelle = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateNow = dateActuelle.format(now);

        Date d1 = new Date();
        try {
            d1 = dateActuelle.parse(dateNow);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date d2 = new Date();
        try {
            d2 = dateActuelle.parse(String.valueOf(nomVilleAdverse[2]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Long duree = (d1.getTime() - d2.getTime())/(1000*60);
        int minTemp = ((duree%60) == 60) ? 59 : (int) (duree % 60);
        int heureTemp = (int) (duree/60);
        if(heureTemp == 0){
            holder.date.setText(mContext.getString(R.string.dureDepuisV1, minTemp));
        }
        else {
            holder.date.setText(mContext.getString(R.string.dureDepuisV2, heureTemp, minTemp));
        }
    }

    @Override
    public int getItemCount() {
        return defisList.size();
    }
}
