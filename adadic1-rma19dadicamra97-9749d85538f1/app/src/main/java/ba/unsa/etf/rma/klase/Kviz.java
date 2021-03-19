package ba.unsa.etf.rma.klase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Kviz implements Serializable {
    String naziv;
    ArrayList<Pitanje> pitanja =  new ArrayList<>();
    Kategorija kategorija;
    String slika;

    public Kviz(Kviz itemAtPosition) {
        naziv=itemAtPosition.naziv;
        pitanja=itemAtPosition.pitanja;
        kategorija=itemAtPosition.kategorija;
        slika=itemAtPosition.slika;
    }


    public String getSlika() {
        return slika;
    }

    public void setSlika(String slika) {
        this.slika = slika;
    }

    public Kviz(String naziv, ArrayList<Pitanje> pitanja, Kategorija kategorija) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.kategorija = kategorija;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Pitanje> getPitanja() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Pitanje> pitanja) {
        this.pitanja = pitanja;
    }

    public Kategorija getKategorija() {
        return kategorija;
    }

    public void setKategorija(Kategorija kategorija) {
        this.kategorija = kategorija;
    }
    void dodajPitanje(Pitanje pitanje){
        pitanja.add(pitanje);
    }

   /* @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString (naziv);
        dest.writeArray(new ArrayList[]{pitanja});
        dest.writeString(slika);
        //kako za kategoriju ?????

       // dest.writeString (id);
    }

    public static final Parcelable.Creator<Kviz> CREATOR = new Parcelable.Creator<Kviz>() {
        public Kviz createFromParcel(Parcel in) {
            return new Kviz(in);
        }

        public Kviz[] newArray(int size) {
            return new Kviz[size];
        }
    };

    private Kviz(Parcel in) {
        naziv = in.readString();
      //  id = in.readString();
        slika = in.readString();
       /// pitanja = in.readArrayList(pitanja);
        ///kako za pitanja i klasu kategorija ???

    }*/


}
