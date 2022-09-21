package com.dueltown.parametres;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dueltown.R;

public class aPropos extends AppCompatActivity {

    TextView version = null;
    TextView explications = null;
    TextView site = null;

    Button contact = null;
    Button aide = null;
    Button conditionsGenerales = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_propos);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        version = findViewById(R.id.version);
        site = findViewById(R.id.site);
        explications = findViewById(R.id.explications);
        contact = findViewById(R.id.contact);
        aide = findViewById(R.id.aide);
        conditionsGenerales = findViewById(R.id.conditionsGenerales);

        // afficher fleche de retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //recuperation de la version de l'application
        String versionNo = "";
        PackageInfo pInfo = null;
        try{
            pInfo = getPackageManager().getPackageInfo("com.dueltown", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            pInfo = null;
        }
        if(pInfo != null) {
            versionNo = pInfo.versionName;
        }

        version.setText(getString(R.string.version, versionNo, getString(R.string.app_name)));

        site.setOnClickListener(siteClick);
        contact.setOnClickListener(contactClick);
        aide.setOnClickListener(aideClick);
        conditionsGenerales.setOnClickListener(CGUClick);
    }

    //pour l'appuie sur la touche de retour
    public boolean onOptionsItemSelected(MenuItem item){
        retour();
        return true;
    }

    @Override
    public void onBackPressed() {
        retour();
    }
    private void retour() {
        this.finish();
    }

    private View.OnClickListener siteClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(getString(R.string.URL_SITE)));
            startActivity(i);
        }
    };

    private View.OnClickListener contactClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(aPropos.this, contact.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener aideClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(aPropos.this, aide.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener CGUClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(aPropos.this, CGU.class);
            startActivity(intent);
        }
    };
}