package com.dueltown.affichagesListes;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dueltown.R;

import java.util.List;

public class AdapterClassementSemaine extends RecyclerView.Adapter<AdapterClassementSemaine.MyViewHolder> {

    private List villeList;
    private String pseudo;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView classement;
        public TextView nomJoueur;
        public TextView ville;
        public TextView points;
        public TextView moyenne;

        public MyViewHolder(View view) {
            super(view);
            classement = view.findViewById(R.id.classement);
            nomJoueur = view.findViewById(R.id.nomJoueur);
            points = view.findViewById(R.id.points);
            ville = view.findViewById(R.id.ville);
            moyenne = view.findViewById(R.id.moyenne);
        }
    }

    public AdapterClassementSemaine(List villeList, String pseudo) {
        this.villeList = villeList;
        this.pseudo = pseudo;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ligne_classement_semaine, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String infos[] = villeList.get(position).toString().split("/");
        holder.classement.setText(infos[0]);

        //souligne le pseudo du joueur s'il est dans la liste
        if(infos[1].equals(pseudo))
        {
            holder.nomJoueur.setPaintFlags(holder.nomJoueur.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
        //sinon on met par defaut car sinon il y a des bugs
        else
        {
            holder.nomJoueur.setPaintFlags(0);
        }

        holder.nomJoueur.setText(infos[1]);
        holder.ville.setText(infos[2]);
        holder.points.setText(infos[3]);
        holder.moyenne.setText(infos[4] + "/5");
    }

    @Override
    public int getItemCount() {
        return villeList.size();
    }
}
