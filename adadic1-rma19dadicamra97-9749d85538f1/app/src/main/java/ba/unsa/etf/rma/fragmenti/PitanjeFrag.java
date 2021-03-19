package ba.unsa.etf.rma.fragmenti;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;


public class PitanjeFrag extends Fragment {


    TextView tekstPitanja;
    ListView listaOdgovora;
    Kviz k =null;
    ArrayList<Pitanje> listaPitanja= new ArrayList<>();
    ArrayList<String> listaO = new ArrayList<>(); ////ODGOVORI
    ArrayList<Pitanje> odradjenaPitanja = new ArrayList<>();
    boolean jelkraj = false;

    Pitanje zaSlanje ;

    Pitanje trenutno = null;
ArrayAdapter<String> adapterOdgovora;
    public interface OnItemClick
    {
        public void onItemClicked(int pos);
    }

    private OnItemClick klik;



    public PitanjeFrag() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        View view =inflater.inflate(R.layout.fragment_pitanje, container, false);

       tekstPitanja  = (TextView) view.findViewById(R.id.tekstPitanja);

        listaOdgovora = (ListView) view.findViewById(R.id.odgovoriPitanja);
        adapterOdgovora = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listaO);

        listaOdgovora.setAdapter(adapterOdgovora);



        return view;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getArguments() != null && getArguments().containsKey("trenkviz")) {
            k = (Kviz) getArguments().getSerializable("trenkviz");


            if (IgrajKvizAkt.brojacPitanja < k.getPitanja().size()) {

                listaPitanja = k.getPitanja();

                // Collections.shuffle(listaPitanja);
                ///uzimamo prvo pitanje
                trenutno = listaPitanja.get(IgrajKvizAkt.brojacPitanja);
                tekstPitanja.setText(trenutno.getNaziv());
                listaO = trenutno.getOdgovori();


                //System.out.println(trenutno.getNaziv());

                adapterOdgovora = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listaO);
                listaOdgovora.setAdapter(adapterOdgovora);
                listaOdgovora.deferNotifyDataSetChanged();


                try {
                    klik = (OnItemClick) getActivity();

                } catch (ClassCastException e) {

                }


                listaOdgovora.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        adapterOdgovora = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listaO) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View v = super.getView(position, convertView, parent);
                                if (trenutno.getTacan().equals(listaO.get(position))) {
                                    v.setBackgroundColor(Color.parseColor("#00e676"));
                                } else {
                                    v.setBackgroundColor(Color.RED);
                                }
                                return v;
                            }
                        };
                        int indeksKliknutog = 0;// listaOdgovora.getSelectedItemPosition()+1;
                        for(String s : listaO){
                            if(s.equals(listaO.get(position))) break;
                            indeksKliknutog++;
                        }
                        if(listaO.get(indeksKliknutog).equals(trenutno.getTacan())){
                            IgrajKvizAkt.brojTacnihOdgovora++;
                        }
                        listaOdgovora.setAdapter(adapterOdgovora);
                        klik.onItemClicked(position);


                    }
                });



            }
            else{

                IgrajKvizAkt.jelgotovK = true;
                System.out.println("da li je zavrsio prokleti kviz " + IgrajKvizAkt.jelgotovK);
                tekstPitanja.setText("Kviz je zavrsen!");
                listaO = new ArrayList<>();
                jelkraj = true;
                Bundle b = new Bundle();
                b.putBoolean("krajKviza", jelkraj);



                //System.out.println(trenutno.getNaziv());

                adapterOdgovora = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listaO);
                listaOdgovora.setAdapter(adapterOdgovora);
                listaOdgovora.deferNotifyDataSetChanged();

               // return;
            }



        }

        else{

        }

    }






}
