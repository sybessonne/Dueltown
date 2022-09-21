package com.dueltown.affichagesListes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.dueltown.R;

public class AdapterListeVilleAutoCompleteTextView extends ArrayAdapter<String> {

    private int itemLayout;

    public AdapterListeVilleAutoCompleteTextView(Context context, int resource, List<String> listeVille) {
        super(context, resource, listeVille);
        itemLayout = resource;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);

        TextView nomVille = view.findViewById(R.id.nomVille);
        nomVille.setText(getItem(position));
        return view;
    }
}
