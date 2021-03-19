package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.klase.Kategorija;

public class KategorijaAdapter extends ArrayAdapter<Kategorija> {

        private Context context;

        ArrayList<Kategorija> klista =new ArrayList<>();
        public KategorijaAdapter(Context context, int textViewResourceId, ArrayList<Kategorija> values) {
            super(context, textViewResourceId, values);
            ///klista = new ArrayList<>();
            this.context = context;
            this.klista = values;
        }

        public int getCount(){

            return klista.size();
        }

        public Kategorija getItem(int position){
            return klista.get(position);
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = new TextView(context);
            view.setTextColor(Color.BLACK);
            view.setGravity(Gravity.CENTER);
            view.setText(klista.get(position).getNaziv());

            return view;
        }

        //View of Spinner on dropdown Popping

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            TextView view = new TextView(context);
            view.setTextColor(Color.BLACK);
            view.setText(klista.get(position).getNaziv());
            view.setHeight(60);

            return view;
        }
    }

