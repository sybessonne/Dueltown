package com.dueltown.affichagesListes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.dueltown.R;

public class AdapterClassementJoueursFrance extends RecyclerView.Adapter<AdapterClassementJoueursFrance.MyViewHolder> {

    private List villeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView classement;
        public TextView nomJoueur;
        public TextView ville;
        public TextView points;

        public MyViewHolder(View view) {
            super(view);
            classement = view.findViewById(R.id.classement);
            nomJoueur = view.findViewById(R.id.nomJoueur);
            points = view.findViewById(R.id.points);
            ville = view.findViewById(R.id.ville);
        }
    }

    public AdapterClassementJoueursFrance(List villeList) {
        this.villeList = villeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_classement_joueurs_france, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String infos[] = villeList.get(position).toString().split("/");
        holder.classement.setText(infos[0]);
        holder.nomJoueur.setText(infos[1]);
        holder.ville.setText(infos[2]);
        holder.points.setText(infos[3]);
    }

    @Override
    public int getItemCount() {
        return villeList.size();
    }
}
