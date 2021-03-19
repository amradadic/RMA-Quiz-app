package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class DodajPitanjeAkt extends AppCompatActivity {
    EditText textPitanja;
    EditText textOdgovora;
    ListView odgovori;
    Button btnDodaj;
    Button btnDodajTacan;
    Button btnSacuvajPitanje;

    ArrayList<String> listaOdgovora = new ArrayList<>();
    String tacanOdgovor;

    ArrayAdapter<String> adapterOdgovora;



    String imeKviza;
    private int pozicijaTacnog;
    Pitanje zaPoslati;
    boolean pitanjePostoji = false;


    public class PitanjeP extends AsyncTask<String,Integer,Void> {

        @Override
        protected Void doInBackground(String... strings) {
            try {
                InputStream is = getResources().openRawResource(R.raw.secret);
                GoogleCredential credentials = GoogleCredential.fromStream(is).
                        createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();

                URL urlObj;
                HttpURLConnection conn;

                try{
                    String urlDohvatanje = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Pitanja/" + URLEncoder.encode(zaPoslati.getNaziv()) + "?access_token=";
                    urlObj = new URL(urlDohvatanje + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    //pitanje postoji u bazi treba azurirati moguca pitanja
                    pitanjePostoji = true;

                }
                catch(FileNotFoundException e) {
                    String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Pitanja?documentId=" + URLEncoder.encode(zaPoslati.getNaziv()) + "&access_token=";
                    urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    int indexTacnog = zaPoslati.getOdgovori().indexOf(zaPoslati.getTacan());

                    String odgovori = "";
                    for (int i = 0; i < zaPoslati.getOdgovori().size(); i++) {
                        odgovori += "{\"stringValue\": \"" + zaPoslati.getOdgovori().get(i) + "\"}";
                        if (i != zaPoslati.getOdgovori().size() - 1) odgovori += ",";
                    }
                    String dokument = "{ \"fields\": { \"naziv\": {\"stringValue\": \"" + zaPoslati.getNaziv() + "\"}, \"indexTacnog\": {\"integerValue\": \"" + indexTacnog + "\"}, \"odgovori\": {\"arrayValue\": {\"values\": [" + odgovori + "]}}}}";
                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                    int code = conn.getResponseCode(); //response code
                    InputStream odgovor = conn.getInputStream();
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(odgovor, "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        Log.d("TOKEN", response.toString()); //umjesto token je odgovor kod njih
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            if(pitanjePostoji)new AlertDialog.Builder(DodajPitanjeAkt.this)
                    .setTitle("Dodavanje pitanja")
                    .setMessage("Uneseno pitanje već postoji!")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            else{
                Intent intent = new Intent(DodajPitanjeAkt.this, DodajKvizAkt.class);
                intent.putExtra("pitanje", zaPoslati);

                setResult(1);
                setResult(RESULT_OK, intent);
                finish();

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_pitanje_akt);
        //new PitanjeP().execute("proba");

        textPitanja = (EditText) findViewById(R.id.etNaziv);
        textOdgovora = (EditText) findViewById(R.id.etOdgovor);
        odgovori = (ListView) findViewById(R.id.lvOdgovori);
        btnDodaj = (Button) findViewById(R.id.btnDodajOdgovor);
        btnDodajTacan = (Button) findViewById(R.id.btnDodajTacan);
        btnSacuvajPitanje = (Button) findViewById(R.id.btnDodajPitanje);


        adapterOdgovora = new ArrayAdapter<String>(this, R.layout.element_liste, R.id.Itemname, listaOdgovora) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                if (position == pozicijaTacnog)
                    v.setBackgroundColor(Color.parseColor("#00e676")); //zeleno
                return v;
            }
        };
        odgovori.setAdapter(adapterOdgovora);


        //TEKST PITANJA I NAZIV PITANJA SU ISTA STVAR


        btnDodaj.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                textOdgovora.setBackgroundColor(Color.parseColor("#FFFFFF"));

                String odgZaDodati;
                odgZaDodati = textOdgovora.getText().toString();
                if (odgZaDodati.length() > 0) {
                    listaOdgovora.add(odgZaDodati);
                    adapterOdgovora.notifyDataSetChanged();
                    textOdgovora.setText("");
                } else {
                    textOdgovora.setBackgroundColor(Color.parseColor("#FF3232")); //ne toliko drecava crvena
                }
            }
        });
        btnDodajTacan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean tacanIspravan = false;
                btnDodajTacan.setClickable(false); ///da se samo jednom moze dodati tacan odgovor
                textOdgovora.setBackgroundColor(Color.parseColor("#FFFFFF")); //bijela


                String odgZaDodati;

                odgZaDodati = textOdgovora.getText().toString();

                tacanOdgovor = odgZaDodati;

                if (odgZaDodati.length() > 0) {
                    tacanIspravan = true;
                    listaOdgovora.add(odgZaDodati);
                    adapterOdgovora.notifyDataSetChanged();
                    textOdgovora.setText("");
                } else {
                    textOdgovora.setBackgroundColor(Color.parseColor("#FF3232"));
                }
                if (tacanIspravan) {
                    pozicijaTacnog = listaOdgovora.size() - 1;
                    odgovori.setAdapter(adapterOdgovora);

                }


            }
        });

        btnSacuvajPitanje.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(imaLiInternetaBiloKakvogMetoda()) {
                    if (tacanOdgovor != null && textPitanja.getText().toString() != null && tacanOdgovor.length() > 0) {
                        zaPoslati = new Pitanje(textPitanja.getText().toString(), textPitanja.getText().toString(), tacanOdgovor, listaOdgovora);
                        new PitanjeP().execute();
                    }
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                    toast.show();
                }


            }
        });


    }
    private boolean imaLiInternetaBiloKakvogMetoda() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
