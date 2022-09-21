package com.dueltown.affichagesListes;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import com.dueltown.R;

public class AdapterClassementJoueursVille extends RecyclerView.Adapter<AdapterClassementJoueursVille.MyViewHolder> {

    private List villeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView classement;
        public TextView nomJoueur;
        public TextView points;

        public MyViewHolder(View view) {
            super(view);
            classement = view.findViewById(R.id.classement);
            nomJoueur = view.findViewById(R.id.nomJoueur);
            points = view.findViewById(R.id.points);
        }
    }

    public AdapterClassementJoueursVille(List villeList) {
        this.villeList = villeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_classement_joueurs_ville, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String infos[] = villeList.get(position).toString().split("/");
        holder.classement.setText(infos[0]);
        holder.nomJoueur.setText(infos[1]);
        holder.points.setText(infos[2]);
    }

    @Override
    public int getItemCount() {
        return villeList.size();
    }
}
