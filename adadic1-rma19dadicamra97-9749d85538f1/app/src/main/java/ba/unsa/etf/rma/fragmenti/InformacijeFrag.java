package ba.unsa.etf.rma.fragmenti;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.aktivnosti.IgrajKvizAkt;
import ba.unsa.etf.rma.aktivnosti.KvizoviAkt;
import ba.unsa.etf.rma.klase.Kviz;


public class InformacijeFrag extends Fragment {






Kviz k =null;

    public InformacijeFrag() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///  nazivKviza= (TextView) fi


    }



    private TextView nazivKviza;
    private TextView brojTacnih;
    private TextView brojPreostalih;
    private TextView procenatTacnih;
    private Button dugmeZavrsi;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {
        //Ovdje se dodjeljuje layout fragmentu, tj. šta će se nalaziti unutar fragmenta
        //Ovu liniju ćemo poslije promijeniti
        View view =inflater.inflate(R.layout.fragment_informacije, container, false);

        nazivKviza = (TextView) view.findViewById(R.id.infNazivKviza);
        brojTacnih = (TextView) view.findViewById(R.id.infBrojTacnihPitanja);
        brojPreostalih = (TextView) view.findViewById(R.id.infBrojPreostalihPitanja);
        procenatTacnih = (TextView) view.findViewById(R.id.infProcenatTacni);
        dugmeZavrsi = (Button) view.findViewById(R.id.btnKraj);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);





        if(getArguments()!=null && getArguments().containsKey("trenkviz")){
            k= (Kviz) getArguments().getSerializable("trenkviz");

            if (k != null) {
                nazivKviza.setText(IgrajKvizAkt.imeKvizaKojegPrimamo);
                String str = String.valueOf(IgrajKvizAkt.brojTacnihOdgovora);
                brojTacnih.setText(str);

                int brPreostalih = k.getPitanja().size() - IgrajKvizAkt.brojacPitanja;
                String s = String.valueOf(brPreostalih);

                IgrajKvizAkt.brojPreostalih = brPreostalih;
                brojPreostalih.setText(s);

                IgrajKvizAkt.procenatTacnih = ((double) IgrajKvizAkt.brojTacnihOdgovora / k.getPitanja().size()) *100;
                String procenat = String.valueOf(IgrajKvizAkt.procenatTacnih) + "%";
                procenatTacnih.setText(procenat);
            }


        }


        dugmeZavrsi.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), KvizoviAkt.class);
                IgrajKvizAkt.brojacPitanja=0;
                IgrajKvizAkt.brojTacnihOdgovora = 0;
                IgrajKvizAkt.brojPreostalih=0;
                IgrajKvizAkt.procenatTacnih=0;

                ///vracanje na pocetnu stranicu
                getActivity().finish();



            }
        });

    }



}
