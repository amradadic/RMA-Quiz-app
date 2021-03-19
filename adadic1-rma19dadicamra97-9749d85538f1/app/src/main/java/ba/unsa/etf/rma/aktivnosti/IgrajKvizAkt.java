package ba.unsa.etf.rma.aktivnosti;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.AlarmClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.fragmenti.InformacijeFrag;
import ba.unsa.etf.rma.fragmenti.PitanjeFrag;
import ba.unsa.etf.rma.fragmenti.RangLista;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class IgrajKvizAkt extends AppCompatActivity implements PitanjeFrag.OnItemClick {
    Kviz k =null;
    public static String imeKvizaKojegPrimamo; //da
    public static int brojTacnihOdgovora = 0; //da
    public static int brojPreostalih = 0; // da
    public static double procenatTacnih = 0; //da
    public static boolean jelgotovK = false;
    protected TextView nazivIgraca;


    public static int brojacPitanja = 0;

    public static final String ACTION_SET_TIMER = "";
    public static final String ACTION_SHOW_ALARMS = "";


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_igraj_kviz_akt);

        k= (Kviz) getIntent().getSerializableExtra("trenutni");
        //System.out.println(k.getNaziv());


        imeKvizaKojegPrimamo = k.getNaziv();
       ArrayList<Pitanje> lisaaaaa= k.getPitanja();

                 /*      DIO ZA ALARM        */
        int brojPitanja = k.getPitanja().size();
        int minute = 0;
        int sati = 0;
        int sekunde = 0;
        if (brojPitanja % 2 == 1)
            minute = brojPitanja / 2 + 1;
        else
            minute = brojPitanja / 2;
        Date time = new Date();
        sekunde = time.getSeconds();
        while(sekunde > 59) {
            minute++;
            sekunde -= 60;
        }
        while (minute > 59 && minute % 60 != 0) {
            sati++;
            minute -= 60;
        }
        int satiAlarma = time.getHours()+sati;
        int minuteAlarma = time.getMinutes()+minute;
        int sekundeAlarma = time.getSeconds() + sekunde; ////////ALARM JE NA CIJELI BROJ
        Intent intentAlarm = new Intent (AlarmClock.ACTION_SET_ALARM);
        intentAlarm.putExtra(AlarmClock.EXTRA_HOUR, satiAlarma);
        intentAlarm.putExtra(AlarmClock.EXTRA_MINUTES, minuteAlarma);
       //ne mogu se poslati sekunde tajmer je precizniji po ovom pitanju !
        intentAlarm.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        //intentAlarm.putExtra(AlarmClock.)
        this.startActivity(intentAlarm);



        Collections.shuffle(lisaaaaa);
        k.setPitanja(lisaaaaa);


        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        InformacijeFrag infF = new InformacijeFrag();
        PitanjeFrag pitF = new PitanjeFrag();



       fragmentTransaction.add(R.id.informacijePlace, infF);
       fragmentTransaction.add(R.id.pitanjePlace, pitF);

       Bundle b = new Bundle();
       b.putSerializable("trenkviz",k );



       infF.setArguments(b);
       pitF.setArguments(b);





       fragmentTransaction.commit();






      // finish();

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                super.onActivityResult(requestCode, resultCode, data);



            }
        }

    }

    @Override
    public void onItemClicked(int pos) {


        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {

                Bundle b = new Bundle();
                boolean jelgotovo  = false;

                jelgotovo =  b.getBoolean("krajKviza");
                System.out.println(jelgotovo);



                if(brojacPitanja != k.getPitanja().size()) {

                    k = (Kviz) getIntent().getSerializableExtra("trenutni");

                    System.out.println("jel gotov" + jelgotovK);


                    Configuration config = getResources().getConfiguration();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


                    b.putSerializable("trenkviz", k);
                    InformacijeFrag infF = new InformacijeFrag();
                    PitanjeFrag pitF = new PitanjeFrag();

                    RangLista rangLista = new RangLista();

                    fragmentTransaction.replace(R.id.informacijePlace, infF);
                    fragmentTransaction.replace(R.id.pitanjePlace, pitF);

                    infF.setArguments(b);
                    pitF.setArguments(b);
                    brojacPitanja++;
                    brojPreostalih = k.getPitanja().size() - brojacPitanja;
                    procenatTacnih = (double) brojTacnihOdgovora / brojacPitanja;
                    fragmentTransaction.commit();

                    if (brojPreostalih == 0) {
                        LayoutInflater li = LayoutInflater.from(IgrajKvizAkt.this);
                        View view = li.inflate(R.layout.alert_ranglista, null);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(IgrajKvizAkt.this).setView(view);
                        nazivIgraca = view.findViewById(R.id.etNaziv); //textview
                        alertDialog.setMessage("Vaše ime: ").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ime = String.valueOf(nazivIgraca.getText().toString());
                                new DajRangListe(new AsyncResponse() {
                                    @Override
                                    public void processFinish(String output) {
                                        if (RangLista.lista != null && RangLista.lista.size() != 0)
                                            new UpdejtRangListe(new AsyncResponse() {
                                                @Override
                                                public void processFinish(String output) {
                                                }
                                            }).execute(String.valueOf(nazivIgraca.getText().toString().trim()));
                                        else
                                            new StaviRLnaBazu(new AsyncResponse() {
                                                @Override
                                                public void processFinish(String output) {
                                                }
                                            }).execute(String.valueOf(nazivIgraca.getText().toString().trim()));
                                        new DajRangListe(new AsyncResponse() {
                                            @Override
                                            public void processFinish(String output) {
                                                FragmentManager fm = getSupportFragmentManager();
                                                FragmentTransaction ft = fm.beginTransaction();
                                                RangLista rangLista = new RangLista();
                                                ft.replace(R.id.pitanjePlace, rangLista).commit();
                                            }
                                        }).execute("proba");
                                    }
                                }).execute("operacije rangliste");
                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        AlertDialog al = alertDialog.create();
                        al.show();
                    }


                }

            }
        }, 2000);


    }


    public class DajRangListe extends AsyncTask<String, Void, String> {
        AsyncResponse delegate = null;

        public DajRangListe(AsyncResponse delegate) { this.delegate = delegate; }

        @Override
        protected String doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();

                String urll ="https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Rangliste?access_token=";
                URL url = new URL(urll + URLEncoder.encode(TOKEN, "utf-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "Bearer"+TOKEN);
                InputStream ins = connection.getInputStream();


                String rezultatRanglista = convertStreamToString(ins);
                Log.d("rang lista", rezultatRanglista);
                JSONObject jo = new JSONObject(rezultatRanglista);
                String naziv;
                JSONObject fields;
                JSONArray joRanglista = null;
                RangLista.lista.clear();
                if (jo.has("documents")) {
                    joRanglista = jo.getJSONArray("documents");
                    for (int i = 0; i < joRanglista.length(); i++) {
                        JSONObject jedanKviz = joRanglista.getJSONObject(i);
                        if (jedanKviz.has("fields")) {
                            fields = jedanKviz.getJSONObject("fields");
                            JSONObject nazivKvizaObjekat;
                            if (fields.has("nazivKviza")) {
                                nazivKvizaObjekat = fields.getJSONObject("nazivKviza");
                                String nazivKviza = nazivKvizaObjekat.getString("stringValue");
                                if (nazivKviza.equals(k.getNaziv())) {
                                    JSONObject lista;
                                    if (fields.has("lista")) {
                                        lista = fields.getJSONObject("lista");
                                        JSONObject mapValue;
                                        if (lista.has("mapValue")) {
                                            mapValue = lista.getJSONObject("mapValue");
                                            JSONObject fields2;
                                            if (mapValue.has("fields")) {
                                                fields2 = mapValue.getJSONObject("fields");
                                                int j = 1;
                                                for (; ; ) {
                                                    String elementListe = "";
                                                    if (fields2.has(String.valueOf(j))) { // uzimamo prvu poziciju u kvizu
                                                        elementListe = String.valueOf(j) + ". ";
                                                        JSONObject kljuc = fields2.getJSONObject(String.valueOf(j));
                                                        if (kljuc.has("mapValue")) {
                                                            JSONObject mapValueVrijednost = kljuc.getJSONObject("mapValue");
                                                            if (mapValueVrijednost.has("fields")) {
                                                                JSONObject fields3 = mapValueVrijednost.getJSONObject("fields");
                                                                String imeIgraca = fields3.names().toString();
                                                                imeIgraca = imeIgraca.replace("[", "");
                                                                imeIgraca = imeIgraca.replace("\"", "");
                                                                imeIgraca = imeIgraca.replace("]", "");
                                                                elementListe += imeIgraca + ": ";
                                                                JSONObject vrijednostObjekat = fields3.getJSONObject(imeIgraca);
                                                                double procenat = vrijednostObjekat.getDouble("doubleValue");
                                                                elementListe += procenat + "%";
                                                            }
                                                        }
                                                    } else break;
                                                    RangLista.lista.add(elementListe);
                                                    ++j;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute (String res) {
            delegate.processFinish(res);
        }
    }

    public static String convertStreamToString(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (IOException e) {}
        finally {
            try {
                in.close();
            }
            catch (IOException e) {}
        }
        return sb.toString();
    }

    public class UpdejtRangListe extends AsyncTask<String, Void, String> {
        AsyncResponse delegate = null;

        public UpdejtRangListe(AsyncResponse delegate) { this.delegate = delegate; }

        @Override
        protected String doInBackground(String... strings) {
            GoogleCredential credentials;
            try {
                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.<String>newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                String urll ="https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Rangliste/" + URLEncoder.encode(k.getNaziv().trim(), "utf-8") + "?currentDocument.exists=true&access_token=";

                URL url = new URL(urll + URLEncoder.encode(TOKEN, "utf-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                String dokument = "{ \"fields\": { \"nazivKviza\": {\"stringValue\":\"" + k.getNaziv() + "\"}, " +
                        "\"lista\": {\"mapValue\": {\"fields\": {\"";
                //dohvatamo podatke za prikaz
                for (int i = 0; i < RangLista.lista.size(); i++) {
                    String igrac =  RangLista.lista.get(i);
                    dokument += igrac.charAt(0) + "\": {\"mapValue\": {\"fields\": {\"";
                    int j;
                    for (j = 3; j < igrac.length(); j++)  // broj.*space*_ (naziv igrača počinje na crtici, 3. karakter)
                        if (igrac.charAt(j) == ':') break;
                    dokument += igrac.substring(3, j) +  "\": {\"doubleValue\": ";  //naziv igrača
                    j += 2;
                    Double procenat = Double.valueOf(igrac.substring(j, igrac.length()-2));
                    dokument += procenat + "}}}}, \"";
                }
                dokument += String.valueOf(RangLista.lista.size()+1) + "\": {\"mapValue\": {\"fields\": {\"" + strings[0] + "\": {\"doubleValue\": " + procenatTacnih + "}}}}";
                dokument += "}}}}}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                int code = connection.getResponseCode();
                InputStream odgovor = connection.getInputStream();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(odgovor, StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        public void onPostExecute(String res) {
            delegate.processFinish(res);
        }
    }

    public class StaviRLnaBazu extends AsyncTask<String, Void, String> {
        AsyncResponse delegate = null;

        public StaviRLnaBazu(AsyncResponse delegate) { this.delegate = delegate; }

        @Override
        protected String doInBackground(String... strings) {
            GoogleCredential credentials;
            try {
                InputStream tajnaStream = getResources().openRawResource(R.raw.secret);
                credentials = GoogleCredential.fromStream(tajnaStream).createScoped(Lists.<String>newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();

                String urll ="https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Rangliste?documentId=" + URLEncoder.encode(k.getNaziv().trim(), "utf-8") +"&access_token=";
                URL url = new URL(urll + URLEncoder.encode(TOKEN, "utf-8"));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                String dokument = "{ \"fields\": { \"nazivKviza\": {\"stringValue\":\"" + k.getNaziv() + "\"}, " +
                        "\"lista\": {\"mapValue\": {\"fields\": {\"1\": {\"mapValue\": {\"fields\": {\"" + strings[0] + "\": {\"doubleValue\": " + procenatTacnih + "}}}}";
                dokument += "}}}}}";
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = dokument.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                int code = connection.getResponseCode(); //response kod
                InputStream odgovor = connection.getInputStream();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(odgovor, StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null)
                        response.append(responseLine.trim());
                    Log.d("ODGOVOR", response.toString());
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public void onPostExecute(String res) {
            delegate.processFinish(res);
        }
    }



}
