package ba.unsa.etf.rma.aktivnosti;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.apache.http.HttpConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ba.unsa.etf.rma.KalendarContentResolver;
import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.KategorijaAdapter;
import ba.unsa.etf.rma.adapteri.KvizAdapter;
import ba.unsa.etf.rma.fragmenti.DetailFrag;
import ba.unsa.etf.rma.fragmenti.ListaFrag;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;
import ba.unsa.etf.rma.servisi.RisiverInformacijaSaInterneta;
import ba.unsa.etf.rma.servisi.DajSveIzBaze;

public class KvizoviAkt extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ListaFrag.OnItemClick, RisiverInformacijaSaInterneta.Receiver
{
    private Spinner spinner;
    private ListView lista;
    private ArrayList<Kviz> listaKvizova = new ArrayList<>();
    private ArrayList<Kategorija> listaKategorija = new ArrayList<>();
    KvizAdapter kvizAdapter;
    KategorijaAdapter kategorijaAdapter;
    public static Kategorija zaSlanjeUFragment;
    public static ArrayList<Kviz> slanjeKvizovauFragment = new ArrayList<>();
    public static ArrayList<Kategorija> listaKategorijaZaSlanje = new ArrayList<>();
    public static ArrayList<Kviz> sviKvizovi = new ArrayList<>();


    private Boolean kakavJeKviz;
    private String staroIme;

    ArrayList<Pitanje> pitanjaKviza = new ArrayList<>();
    String imeKvizaKojiPrimamo;
    Kategorija primljenaKategorija;
    public static final int TEXT_REQUEST = 1;

    String novoIme;

    private ArrayList<Pitanje> listaPitanja = new ArrayList<>();
    private Kategorija kategorijaSve = new Kategorija("Svi", "0");





     public class Kvizovi extends AsyncTask<String,Integer,Void> {

     @Override
     protected Void doInBackground(String... strings) {

         try {
             InputStream is = getResources().openRawResource(R.raw.secret);
             GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
             credentials.refreshToken();
             String TOKEN = credentials.getAccessToken();
             Kategorija izSpinnera = (Kategorija) spinner.getSelectedItem();
             String query = "{\n" +
                     "\"structuredQuery\": {\n" +
                     "\"where\": {\n" +
                     "\"fieldFilter\": {\n" +
                     "\"field\": {\"fieldPath\": \"idKategorije\"}, \n" +
                     "\"op\": \"EQUAL\",\n" +
                     "\"value\": {\"stringValue\": \"" + izSpinnera.getNaziv() + "\"}\n" +
                     "}\n" +
                     "},\n" +
                     "\"select\": {\"fields\": [ {\"fieldPath\": \"idKategorije\"}, {\"fieldPath\": \"naziv\"}, {\"fieldPath\": \"pitanja\"} ] }, \n" +
                     "\"from\": [{\"collectionId\" : \"Kvizovi\"}], \n" +
                     "\"limit\" : 1000\n" +
                     "}\n" +
                     "}";
             ///rma-spirala-d5105
             String url1 = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents:runQuery?access_token=" + TOKEN;
             try {
                 URL url = new URL(url1);
                 HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setDoInput(true);
                 urlConnection.setRequestMethod("POST");
                 urlConnection.setRequestProperty("Content-Type", "application/json");
                 urlConnection.setRequestProperty("Accept", "application/json");
                 try (OutputStream os = urlConnection.getOutputStream()) {
                     byte[] input = query.getBytes("utf-8");
                     os.write(input, 0, input.length);
                 }

                 int code = urlConnection.getResponseCode();
                 InputStream in = urlConnection.getInputStream();

                 try (BufferedReader br = new BufferedReader(
                         new InputStreamReader(in, "utf-8"))) {
                     StringBuilder response = new StringBuilder();
                     String responseLine = null;
                     while ((responseLine = br.readLine()) != null) {
                         response.append(responseLine.trim());
                     }

                     Log.d("TOKEN", response.toString()); //umjesto token je odgovor kod njih

                 }

             } catch (IOException e) {
                 e.printStackTrace();
             }


         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }

 }



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kvizovi_akt);

        if(!imaLiInternetaBiloKakvogMetoda()){
            Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije, ne može se učitati sa FIREBASE!", Toast.LENGTH_SHORT);
            toast.show();

        }


        spinner = (Spinner) findViewById(R.id.spPostojeceKategorije);
        if (spinner != null)
        {
            lista = (ListView) findViewById(R.id.lvKvizovi);
            Resources res = getResources();

            kvizAdapter = new KvizAdapter(this, listaKvizova);

            kategorijaAdapter = new KategorijaAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaKategorija);
            kategorijaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(kategorijaAdapter);
            spinner.setOnItemSelectedListener(this);

            lista.setAdapter(kvizAdapter);


            napuniSve();

            ////////                      OBICNI KLIK
            lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(KvizoviAkt.this, IgrajKvizAkt.class);



                    if (position >= 0 && position <= listaKvizova.size()){
                        String pattern = "yyyy-MM-dd";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        Date tren = new Date();
                        Map<String,String> mapa = new KalendarContentResolver(KvizoviAkt.this).procitajPodatke();

                        for(Map.Entry<String,String> m : mapa.entrySet()){
                            long vrijeme = Long.parseLong(m.getKey());
                            Date datum = new Date(vrijeme);
                            if(simpleDateFormat.format(tren).equals(simpleDateFormat.format(datum))){
                                int trajanjeKviza = (int) Math.ceil(listaKvizova.get(position).getPitanja().size()/2.);
                                long miliTren = tren.getTime();
                                long miliEvent = datum.getTime();
                                long miliTrajanjeKviza = TimeUnit.MINUTES.toMillis(trajanjeKviza);
                                int minuteDoDogadjaja = (int) (TimeUnit.MILLISECONDS.toMinutes(miliEvent-miliTren)+1);

                                if(miliTren+miliTrajanjeKviza > miliEvent && miliTren < miliEvent){
                                    new AlertDialog.Builder(KvizoviAkt.this).setTitle("POKUSAJ IGRANJA ").setMessage("Imate dogadjaj u kalendaru " +
                                            "za " + minuteDoDogadjaja + " minuta !").setNegativeButton(android.R.string.ok , null).setIcon(android.R.drawable.ic_dialog_alert).show();
                                    return;
                                }


                            }
                        }
                        intent.putExtra("trenutni", listaKvizova.get(position));

                }
                    startActivityForResult(intent, TEXT_REQUEST);
                }

            });


            ///////                      DUGI KLIK
            lista.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    if(imaLiInternetaBiloKakvogMetoda()) {

                        if (listaKvizova != null && position >= 0 && position < listaKvizova.size()) {
                            Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                            staroIme = listaKvizova.get(position).getNaziv();
                            intent.putExtra("jedanKviz", listaKvizova.get(position));
                            ArrayList<Kviz> kvizoviZaSlanje = new ArrayList<>();
                            kvizoviZaSlanje = vratiKvizoveKategorije((Kategorija) spinner.getSelectedItem());
                            Kategorija kateg = (Kategorija) spinner.getSelectedItem();
                            intent.putExtra("kategorije", listaKategorija);
                            intent.putExtra("kategor", kateg);
                            intent.putExtra("kvizovi", kvizoviZaSlanje);
                            startActivityForResult(intent, TEXT_REQUEST);
                            //startActivity(intent);
                        }

                    }
                    else{
                      Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                      toast.show();

                    }
                    return true;
                }
            });


            View footerView = getLayoutInflater().inflate(R.layout.listview_footer, null);
            lista.addFooterView(footerView);


            /*footerView.setOnClickListener(new View.OnClickListener() {
                ///ako zatreba neka bude tu

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                    ArrayList<Kviz> kvizoviZaSlanje = new ArrayList<>();
                    kvizoviZaSlanje = vratiKvizoveKategorije((Kategorija) spinner.getSelectedItem());
                    Kategorija kateg = (Kategorija) spinner.getSelectedItem();
                    intent.putExtra("kategorije", listaKategorija);
                    intent.putExtra("kategor", kateg);
                    intent.putExtra("kvizovi", kvizoviZaSlanje);
                    Kviz kv = null;
                    intent.putExtra("jedanKviz", kv);

                    startActivityForResult(intent, TEXT_REQUEST);
                    //startActivity(intent);

                }
            });*/


            footerView.setOnLongClickListener(new AdapterView.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {

                    if(imaLiInternetaBiloKakvogMetoda()){
                    Intent intent = new Intent(KvizoviAkt.this, DodajKvizAkt.class);
                    ArrayList<Kviz> kvizoviZaSlanje = new ArrayList<>();
                    kvizoviZaSlanje = vratiKvizoveKategorije((Kategorija) spinner.getSelectedItem());
                    Kategorija kateg = (Kategorija) spinner.getSelectedItem();
                    intent.putExtra("kategorije", listaKategorija);
                    intent.putExtra("kategor", kateg);
                    intent.putExtra("kvizovi", kvizoviZaSlanje);
                    Kviz kv = null;
                    intent.putExtra("jedanKviz", kv);

                    startActivityForResult(intent, TEXT_REQUEST);
                }else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Nema internet konekcije !", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    return true;
                }

            });

            ucitajSvePodatkeIzBaze();
        } //////////kraj prvog ifa za spinner
        else {
            Kategorija pocetna = new Kategorija("Svi", "0"); ///rucno dodavanje kategorije
            listaKategorija.add(pocetna);

            zaSlanjeUFragment = pocetna; ///POSTAVLJAMO NA POCETNU SVI KATEGORIJU

            for (Kategorija k : listaKategorija) {
                System.out.println(k.getNaziv());
            }

            listaKategorijaZaSlanje = listaKategorija;

            ///standardno stimamo fragmente

            Configuration config = getResources().getConfiguration();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            DetailFrag detailFrag = new DetailFrag();
            ListaFrag listaFrag = new ListaFrag();

            ArrayList<Pitanje> li = new ArrayList<>();
            Kategorija k1 = new Kategorija("k1", "5");
            //listaKategorijaZaSlanje.add(k1);

            Kviz kviz1 = new Kviz("ime", li, k1);

            Kategorija k2 = new Kategorija("kat2", "10");
            // listaKategorijaZaSlanje.add(k2);
            Kviz kviz2 = new Kviz("ime2", li, k2);

            ///slanjeKvizovauFragment = filtrirajKvizove(zaSlanjeUFragment);
            //sviKvizovi.add(kviz1); ///doda 2 puta jer su ovdje istod dodani zasto ja ne znam

            //sviKvizovi.add(kviz2);
            //slanjeKvizovauFragment.add(kviz1);
            //slanjeKvizovauFragment.add(kviz2);

            fragmentTransaction.add(R.id.listPlace, listaFrag);
            fragmentTransaction.add(R.id.detailPlace, detailFrag);

            Bundle b = new Bundle();

            //for(Kategorija k : listaKategorija){
            //System.out.println(k.getNaziv());}

            b.putSerializable("listaKvizova", slanjeKvizovauFragment);

            b.putSerializable("listaKategorija", listaKategorija);

            listaFrag.setArguments(b);
            detailFrag.setArguments(b);

            fragmentTransaction.commit();
        }
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
            case DajSveIzBaze.STATUS_RUNNING:
                /* Ovdje ide kod koji obavještava korisnika da je poziv upućen */
                System.out.println("tu sam kad se vratim runninn");
                break;
            case DajSveIzBaze.STATUS_FINISHED:
                /* Dohvatanje rezultata i update UI */
                System.out.println("tu sam kad se vratim");

                if(resultData.getString("odradjeno") != null &&
                        resultData.get("odradjeno").equals("sve iz baze se vraca"))
                {
                    System.out.println("tu sam kad se vratim");
                    listaKategorija.clear();
                    listaKategorija.addAll((ArrayList<Kategorija>) resultData.getSerializable("sveKategorije"));
                    kategorijaAdapter.notifyDataSetChanged();
                    listaKategorija.add(kategorijaSve);
                    kategorijaAdapter.notifyDataSetChanged();

                    listaKvizova.clear();
                    listaKvizova.addAll((ArrayList<Kviz>) resultData.getSerializable("sviKvizovi"));
                    kvizAdapter.notifyDataSetChanged();

                    listaPitanja.clear();
                    listaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("svaPitanja"));

                    for(int i = 0; i < listaKategorija.size(); i++)
                    {
                        if(listaKategorija.get(i).getNaziv().equals("Svi"))
                        {
                            spinner.setSelection(i);
                            break;
                        }
                    }

                    kategorijaAdapter.notifyDataSetChanged();
                    kvizAdapter.notifyDataSetChanged();
                }

                break;
            case DajSveIzBaze.STATUS_ERROR:
                System.out.println("tu sam kad se vratim error");
                /* Slučaj kada je došlo do greške */
                if(resultData.getString("nije odradjeno") != null &&
                        resultData.get("nije odradjeno").equals("nema neke od kolekcija u bazi"))
                {
                    if(resultData.getSerializable("sveKategorije") != null)
                    {
                        listaKategorija.clear();
                        listaKategorija.addAll((ArrayList<Kategorija>) resultData.getSerializable("sveKategorije"));
                        kategorijaAdapter.notifyDataSetChanged();
                        boolean b = false;
                        for(Kategorija k: listaKategorija){
                            if(k.getNaziv().equals("Svi")){
                                b= true;
                            }
                        }
                        if(b == false){
                            listaKategorija.add(kategorijaSve);
                            kategorijaAdapter.notifyDataSetChanged();
                        }
                    }

                    if(resultData.getSerializable("sviKvizovi") != null)
                    {
                        System.out.println("da li se uciraju kvizi");
                        listaKvizova.clear();
                        listaKvizova.addAll((ArrayList<Kviz>) resultData.getSerializable("sviKvizovi"));
                        kvizAdapter.notifyDataSetChanged();
                        for(Kviz k : listaKvizova){
                            System.out.println(k.getNaziv());
                        }
                        kvizAdapter.notifyDataSetChanged();
                    }

                    if(resultData.getSerializable("svaPitanja") != null)
                    {
                        System.out.println("da li se ucitaju pitanja");
                        listaPitanja.clear();
                        listaPitanja.addAll((ArrayList<Pitanje>) resultData.getSerializable("svaPitanja"));

                    }
                }

                break;
        }
    }

    //interfejs koji je vezan za fragment
    @Override
    public void onItemClicked(int pos) {
        //listaKategorijaZaSlanje = listaKategorija;

        //slanjeKvizovauFragment = filtrirajKvizove(zaSlanjeUFragment);

        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        DetailFrag detailFrag = new DetailFrag();


        Bundle b = new Bundle();
        // b.putSerializable("listaKategorija", listaKategorija);

        b.putSerializable("listaKvizova", slanjeKvizovauFragment);
        b.putSerializable("listaKategorija", listaKategorija);

        detailFrag.setArguments(b);

        //ListaFrag listaFrag = new ListaFrag();
        //fragmentTransaction.replace(R.id.listPlace, listaFrag);
        fragmentTransaction.replace(R.id.detailPlace, detailFrag);


        fragmentTransaction.commit();
    }


    public void napuniSve()
    {
        Kategorija pocetna = new Kategorija("Svi", "0");

        listaKategorija.add(pocetna);
        kategorijaAdapter.notifyDataSetChanged();

    }


    public ArrayList<Kviz> vratiKvizoveKategorije(Kategorija k) {
        ArrayList<Kviz> zaVracanje = new ArrayList<>();
        if (k.getNaziv().equals("Svi")) zaVracanje = listaKvizova;
        else {
            Kategorija kate = (Kategorija) spinner.getSelectedItem();
            for (Kviz kv : listaKvizova) {
                if (kv.getKategorija().getNaziv().equals(kate.getNaziv())) zaVracanje.add(kv);
            }
        }
        return zaVracanje;
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


         if(imaLiInternetaBiloKakvogMetoda()){
            Toast toast = Toast.makeText(getApplicationContext(), "Ucitavaju se kvizovi, molimo sacekajte!", Toast.LENGTH_SHORT);
            toast.show();}


        if (kategorijaAdapter.getItem(position).getNaziv().equals("Svi")) {
            kvizAdapter = new KvizAdapter(this, listaKvizova);
            lista.setAdapter(kvizAdapter);
        } else {
            Kategorija kate = kategorijaAdapter.getItem(position);


            kvizAdapter.getFilter().filter(kate.getNaziv() + " " + kate.getId(), new Filter.FilterListener() {
                @Override
                public void onFilterComplete(int count) {}
            });
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // super.onActivityResult(requestCode, resultCode, data);

        System.out.println(requestCode);
        System.out.println(resultCode);
        System.out.println("tu sam");

        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                //ne radi nista vrati se
                //System.out.println("tu sam");

            }
        }


        if (requestCode == 1) {
            System.out.println("jesam li bar ovdje");
            if (resultCode == RESULT_OK) {
                System.out.println("TU SAM");


                if (spinner != null) {
                    super.onActivityResult(requestCode, resultCode, data);

                    listaKategorija = (ArrayList<Kategorija>) data.getSerializableExtra("SveKategorije");

                    kategorijaAdapter = new KategorijaAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaKategorija);
                    spinner.setAdapter(kategorijaAdapter);
                    kategorijaAdapter.notifyDataSetChanged();

                    int brojac = 0;
                    for (Kategorija k : listaKategorija) {
                        if (k.getNaziv().equals("Dodaj Kategoriju")) break;
                        brojac++;

                    }
                    listaKategorija.remove(brojac);
                    kategorijaAdapter.notifyDataSetChanged();
                    spinner.setSelection(0);


                    Kategorija primljenaKategorija;
                    primljenaKategorija = (Kategorija) data.getSerializableExtra("kategorijaZaSlanje");

                    staroIme = (String) data.getSerializableExtra("staroIme"); ///dodala sam ovo
                    Boolean kakavJeKviz = (Boolean) data.getBooleanExtra("istinitost", false);

                    Kviz prim;
                    prim = (Kviz) data.getSerializableExtra("vracamKviz");


                    if (kakavJeKviz == true) {
                        listaKvizova.add(prim);
                        kvizAdapter.notifyDataSetChanged();
                    } else if (kakavJeKviz == false) {

                        int brojaccc = 0;
                        for (Kviz k : listaKvizova) {
                            if (k.getNaziv().equals(staroIme)) {
                                listaKvizova.remove(brojaccc);

                                kvizAdapter.notifyDataSetChanged();

                                listaKvizova.add(prim);

                                kvizAdapter.notifyDataSetChanged();
                                break;
                            }
                            brojac++;
                        }
                        kvizAdapter.notifyDataSetChanged();


                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);

                    System.out.println("osdhoghsgphsdpfhvsdpyhfvsdhfvhspd'hs'dhg'spd");

                    listaKategorija = (ArrayList<Kategorija>) data.getSerializableExtra("SveKategorije");
                    int brojac = 0;
                    for (Kategorija k : listaKategorija) {
                        if (k.getNaziv().equals("Dodaj Kategoriju")) break;
                        brojac++;
                        ////brisanje kategorije dodak kat
                    }
                    listaKategorija.remove(brojac);

                    listaKategorijaZaSlanje = listaKategorija;

                    Kategorija primljenaKategorija;
                    primljenaKategorija = (Kategorija) data.getSerializableExtra("kategorijaZaSlanje");

                    zaSlanjeUFragment = primljenaKategorija;


                    Kviz prim;
                    prim = (Kviz) data.getSerializableExtra("vracamKviz");

                    if (kakavJeKviz == true) {
                        sviKvizovi.add(prim);
                    } else if (kakavJeKviz == false) {

                        int brojaccc = 0;
                        for (Kviz k : sviKvizovi) {
                            if (k.getNaziv().equals(staroIme)) {
                                sviKvizovi.remove(brojaccc);
                                sviKvizovi.add(prim);
                                break;
                            }
                            brojac++;
                        }

                    }


                }

            }

        }

        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {

                listaKategorija = (ArrayList<Kategorija>) data.getSerializableExtra("SveKategorije");

                kategorijaAdapter = new KategorijaAdapter(this, android.R.layout.simple_spinner_dropdown_item, listaKategorija);
                spinner.setAdapter(kategorijaAdapter);
                kategorijaAdapter.notifyDataSetChanged();

                int brojac = 0;
                for (Kategorija k : listaKategorija) {
                    if (k.getNaziv().equals("Dodaj Kategoriju")) break;
                    brojac++;

                }
                listaKategorija.remove(brojac);
                kategorijaAdapter.notifyDataSetChanged();
                spinner.setSelection(0);

            }
        }



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
