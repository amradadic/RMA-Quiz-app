package ba.unsa.etf.rma.adapteri;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import ba.unsa.etf.rma.R;
import ba.unsa.etf.rma.klase.Pitanje;

public class PitanjeAdapterPlus extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private ArrayList<Pitanje> pitanjeList ;
    private Context context;

    public PitanjeAdapterPlus(ArrayList<Pitanje> pitanjeList, Context context) {
        this.pitanjeList = pitanjeList;
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return pitanjeList.size();
    }

    @Override
    public Object getItem(int position) {
        return pitanjeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder{
        TextView pitanje;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View updateView;
        PitanjeAdapter.ViewHolder viewHolder;
        if(view == null ){
            updateView = mLayoutInflater.inflate(R.layout.element_liste_moguca_pitanja, null);
            viewHolder = new PitanjeAdapter.ViewHolder();
            viewHolder.pitanje = (TextView) updateView.findViewById(R.id.Itemname);
            updateView.setTag(viewHolder);
        }else{
            updateView = view;
            viewHolder = (PitanjeAdapter.ViewHolder) updateView.getTag();
        }

        final Pitanje item = (Pitanje) getItem(position);
        viewHolder.pitanje.setText(item.getNaziv());
        return updateView;
    }
}
