package com.dueltown.affichagesListes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dueltown.R;

import java.util.List;

public class AdapterListePaysSpinner extends ArrayAdapter<String>{

    private final LayoutInflater mInflater;
    private final List<String> items;
    private final int mResource;

    public AdapterListePaysSpinner(Context context, int resource, List objects) {
        super(context, resource, 0, objects);

        mInflater = LayoutInflater.from(context);
        mResource = resource;
        items = objects;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView offTypeTv = view.findViewById(R.id.nomPays);
        offTypeTv.setText(items.get(position));

        return view;
    }
}