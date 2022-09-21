package com.dueltown.affichagesListes;

import android.content.Context;
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

public class AdapterDefisPasFinis extends RecyclerView.Adapter<AdapterDefisPasFinis.MyViewHolder> {

    private List defisList;
    private Context mcontext;
    int dureeDefi = 24; // en heures

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nomVille;
        public TextView date;

        public MyViewHolder(View view) {
            super(view);
            nomVille = view.findViewById(R.id.nomVille);
            date = view.findViewById(R.id.date);
        }
    }

    public AdapterDefisPasFinis(List defisList, Context c) {
        this.mcontext = c;
        this.defisList = defisList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_defis_pas_finis, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String nomVilleAdverse [] = defisList.get(position).toString().split("/");
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
        int minTemp = ((duree%60) == 0) ? 1 : (int) (duree % 60);
        int heureTemp = (int) (duree/60);

        if(heureTemp == dureeDefi - 1) {
            holder.date.setText(mcontext.getString(R.string.tempsRestantV1, (60 - minTemp)));
        }
        else {
            holder.date.setText(mcontext.getString(R.string.tempsRestantV2, (dureeDefi - 1 - heureTemp), (60 - minTemp)));
        }
    }

    @Override
    public int getItemCount() {
        return defisList.size();
    }
}
