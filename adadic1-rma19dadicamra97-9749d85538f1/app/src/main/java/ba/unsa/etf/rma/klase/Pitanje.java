package ba.unsa.etf.rma.klase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Pitanje implements Serializable {
    /*Atributi: string naziv, string tekstPitanja, ArrayList<string> odgovori, string tacan
- Setteri i getteri za sve atribute oblika getX(), setX(T X), gdje je X naziv atributa (u
nazivu metode počinje velikim slovom inače malim), T je tip atributa
- Metoda: ArrayList<string> dajRandomOdgovore() - vraća listu odgovora poredanu
u slučajnom redosljedu, u pravilu uvijek drugačijem
*/
    String naziv, tekstPitanja, tacan;
    ArrayList<String> odgovori = new ArrayList<>();

    public Pitanje(String naziv, String tekstPitanja, String tacan, ArrayList<String> odgoviri) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.tacan = tacan;
        this.odgovori = odgoviri;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    ArrayList<String> dajRandomOdgovore(){
        Collections.shuffle(odgovori);
        return odgovori;
    }

}
