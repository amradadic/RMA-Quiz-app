package ba.unsa.etf.rma;

import android.content.Context;

import android.database.Cursor;
import android.provider.CalendarContract;

import java.util.HashMap;
import java.util.Map;

public class KalendarContentResolver {
    public static final String[] proj =  new String[]{
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.TITLE
    };
    Context context;
     //konstruktor
    public KalendarContentResolver(Context context) {
        this.context = context;
    }

    public Map<String, String>  procitajPodatke(){
        Map<String, String> mapaZaVracanje = new HashMap<>();
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI,proj, null,null,null);

        }catch (SecurityException e){
            e.printStackTrace();
        }
        if(cursor != null)
        while(cursor.moveToNext()){
            String pocetak = cursor.getString(0);
            String naziv = cursor.getString(1);
            mapaZaVracanje.put(pocetak,naziv);
        }
        return mapaZaVracanje;




    }
}
