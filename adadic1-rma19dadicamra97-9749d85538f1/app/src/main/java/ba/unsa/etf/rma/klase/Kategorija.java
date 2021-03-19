package ba.unsa.etf.rma.klase;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Kategorija implements Serializable {

    private String naziv, id ;

    public Kategorija(String naziv, String id) {
        this.naziv = naziv;
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*@Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString (naziv);
        dest.writeString (id);
    }

    public static final Parcelable.Creator<Kategorija> CREATOR = new Parcelable.Creator<Kategorija>() {
        public Kategorija createFromParcel(Parcel in) {
            return new Kategorija(in);
        }

        public Kategorija[] newArray(int size) {
            return new Kategorija[size];
        }
    };

    private Kategorija(Parcel in) {
        naziv = in.readString();
        id = in.readString();
    }*/
}
