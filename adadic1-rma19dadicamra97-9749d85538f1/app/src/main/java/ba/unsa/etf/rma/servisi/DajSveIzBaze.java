package ba.unsa.etf.rma.servisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DajSveIzBaze extends IntentService
{
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public DajSveIzBaze()
    {
        super(null);
    }

    public DajSveIzBaze(String name)
    {
        super(name);
        // Sav posao koji treba da obavi konstruktor treba da se
        // nalazi ovdje
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // Akcije koje se trebaju obaviti pri kreiranju servisa
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Kod koji se nalazi ovdje će se izvršavati u posebnoj niti
        // Ovdje treba da se nalazi funkcionalnost servisa koja je
        // vremenski zahtjevna
        final ResultReceiver resultReceiver = intent.getParcelableExtra("risiver");

        ArrayList<Kategorija> sveKategorije = new ArrayList<>();
        ArrayList<Kviz> sviKvizovi = new ArrayList<>();
        ArrayList<Pitanje> svaPitanja = new ArrayList<>();

        String token = dajTokenZaPristupBazi();

        //ucitavanje svih kategorija
        boolean nemaKategorijaUBazi = false;
        boolean uspjesnoProcitaneSveKategorije = false;
        try
        {
            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kategorije";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer " + token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray documents = jsonObject.getJSONArray("documents");

            for(int i=0; i<documents.length(); i++)
            {
                JSONObject jsonJedanDokument = documents.getJSONObject(i);

                JSONObject jsonFields = jsonJedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String nazivKategorije = jsonNaziv.getString("stringValue");

                JSONObject jsonIdIkonice = jsonFields.getJSONObject("idIkonice");
                String idKategorije = String.valueOf(jsonIdIkonice.getInt("integerValue"));

                Kategorija novoprocitanaKategorija = new Kategorija(nazivKategorije, idKategorije);
                sveKategorije.add(novoprocitanaKategorija);
            }
            uspjesnoProcitaneSveKategorije = true;
        }
        catch (JSONException e)
        {
            Log.w("UPOZORENJE:", "nema kolekcije sa kategorijama u bazi");
            nemaKategorijaUBazi = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //ucitavanje svih kvizova
        boolean uspjesnoProcitaniSviKvizovi = false;
        boolean nemaKvizovaUBazi = false;
        try
        {
            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kvizovi";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer " + token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray jsonDocuments = jsonObject.getJSONArray("documents");

            for(int i=0; i<jsonDocuments.length(); i++)
            {
                JSONObject jsonJedanDokument = jsonDocuments.getJSONObject(i);
                JSONObject jsonFields = jsonJedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String nazivKviza = jsonNaziv.getString("stringValue");

                JSONObject jsonIdKategorije = jsonFields.getJSONObject("idKategorije");
                ///////////////
                String stringIdKategorije = jsonIdKategorije.getString("stringValue");

                /////////
                Kategorija kategorijaKojojPripadaKviz = null;
                if(stringIdKategorije.equals("Svi") == true){
                    kategorijaKojojPripadaKviz = new Kategorija("Svi", "0");
                }
                else{

                    kategorijaKojojPripadaKviz = dajKategorijuKviza(stringIdKategorije, token);
                }

                ////////////////////

                JSONObject jsonPitanja = jsonFields.getJSONObject("pitanja");
                JSONObject jsonArrayValue = jsonPitanja.getJSONObject("arrayValue");
                JSONArray jsonValues = jsonArrayValue.getJSONArray("values");

                ArrayList<Pitanje> pitanjaIzProcitnogKviza = new ArrayList<>();
                for(int j = 0; j< jsonValues.length(); j++)
                {
                    JSONObject jsonIdJendogPitanja = jsonValues.getJSONObject(j);
                    String stringIdJendogPitanja = jsonIdJendogPitanja.getString("stringValue");

                    Pitanje pitanjeIzKviza = dajPitanjeKviza(stringIdJendogPitanja, token);

                    if(pitanjeIzKviza == null)
                        throw new JSONException("NIJE DOBRO UCITANO "+String.valueOf(j)+". PITANJE");
                    else
                        pitanjaIzProcitnogKviza.add(pitanjeIzKviza);
                }
                Kviz novoprocitaniKviz = new Kviz(nazivKviza, pitanjaIzProcitnogKviza, kategorijaKojojPripadaKviz);
                sviKvizovi.add(novoprocitaniKviz);
            }

            uspjesnoProcitaniSviKvizovi = true;
        }
        catch (JSONException e)
        {
            Log.w("UPOZORENJE:", "nema kolekcije sa kvizovima u bazi");
            nemaKvizovaUBazi = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //ucitavnje svih pitanja
        boolean uspjesnoProcitanaSvaPitanja = false;
        boolean nemaPitanjaUBazi = false;
        try
        {
            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Pitanja";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer " + token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray jsonDocuments = jsonObject.getJSONArray("documents");

            for(int i=0; i<jsonDocuments.length(); i++)
            {
                JSONObject jedanDokument = jsonDocuments.getJSONObject(i);
                JSONObject jsonFields = jedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String nazivProcitanogPitanja = jsonNaziv.getString("stringValue");

                ArrayList<String> alOdgovori = new ArrayList<>();
                JSONObject jsonOdgovori = jsonFields.getJSONObject("odgovori");
                JSONObject jsonArrayValue = jsonOdgovori.getJSONObject("arrayValue");
                JSONArray jsonValues = jsonArrayValue.getJSONArray("values");

                ArrayList<String> odgovoriProcitanogPitanja = new ArrayList<String>();
                for(int j = 0; j < jsonValues.length(); j++)
                {
                    JSONObject jedanOdogovor = jsonValues.getJSONObject(j);
                    odgovoriProcitanogPitanja.add(jedanOdogovor.getString("stringValue"));
                }

                JSONObject jsonIndexTacnog = jsonFields.getJSONObject("indexTacnog");
                int intIndexTacnog = jsonIndexTacnog.getInt("integerValue");
                String tacanOdgovor = odgovoriProcitanogPitanja.get(intIndexTacnog);

                Pitanje novoprocitanoPitanje = new Pitanje(nazivProcitanogPitanja, nazivProcitanogPitanja, tacanOdgovor, odgovoriProcitanogPitanja);
                svaPitanja.add(novoprocitanoPitanje);
            }

            uspjesnoProcitanaSvaPitanja = true;
        }
        catch (JSONException e)
        {
            Log.w("UPOZORENJE:", "nema kolekcije sa pitanjima u bazi");
            nemaPitanjaUBazi = true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        //vracanje elemenata kroz risiver, ili vracanje informacije da je pri citanju iz baze doslo do greske
        Bundle bundle = new Bundle();
        if(uspjesnoProcitaneSveKategorije == true && uspjesnoProcitaniSviKvizovi == true && uspjesnoProcitanaSvaPitanja == true)
        {
            bundle.putSerializable("sveKategorije", sveKategorije);
            bundle.putSerializable("sviKvizovi", sviKvizovi);
            bundle.putSerializable("svaPitanja", svaPitanja);



            bundle.putSerializable("odradjeno", "sve iz baze se vraca");
            resultReceiver.send(STATUS_FINISHED, bundle);
        }
        else
        {
            bundle.putString("nije odradjeno", "nema neke od kolekcija u bazi");

            if(nemaKategorijaUBazi == false)
                bundle.putSerializable("sveKategorije", sveKategorije);

            if(nemaKvizovaUBazi == false)
                bundle.putSerializable("sviKvizovi", sviKvizovi);

            if(nemaPitanjaUBazi == false)
                bundle.putSerializable("svaPitanja", svaPitanja);


            resultReceiver.send(STATUS_ERROR, bundle);
        }
    }

    private Pitanje dajPitanjeKviza(String stringIdTrazenogPitanja, String token)
    {
        Pitanje procitanoPitanjeIzBaze = null;
        try
        {
            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Pitanja";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer " + token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray documents;
            //////////////////////////////////////

            JSONArray jsonDocuments = jsonObject.getJSONArray("documents");

            for(int i=0; i<jsonDocuments.length(); i++)
            {
                JSONObject jedanDokument = jsonDocuments.getJSONObject(i);

                JSONObject jsonFields = jedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String stringNazivPitanja = jsonNaziv.getString("stringValue");

                if(stringNazivPitanja.equals(stringIdTrazenogPitanja))
                {
                    String nazivProcitanogPitanja = jsonNaziv.getString("stringValue");
                    String tekstProcitanogPitanja = jsonNaziv.getString("stringValue");

                    ArrayList<String> alOdgovoriNaProcitanoPitanje = new ArrayList<>();
                    JSONObject jsonOdgovori = jsonFields.getJSONObject("odgovori");
                    JSONObject jsonArrayValue = jsonOdgovori.getJSONObject("arrayValue");
                    JSONArray jsonValues = jsonArrayValue.getJSONArray("values");
                    for(int j = 0; j < jsonValues.length(); j++)
                    {
                        JSONObject jsonJedanOdogovor = jsonValues.getJSONObject(j);
                        alOdgovoriNaProcitanoPitanje.add(jsonJedanOdogovor.getString("stringValue"));
                    }

                    JSONObject jsonIndexTacnog = jsonFields.getJSONObject("indexTacnog");
                    int intIndexTacnog = jsonIndexTacnog.getInt("integerValue");
                    String tacanOdgovrNaPitanje = alOdgovoriNaProcitanoPitanje.get(intIndexTacnog);

                    procitanoPitanjeIzBaze = new Pitanje(nazivProcitanogPitanja, tekstProcitanogPitanja, tacanOdgovrNaPitanje, alOdgovoriNaProcitanoPitanje);
                    break;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return procitanoPitanjeIzBaze;
    }

    private Kategorija dajKategorijuKviza(String stringIdTrazeneKategorije, String token)
    {
        Kategorija procitanaOdgovoarajucaKategorija = null;

        try
        {
            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Kategorije";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer " + token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray jsonDocuments = jsonObject.getJSONArray("documents");

            for(int i=0; i<jsonDocuments.length(); i++)
            {
                JSONObject jsonJedanDokument = jsonDocuments.getJSONObject(i);
                JSONObject jsonFields = jsonJedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String stringNazivKategorije = jsonNaziv.getString("stringValue");

                if(stringNazivKategorije.equals(stringIdTrazeneKategorije))
                {
                    String nazivProcitaneKategorije = jsonNaziv.getString("stringValue");

                    JSONObject jsonIdIkonice = jsonFields.getJSONObject("idIkonice");
                    String idIkoniceProcitaneKategorije = String.valueOf(jsonIdIkonice.getInt("integerValue"));

                    procitanaOdgovoarajucaKategorija = new Kategorija(nazivProcitaneKategorije, idIkoniceProcitaneKategorije);
                    break;
                }
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return procitanaOdgovoarajucaKategorija;
    }

    public String pretvoriInputStreamUString(InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        StringBuilder sb = new StringBuilder();
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");
        }
        catch (IOException e)
        {
            e.getStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.getStackTrace();
            }
        }

        return sb.toString();
    }

    public String dajTokenZaPristupBazi()
    {
        String token = null;

        try
        {
            InputStream is = getBaseContext().getResources().openRawResource(R.raw.secret);
            GoogleCredential credentials = null;
            credentials = GoogleCredential.fromStream(is).
                    createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
            credentials.refreshToken();

            token = credentials.getAccessToken();

            is.close();
            //Log.d("TOKEN", token);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return token;
    }
}