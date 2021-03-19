package ba.unsa.etf.rma.fragmenti;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.KategorijaAdapter;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;

public class ListaFrag extends Fragment {

    ListView listView;
    KategorijaAdapter kategorijaAdapter;
    ArrayList<Kategorija> listaKategorija = new ArrayList<>();
    private OnItemClick klik;


    public ListaFrag() {
    }

    public interface OnItemClick {
        public void onItemClicked(int pos);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("listaKategorija")) {



            listaKategorija = (ArrayList<Kategorija>) getArguments().getSerializable("listaKategorija");
            //Kategorija ka = new Kategorija("Testkat", "2");

            //listaKategorija.add(ka);


            kategorijaAdapter = new KategorijaAdapter(getContext(), android.R.layout.simple_list_item_1, listaKategorija);
            listView.setAdapter(kategorijaAdapter);
            listView.deferNotifyDataSetChanged();

            for(Kategorija k : listaKategorija){
                System.out.println(k.getNaziv());
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Kategorija kat = listaKategorija.get(position);
                    KvizoviAkt.zaSlanjeUFragment = kat;




                    try {
                        klik = (ListaFrag.OnItemClick) getActivity();

                    } catch (ClassCastException e) {

                    }
                    klik.onItemClicked(position);
                }
            });




        }


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lista, container, false);

        listView = (ListView) view.findViewById(R.id.listaKategorija);
        kategorijaAdapter = new KategorijaAdapter(getContext(), android.R.layout.simple_list_item_1, listaKategorija);
        listView.setAdapter(kategorijaAdapter);


        return view;
    }


}

