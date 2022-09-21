package com.dueltown.affichagesListes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.dueltown.R;

public class AdapterClassementVilles extends RecyclerView.Adapter<AdapterClassementVilles.MyViewHolder> {

    private List villeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView classement;
        public TextView nomVille;
        public TextView victoires;
        public TextView defaites;

        public MyViewHolder(View view) {
            super(view);
            classement = view.findViewById(R.id.classement);
            nomVille = view.findViewById(R.id.nomVille);
            victoires = view.findViewById(R.id.victoires);
            defaites = view.findViewById(R.id.defaites);
        }
    }

    public AdapterClassementVilles(List villeList) {
        this.villeList = villeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_classement_ville, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String infos[] = villeList.get(position).toString().split("/");
        holder.classement.setText(infos[0]);
        holder.nomVille.setText(infos[1]);
        holder.victoires.setText(infos[2]);
        holder.defaites.setText(infos[3]);
    }

    @Override
    public int getItemCount() {
        return villeList.size();
    }
}
