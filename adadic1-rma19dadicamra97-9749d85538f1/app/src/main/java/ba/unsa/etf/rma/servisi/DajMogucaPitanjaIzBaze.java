package ba.unsa.etf.rma.servisi;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

public class DajMogucaPitanjaIzBaze extends IntentService
{
    public static final int STATUS_RUNNING = 0;
    public static final int STATUS_FINISHED = 1;
    public static final int STATUS_ERROR = 2;

    public DajMogucaPitanjaIzBaze()
    {
        super(null);
    }

    public DajMogucaPitanjaIzBaze(String name)
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

        ArrayList<Pitanje> dodanaPitanja = (ArrayList<Pitanje>) intent.getSerializableExtra("dodanaPitanja");

        String token = null;

        try
        {
            token = dajTokenZaKonekciju();

            String url = "https://firestore.googleapis.com/v1/projects/rma-spirala-d5105/databases/(default)/documents/Pitanja";
            URL urlObjekat = new URL(url);
            HttpURLConnection konekcija = (HttpURLConnection) urlObjekat.openConnection();
            konekcija.setRequestProperty("Authorization", "Bearer "+token);
            konekcija.setRequestMethod("GET");
            konekcija.setRequestProperty("Content-Type", "appliction/json");
            konekcija.setRequestProperty("Accept", "appliation/json");

            InputStream odgovor = konekcija.getInputStream();
            String rezultat = pretvoriInputStreamUString(odgovor);

            JSONObject jsonObject = new JSONObject(rezultat);
            JSONArray jsonDocuments = jsonObject.getJSONArray("documents");

            ArrayList<Pitanje> mogucaPitanja = new ArrayList<>();

            for(int i=0; i<jsonDocuments.length(); i++)
            {
                JSONObject jsonJedanDokument = jsonDocuments.getJSONObject(i);
                JSONObject jsonFields = jsonJedanDokument.getJSONObject("fields");

                JSONObject jsonNaziv = jsonFields.getJSONObject("naziv");
                String nazivPitanja = jsonNaziv.getString("stringValue");
                String tekstPitanja = jsonNaziv.getString("stringValue");

                boolean dodajPitanje = true;
                for(int j=0; j<dodanaPitanja.size(); j++)
                {
                    if(nazivPitanja.equals(dodanaPitanja.get(j).getNaziv()))
                    {
                        dodajPitanje = false;
                        break;
                    }
                }

                if(dodajPitanje == false)
                    continue;

                JSONObject indexTacnog = jsonFields.getJSONObject("indexTacnog");
                int intIndexTacnog = indexTacnog.getInt("integerValue");

                JSONObject odgovori = jsonFields.getJSONObject("odgovori");
                JSONObject arrayValue = odgovori.getJSONObject("arrayValue");
                JSONArray values = arrayValue.getJSONArray("values");

                ArrayList<String> odgovoriNaPitanje = new ArrayList<>();
                for(int j=0; j<values.length(); j++)
                {
                    JSONObject jsonJedanOdgovor = values.getJSONObject(j);
                    String stringOdogovr = jsonJedanOdgovor.getString("stringValue");

                    odgovoriNaPitanje.add(stringOdogovr);
                }
                String tacanOdogvor =  odgovoriNaPitanje.get(intIndexTacnog);

                Pitanje pitanje = new Pitanje(nazivPitanja, tekstPitanja, tacanOdogvor, odgovoriNaPitanje);
                mogucaPitanja.add(pitanje);
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable("mogucaPitanja", mogucaPitanja);
            resultReceiver.send(STATUS_FINISHED,bundle);
        }
        catch (Exception e)
        {
            Bundle bundle = new Bundle();
            bundle.putString("nisuUcitanaMoguca", "ili nema kolekcije pitanja, ili greska u kodu");
            resultReceiver.send(STATUS_ERROR, bundle);
        }
    }

    public String dajTokenZaKonekciju()
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return token;
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
}
