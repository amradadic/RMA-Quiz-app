package ba.unsa.etf.rma.aktivnosti;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

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
import ba.unsa.etf.rma.adapteri.KategorijaAdapter;
import ba.unsa.etf.rma.adapteri.PitanjeAdapter;
import ba.unsa.etf.rma.adapteri.PitanjeAdapterPlus;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.servisi.DajMogucaPitanjaIzBaze;
import ba.unsa.etf.rma.servisi.DajSveIzBaze;
import ba.unsa.etf.rma.servisi.RisiverInformacijaSaInterneta;

public class DodajKvizAkt extends AppCompatActivity implements RisiverInformacijaSaInterneta.Receiver
{
    private static final String TAG = DodajKvizAkt.class.getName();
    private Spinner spinner;
    private ListView listaPitanjaUKvizu;
    private ListView listaMogucihPitanja;
    private EditText nazivKvizaText;
    private Button dugmeDodajKviz;
    private Button importujKviz;
    Kviz slanjekviza;
    Intent intentDodajKviz;

    ArrayList<Pitanje> listaPitanja = new ArrayList<>(); /// sva pitanja koja se nalaze u kvizu trenutno
    ArrayList<Pitanje> listaMogucih = new ArrayList<>(); ///moguca pitanja koja nisu dodana trenutnom kvizu
    KategorijaAdapter kategorijaAdapter;

    PitanjeAdapter adapterPitanja;
    //PitanjeAdapter adapterMogucihPitanja;
    PitanjeAdapterPlus adapterMogucihPitanja;

    KvizoviAkt kvizoviAkt ;

    String primljenoImeKviza;

    ArrayList<Kategorija> kategorijeIzKvizoviAkt =  new ArrayList<>() ;
    ArrayList<Kviz> kvizoviIzKvizoviAkt =  new ArrayList<>() ;

    Kviz primljeni = null;

    Kategorija ka = null;

    ArrayList<Pitanje> pitanjaZaDodajKviz = new ArrayList<>();
    Boolean dalDodajemoIliMjenjamo;
    public static final int TEXT_REQUEST = 1;
    public static final int TEXT_REQUEST_2 = 2;
    private static final int READ_REQUEST_CODE = 44;

    // public static final int TEXT_REQUEST_3=3;

    private boolean ima  = false;

    public class DodajKviz extends AsyncTask<String,Integer,Void>{

        private boolean trebaAlert = false;

        @Override
        protected Void doInBackground(String... strings) {
            try {
                InputStream is = getResources().openRawResource(R.raw.secret);
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();

                URL urlObj;
                HttpURLConnection conn;
                String editovanje = strings[0];

                if(editovanje.equals("EDITOVANJE")){
                    try {
                        String urlBrisanje = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kvizovi/" + strings[1] + "?currentDocument.exists=true&access_token=";
                        urlObj = new URL(urlBrisanje + URLEncoder.encode(TOKEN, "UTF-8"));
                        conn = (HttpURLConnection) urlObj.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("DELETE");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept", "application/json");
                        int responseCode = conn.getResponseCode();
                    }
                    catch(Exception e){
                    }
                }

                try{
                    String urlGet = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kvizovi/" + URLEncoder.encode(slanjekviza.getNaziv()) + "?access_token=";
                    urlObj = new URL(urlGet + URLEncoder.encode(TOKEN, "UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    InputStream in = new BufferedInputStream(conn.getInputStream());
                    trebaAlert = true;
                }
                catch (FileNotFoundException e){
                    String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kvizovi?documentId=" + URLEncoder.encode(slanjekviza.getNaziv()) + "&access_token=";
                    urlObj = new URL(url+ URLEncoder.encode(TOKEN,"UTF-8"));
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type","application/json");
                    conn.setRequestProperty("Accept", "application/json");

                    String pitanja = "";
                    for (int i = 0; i < slanjekviza.getPitanja().size(); i++){
                        pitanja += "{\"stringValue\": \"" + slanjekviza.getPitanja().get(i).getNaziv() + "\"}";
                        if(i!=slanjekviza.getPitanja().size()-1) pitanja += ",";
                    }
                    String dokument = "{ \"fields\": { \"naziv\": {\"stringValue\": \"" + slanjekviza.getNaziv() + "\"}, \"idKategorije\": {\"stringValue\": \"" + slanjekviza.getKategorija().getNaziv() + "\"}, \"pitanja\": {\"arrayValue\": {\"values\": [" + pitanja + "]}}}}";

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
            Log.d("nista", "nista");
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            if(trebaAlert)new AlertDialog.Builder(DodajKvizAkt.this)
                    .setTitle("Kviz")
                    .setMessage("Uneseni kviz već postoji!")
                    .setNegativeButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            else{
                //FtesetResult(1);
                setResult(RESULT_OK, intentDodajKviz);
                finish();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_kviz_akt);

        kategorijeIzKvizoviAkt = (ArrayList<Kategorija>) getIntent().getSerializableExtra("kategorije");
        kvizoviIzKvizoviAkt = (ArrayList<Kviz>) getIntent().getSerializableExtra("kvizovi");

        spinner= (Spinner) findViewById(R.id.spKategorije);
        listaPitanjaUKvizu = (ListView) findViewById(R.id.lvDodanaPitanja);
        listaMogucihPitanja = (ListView) findViewById(R.id.lvMogucaPitanja);
        nazivKvizaText = (EditText) findViewById(R.id.etNaziv);
        dugmeDodajKviz = (Button) findViewById(R.id.btnDodajKviz);
        importujKviz = (Button) findViewById(R.id.btnImportKviz);

        Resources res = getResources();

        //adapteri
        // ADAPTER ZA SPINNER
        kategorijaAdapter = new KategorijaAdapter(this,android.R.layout.simple_spinner_dropdown_item, kategorijeIzKvizoviAkt);
        kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(kategorijaAdapter);

        //ADAPTER ZA GORNJU LISTU
        adapterPitanja = new PitanjeAdapter(listaPitanja, this);
        listaPitanjaUKvizu.setAdapter(adapterPitanja);

        //ADAPTER ZA DONJU LISTU
        adapterMogucihPitanja = new PitanjeAdapterPlus(listaMogucih, this);
        listaMogucihPitanja.setAdapter(adapterMogucihPitanja);

        ///postavljanje kategorije
        final Kategorija primljenaKategorija;
        primljenaKategorija = (Kategorija) getIntent().getSerializableExtra("kategor");
        int indeks=0;
        for (Kategorija k : kategorijeIzKvizoviAkt)
        {
            if(k.getNaziv().equals(primljenaKategorija.getNaziv())) break;
            indeks++;
        }
        spinner.setSelection(indeks);

        Kategorija dodajKat = new Kategorija("Dodaj Kategoriju", "1");
        kategorijeIzKvizoviAkt.add(dodajKat);
        kategorijaAdapter.notifyDataSetChanged();

        primljeni = (Kviz) getIntent().getSerializableExtra("jedanKviz");
        System.out.println(listaPitanja.size());

        if(primljeni != null )
        {
            //POSTAVLJANJE TEKSTA U TEKST EDITORU
            nazivKvizaText.setText(primljeni.getNaziv());
            if(primljeni.getNaziv().equals("Dodaj Kviz")){
                nazivKvizaText.setText("");

            }
            adapterPitanja = new PitanjeAdapter(primljeni.getPitanja(), this);
            listaPitanjaUKvizu.setAdapter(adapterPitanja);

        }
        else {
            //POSTAVLJANJE TEKSTA U TEKST EDITORU
            nazivKvizaText.setText("");
            adapterPitanja = new PitanjeAdapter(pitanjaZaDodajKviz, this);
            listaPitanjaUKvizu.setAdapter(adapterPitanja);
        }

        listaPitanjaUKvizu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //NE RADITI SA LISTVIEW NEGO SA ARRAY LISTAMA
                System.out.println(position);
                System.out.println("velicina liste");
                System.out.println(listaPitanja.size());
                for(Pitanje p : listaPitanja){
                    System.out.println(p.getNaziv());
                }

                Pitanje p = listaPitanja.get(position); ///ovdje kad se ucitaju iz baze u suprotnom nece

                listaPitanja.remove(p);
                listaMogucih.add(p);
                adapterMogucihPitanja.notifyDataSetChanged();
                adapterPitanja.notifyDataSetChanged();

            }
        });
        listaMogucihPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //NE RADITI SA LISTVIEW NEGO SA ARRAY LISTAMA
                Pitanje p = listaMogucih.get(position);
                listaMogucih.remove(p);
                listaPitanja.add(p);
                adapterPitanja.notifyDataSetChanged();
                adapterMogucihPitanja.notifyDataSetChanged();

            }
        });



        dugmeDodajKviz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                intentDodajKviz = new Intent();
                //Intent intent  = new Intent(DodajKvizAkt.this, KvizoviAkt.class);
                intentDodajKviz.putExtra("SveKategorije", kategorijeIzKvizoviAkt);

                Kategorija zaSlanje = (Kategorija) spinner.getSelectedItem();
                String imeZaSlanje = nazivKvizaText.getText().toString();
                slanjekviza = new Kviz(imeZaSlanje, listaPitanja, zaSlanje);

                if(imeZaSlanje.length() > 0){
                    if(primljeni != null) {
                        primljenoImeKviza = primljeni.getNaziv();
                    }

                    if(primljeni!= null){
                        dalDodajemoIliMjenjamo=false;
                        //update
                        intentDodajKviz.putExtra("katrgorijaZaSlanje", zaSlanje);
                        intentDodajKviz.putExtra("istinitost", dalDodajemoIliMjenjamo);
                        intentDodajKviz.putExtra("staroIme", primljenoImeKviza);
                        intentDodajKviz.putExtra("vracamKviz", slanjekviza);
                        new DodajKviz().execute("EDITOVANJE", primljenoImeKviza);

                    }
                    else{
                        dalDodajemoIliMjenjamo = true;
                        //dodavanje
                        intentDodajKviz.putExtra("istinitost", dalDodajemoIliMjenjamo);
                        intentDodajKviz.putExtra("staroIme", primljenoImeKviza);
                        intentDodajKviz.putExtra("vracamKviz", slanjekviza);
                        new DodajKviz().execute("DODAVANJE");
                    }

                    /*//FtesetResult(1);
                    setResult(RESULT_OK, intent);
                    finish();*/

                }


            }
        });

        importujKviz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //////////////////////DUGME ZA IMPORT KVIZA

                if (imaLiInternetaBiloKakvogMetoda()){
                    Intent intent = new Intent();

                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);

                intent.addCategory(Intent.CATEGORY_OPENABLE);

                intent.setType("text/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, READ_REQUEST_CODE);
                }
            }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        });



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override


            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(imaLiInternetaBiloKakvogMetoda()) {
                    Kategorija kategorija_kliknuta = (Kategorija) spinner.getSelectedItem();
                    if (kategorija_kliknuta.getNaziv().equals("Dodaj Kategoriju")) {
                        Intent intent = new Intent(DodajKvizAkt.this, DodajKategorijuAkt.class);
                        startActivityForResult(intent, TEXT_REQUEST_2);
                    }
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //dodaje ono Dodaj Pitanje u listu ove 2 linije znaci
        View footerView = getLayoutInflater().inflate(R.layout.listview_footer_dodaj_pitanje, null);
        listaPitanjaUKvizu.addFooterView(footerView);
        footerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imaLiInternetaBiloKakvogMetoda()) {
                    Intent intent = new Intent(DodajKvizAkt.this, DodajPitanjeAkt.class);
                    String imeKviza = nazivKvizaText.getText().toString();

                    intent.putExtra("listap", listaPitanja);
                    intent.putExtra("imekv", imeKviza);
                    startActivityForResult(intent, TEXT_REQUEST);
                }else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        ubaciUMogucaPitanjaSvaIzBazeKojaNisuUDodanim();
    } //kraj onCreate

    private void ubaciUMogucaPitanjaSvaIzBazeKojaNisuUDodanim()
    {
        /*Kategorija sviKategorija = (Kategorija) spinner.getSelectedItem();
        if(sviKategorija.getNaziv().equals("Svi"))
        {
            ucitajSvePodatkeIzBaze();
            return;
        }*/

        Intent mojIntent = new Intent(Intent.ACTION_SYNC, null, this, DajMogucaPitanjaIzBaze.class);
        RisiverInformacijaSaInterneta risiver = new RisiverInformacijaSaInterneta(new Handler());
        risiver.setReceiver(this);

        if(primljeni != null) mojIntent.putExtra("dodanaPitanja", primljeni.getPitanja());
        else mojIntent.putExtra("dodanaPitanja", listaPitanja);

        mojIntent.putExtra("risiver", risiver);
        startService(mojIntent);
    }

    private void ucitajSvePodatkeIzBaze()
    {
        Intent mojIntent = new Intent(Intent.ACTION_SYNC, null, this, DajSveIzBaze.class);
        RisiverInformacijaSaInterneta risiver = new RisiverInformacijaSaInterneta(new Handler());
        risiver.setReceiver(this);
        mojIntent.putExtra("risiver", risiver);
        startService(mojIntent);
    }



    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        switch (resultCode)
        {
            case DajMogucaPitanjaIzBaze.STATUS_RUNNING:
                /* Ovdje ide kod koji obavještava korisnika da je poziv upućen */
                break;
            case DajMogucaPitanjaIzBaze.STATUS_FINISHED:
                /* Dohvatanje rezultata i update UI */
                if(resultData.getSerializable("mogucaPitanja") != null)
                {

                    ArrayList<Pitanje> mogucaPitanjaDobavljenaIzBaze = (ArrayList<Pitanje>) resultData.getSerializable("mogucaPitanja");
                    listaMogucih.clear();
                    listaMogucih.addAll(mogucaPitanjaDobavljenaIzBaze);
                    adapterMogucihPitanja.notifyDataSetChanged();
                }
                else if(resultData.getString("odradjeno") != null &&
                        resultData.get("odradjeno").equals("sve iz baze se vraca"))
                {
                    kategorijeIzKvizoviAkt.clear();
                    kategorijeIzKvizoviAkt.addAll((ArrayList<Kategorija>) resultData.getSerializable("sveKategorije"));

                    kvizoviIzKvizoviAkt.clear();
                    kvizoviIzKvizoviAkt.addAll((ArrayList<Kviz>) resultData.getSerializable("sviKvizovi"));

                    listaPitanja.clear();
                    listaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("svaPitanja"));
                    adapterPitanja.notifyDataSetChanged();

                    for(int i = 0; i < kategorijeIzKvizoviAkt.size(); i++)
                    {
                        if(kategorijeIzKvizoviAkt.get(i).getNaziv().equals("Svi"))
                        {
                            spinner.setSelection(i);
                            break;
                        }
                    }

                    kategorijaAdapter.notifyDataSetChanged();
                    adapterMogucihPitanja.notifyDataSetChanged();
                    adapterPitanja.notifyDataSetChanged();
                }

                break;
            case DajMogucaPitanjaIzBaze.STATUS_ERROR:
                /* Slučaj kada je došlo do greške */
                if(resultData.getString("nisuUcitanaMoguca") != null)
                {
                    String poruka = resultData.getString("nisuUcitanaMoguca");
                    Log.w("UPOZORENJE: ", poruka);
                }
                else if(resultData.getString("nije odradjeno") != null &&
                        resultData.get("nije odradjeno").equals("nema neke od kolekcija u bazi"))
                {
                    if(resultData.getSerializable("sveKategorije") != null)
                    {
                        kategorijeIzKvizoviAkt.clear();
                        kategorijeIzKvizoviAkt.addAll((ArrayList<Kategorija>) resultData.getSerializable("sveKategorije"));
                    }

                    if(resultData.getSerializable("sviKvizovi") != null)
                    {
                        kvizoviIzKvizoviAkt.clear();
                        kvizoviIzKvizoviAkt.addAll((ArrayList<Kviz>) resultData.getSerializable("sviKvizovi"));
                    }

                    if(resultData.getSerializable("svaPitanja") != null)
                    {
                        listaPitanja.clear();
                        listaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("svaPitanja"));
                        adapterPitanja.notifyDataSetChanged();
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if(resultCode==RESULT_OK){

                Pitanje primljenoPitanje;
                adapterPitanja = new PitanjeAdapter(listaPitanja, this);
                listaPitanjaUKvizu.setAdapter(adapterPitanja);

                primljenoPitanje = (Pitanje) data.getSerializableExtra("pitanje");

                listaPitanja.add(primljenoPitanje);
                adapterPitanja.notifyDataSetChanged();
                adapterMogucihPitanja.notifyDataSetChanged();

            }
        }
        else if(requestCode == 2){
            if(resultCode==RESULT_OK){

                Kategorija dodana ;
                dodana = (Kategorija) data.getSerializableExtra("dodanaKategorija");
                kategorijeIzKvizoviAkt.add(dodana);
                kategorijaAdapter.notifyDataSetChanged();
                spinner.setSelection(kategorijeIzKvizoviAkt.size()-1);
                kategorijaAdapter.notifyDataSetChanged();
            }
        }

        ///za dodatno dugmence
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            Uri uri =null;
            Kviz k = null;

            if (data != null)
            {
                uri = data.getData();
                try
                {
                    String dat = pomocnaCVSfunkcija(uri);
                    Kviz kvizzzz = dajKvizIzDatoteke(dat);

                    if(kvizzzz!=null)
                    {
                        nazivKvizaText.setText(kvizzzz.getNaziv());

                        listaPitanja = kvizzzz.getPitanja();

                        adapterPitanja = new PitanjeAdapter(listaPitanja, this);

                        listaPitanjaUKvizu.setAdapter(adapterPitanja);

                        if (!ima)
                        {
                            kategorijeIzKvizoviAkt.add(ka);
                            kategorijaAdapter = new KategorijaAdapter(this, android.R.layout.simple_spinner_dropdown_item, kategorijeIzKvizoviAkt);
                            spinner.setAdapter(kategorijaAdapter);
                            spinner.setSelection(kategorijeIzKvizoviAkt.size() - 1);
                            kategorijaAdapter.notifyDataSetChanged();

                        }
                        else {
                            int i = 0;
                            for (Kategorija kaaaaat : kategorijeIzKvizoviAkt) {
                                if (kaaaaat.getNaziv().equals(kvizzzz.getKategorija().getNaziv()))
                                    break;
                                i++;
                            }
                            spinner.setSelection(i);
                            kategorijaAdapter.notifyDataSetChanged();
                        }
                    }
                    // adapterPitanja.notifyDataSetChanged();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Kviz dajKvizIzDatoteke(String s){
        Kviz zaVracanje = null;
        String naziv = "";
        String imeKat = "";
        int brojP;

        String[] podaci = s.split("\n"); //sad smo splitali po redovima
        ArrayList<Pitanje> listaP  = new ArrayList<>();


        if(podaci.length != 0){
            String[] redNula = podaci[0].split(",");
            naziv = redNula[0];
            imeKat = redNula[1];
            brojP = Integer.parseInt(redNula[2]);   //////treba se postaviti

            if(brojP != podaci.length -1) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz kojeg importujete ima neispravan broj odgovora!");
                alertDialog.show();
                return null;
            }

            ///naziv je ime kviza
            // boolean jelPostoji = false;

            for(Kviz kviz : kvizoviIzKvizoviAkt){
                if(kviz.getNaziv().equals(naziv)){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz vec postoji!");
                    alertDialog.show();
                    return null;
                }
            }

            int i = 1;
            while(i < podaci.length){
                String [] red = podaci[i].split(",");
                Pitanje p = null; /////treba dodat new
                String imePitanja ;
                int brojOdg;
                ArrayList<String> odgovori =new ArrayList<>();
                String tacan = "" ;  ///kad se seta naziv seta se i tekst
                int indeksTacnog;
                if(Integer.parseInt(red[1]) == red.length-3){
                    if(Integer.parseInt(red[2]) < 0 || Integer.parseInt(red[2])>=red.length -3){ ////kako provjeriti da li je broj cijeli broj odat uslov
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz kojeg importujete ima neispravan index tacnog odgovora!");
                        alertDialog.show();

                    }
                    //ako je tacnoooooo
                    imePitanja = red[0];
                    brojOdg = Integer.parseInt(red[1]);
                    try{

                        indeksTacnog = Integer.parseInt(red[2]);

                    }
                    catch (NumberFormatException exception){
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz kojeg importujete ima neispravan broj odgovora!");
                        alertDialog.show();
                        return null;

                    }

                    int j = 3; //idu od 3 odgovori
                    while(j < red.length){
                        if(j - 3 == indeksTacnog ) tacan = red[j]; //postavljamo tekst tacnog odgovora
                        odgovori.add(red[j]);
                        j++;
                    }




                    //sad imamo listu odgovora, kreiramo piranje
                    p = new Pitanje(imePitanja, imePitanja, tacan,odgovori);
                    listaP.add(p);




                }
                else{
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz kojeg importujete ima neispravan index tacnog odgovora!");
                    alertDialog.show();


                }


                i++;
            }
        }
        ka = new Kategorija(imeKat, "0");

        for(Kategorija kkk : kategorijeIzKvizoviAkt){
            if(kkk.getNaziv().equals(ka.getNaziv())){
                ima = true;
                break;
            }
        }

        int i , j;

        for(i = 0; i < listaP.size(); i++){
            for(j = i+1; j<listaP.size(); j++){
                String s1 = listaP.get(i).getNaziv();
                String s2 = listaP.get(j).getNaziv();
                if(s1.equals(s2)){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setMessage("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
                    alertDialog.show();
                    return null;

                }
            }
        }



        zaVracanje = new Kviz(naziv, listaP, ka);

        // kvizoviIzKvizoviAkt.add(zaVracanje);


        return zaVracanje;
    }

    private String pomocnaCVSfunkcija(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line + "\n"); //novi red isto
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    @Override
    public void onBackPressed(){

        Intent intent  = new Intent(DodajKvizAkt.this, KvizoviAkt.class);
        intent.putExtra("SveKategorije", kategorijeIzKvizoviAkt);

        setResult(3);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
        finish();

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