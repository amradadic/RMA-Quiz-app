package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

import org.json.JSONException;

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

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;


public class DodajKategorijuAkt extends AppCompatActivity implements IconDialog.Callback{
    private Button dugmeDodajKategoriju;
    private Button dugmeDodajIkonu;
    private EditText imeKategrije;
    private EditText imeIkone;
    private Icon[] selectedIcons;

    public class KategorijeD extends AsyncTask<String,Integer,Void> {

        private boolean trebaAlert = false;

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
                    String urlPostoji = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kategorije/" + URLEncoder.encode(strings[0]) + "?access_token=";
                    urlObj = new URL(urlPostoji + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    InputStream in = new BufferedInputStream(conn.getInputStream());

                    trebaAlert = true;
                }
                catch(FileNotFoundException e){
                    //znaci ne postoji kategorija i treba se dodati
                    String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kategorije?documentId=" + URLEncoder.encode(strings[0]) + "&access_token=";
                    urlObj = new URL(url+ URLEncoder.encode(TOKEN,"UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    String dokument = "{ \"fields\": { \"naziv\": {\"stringValue\": \"" + strings[0] + "\"}, \"idIkonice\": {\"integerValue\": \"" + strings[1] + "\"}}}";
                    try (OutputStream os = conn.getOutputStream()){
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }
                    int code = conn.getResponseCode(); //response code
                    InputStream odgovor = conn.getInputStream();
                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(odgovor, "utf-8"))){
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while((responseLine = br.readLine()) != null){
                            response.append(responseLine.trim());
                        }
                        Log.d( "TOKEN", response.toString()); //umjesto token je odgovor kod njih
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
            if(trebaAlert)new AlertDialog.Builder(DodajKategorijuAkt.this)
                    .setTitle("Dodavanje kategorije")
                    .setMessage("Unesena kategorija već postoji!")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            else{
                Intent intent = new Intent(DodajKategorijuAkt.this, DodajKvizAkt.class);
                String nazivKategorije = imeKategrije.getText().toString();
                String nazivId_Slike = imeIkone.getText().toString();
                Kategorija kat = new Kategorija(nazivKategorije, nazivId_Slike);
                intent.putExtra("dodanaKategorija", kat);


                setResult(2);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kategoriju_akt);

        //new KategorijeD().execute("proba");

        final IconDialog iconDialog = new IconDialog();

        dugmeDodajKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);
        dugmeDodajIkonu = (Button) findViewById(R.id.btnDodajIkonu);
        imeKategrije = (EditText) findViewById(R.id.etNaziv);
        imeIkone = (EditText) findViewById(R.id.etIkona);


        imeIkone.setEnabled(false);

        dugmeDodajIkonu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(selectedIcons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
            }
        });

        dugmeDodajKategoriju.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new KategorijeD().execute(imeKategrije.getText().toString(), imeIkone.getText().toString());

                /*Intent intent = new Intent(DodajKategorijuAkt.this, DodajKvizAkt.class);
                String nazivKategorije = imeKategrije.getText().toString();
                String nazivId_Slike = imeIkone.getText().toString();
                Kategorija kat = new Kategorija(nazivKategorije, nazivId_Slike);
                intent.putExtra("dodanaKategorija", kat);


                setResult(2);
                setResult(RESULT_OK, intent);
                finish();*/

            }
        });

        imeKategrije.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imeIkone.setText("");
            }
        });

    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        selectedIcons = icons;
        imeIkone.setText(String.valueOf(selectedIcons[0].getId()));

    }
}
