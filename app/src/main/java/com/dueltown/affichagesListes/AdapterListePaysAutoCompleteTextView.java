package com.dueltown.affichagesListes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dueltown.R;

import java.util.List;

public class AdapterListePaysAutoCompleteTextView extends ArrayAdapter<String> {

    private int itemLayout;

    public AdapterListePaysAutoCompleteTextView(Context context, int resource, List<String> listePays) {
        super(context, resource, listePays);
        itemLayout = resource;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);

        TextView nomVille = view.findViewById(R.id.nomPays);
        nomVille.setText(getItem(position));
        return view;
    }
}
