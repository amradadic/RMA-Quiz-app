package ba.unsa.etf.rma.fragmenti;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.adapteri.KvizAdapter;
import ba.unsa.etf.rma.aktivnosti.DodajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kategorija;
import ba.unsa.etf.rma.klase.Kviz;
import ba.unsa.etf.rma.klase.Pitanje;

import static ba.unsa.etf.rma.aktivnosti.KvizoviAkt.sviKvizovi;

public class DetailFrag extends Fragment {

    GridView gridView;
    private Boolean kakavJeKviz;
    KvizAdapter kvizAdapter;
    ArrayList<Kviz> listaKvizova = new ArrayList<>();
    public static final int TEXT_REQUEST = 1;
    String staroIme;
    ArrayList<Kategorija> listaKategorija = new ArrayList<>();

    public DetailFrag() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("listaKvizova")) {
            ///Kategorija ka = new Kategorija("Testkat", "2");
            ArrayList<Pitanje> listap = new ArrayList<>();

            //Kviz k = new Kviz("test", listap, ka);
            Kategorija kat = new Kategorija("Svi", "671"); ///671 je plus
            Kviz dodajKv = new Kviz("Dodaj Kviz", listap, kat); ///provjeriti imal kviza ovog vec


            listaKvizova = (ArrayList<Kviz>) getArguments().getSerializable("listaKvizova");
            listaKategorija = (ArrayList<Kategorija>) getArguments().getSerializable("listaKategorija");



            listaKvizova = filtrirajKvizove(KvizoviAkt.zaSlanjeUFragment);
            //listaKvizova.add(dodajKv);


            kvizAdapter = new KvizAdapter(getContext(),listaKvizova);
            gridView.setAdapter(kvizAdapter);
            gridView.deferNotifyDataSetChanged();
            kvizAdapter.notifyDataSetChanged();

            boolean imal = false;
            for(Kviz k : sviKvizovi){
                if(k.getNaziv().equals("Dodaj Kviz")){
                    imal=true;
                    break;
                }
            }

            if(!imal){
                sviKvizovi.add(dodajKv);
            }
            kvizAdapter.notifyDataSetChanged();




            /*gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(getContext(), IgrajKvizAkt.class);
                    if (position >= 0 && position <= listaKvizova.size())
                        intent.putExtra("trenutni", listaKvizova.get(position));
                    startActivityForResult(intent, TEXT_REQUEST);

                }
            });*/


            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (listaKvizova != null && position >= 0 && position < listaKvizova.size()) {
                        Intent intent = new Intent(getContext(), DodajKvizAkt.class);
                        staroIme = listaKvizova.get(position).getNaziv();
                        ///staroIme = listaKvizova.get(position).getNaziv();
                        intent.putExtra("jedanKviz", listaKvizova.get(position));
                        ArrayList<Kviz> kvizoviZaSlanje = new ArrayList<>();
                        kvizoviZaSlanje = sviKvizovi;
                        Kategorija kateg = KvizoviAkt.zaSlanjeUFragment;
                        intent.putExtra("kategorije", listaKategorija);
                        intent.putExtra("kategor", kateg);
                        intent.putExtra("kvizovi", kvizoviZaSlanje);
                        startActivityForResult(intent, 1);
                        //startActivity(intent);
                    }


                    return true;
                }
            });



            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if(!listaKvizova.get(position).getNaziv().equals("Dodaj Kviz")){
                        Intent intent = new Intent(getContext(), IgrajKvizAkt.class);
                        if (position >= 0 && position <= listaKvizova.size())
                            intent.putExtra("trenutni", listaKvizova.get(position));
                        startActivityForResult(intent, TEXT_REQUEST);}



                }
            });

        }





    }






    public ArrayList<Kviz> filtrirajKvizove(Kategorija k) {
        ArrayList<Kviz> zaVracanje = new ArrayList<>();
        if (k.getNaziv().equals("Svi")) zaVracanje = sviKvizovi;
        else {
            //Kategorija kate = (Kategorija) spinner.getSelectedItem();
            for (Kviz kv : sviKvizovi) {
                if (kv.getKategorija().getNaziv().equals(KvizoviAkt.zaSlanjeUFragment.getNaziv())) zaVracanje.add(kv);
            }
        }
        ///////////////////////////////////////////////////////
        ArrayList<Pitanje> listap = new ArrayList<>();
        //Kategorija kat = new Kategorija("Svi", "671"); ///671 je plus
        //Kviz dodajKv = new Kviz("Dodaj Kviz", listap, kat); ///provjeriti imal kviza ovog vec
//
        //zaVracanje.add(dodajKv);
        return zaVracanje;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        gridView = view.findViewById(R.id.gridKvizovi);
        kvizAdapter = new KvizAdapter(getContext(), listaKvizova);
        gridView.setAdapter(kvizAdapter);





        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        System.out.println("FRAGMENT ONACTIVITY");


        listaKategorija = (ArrayList<Kategorija>) data.getSerializableExtra("SveKategorije");
        int brojac = 0;
        for (Kategorija k : listaKategorija) {
            if (k.getNaziv().equals("Dodaj Kategoriju")) break;
            brojac++;
            ////brisanje kategorije dodak kat
        }
        listaKategorija.remove(brojac);

        KvizoviAkt.listaKategorijaZaSlanje = listaKategorija;

        Kategorija primljenaKategorija;
        primljenaKategorija = (Kategorija) data.getSerializableExtra("kategorijaZaSlanje");

        KvizoviAkt.zaSlanjeUFragment = primljenaKategorija;


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
