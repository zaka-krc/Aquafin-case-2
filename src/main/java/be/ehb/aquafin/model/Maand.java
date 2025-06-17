package be.ehb.aquafin.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Maand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Min(value = 2005, message = "Jaar moet na 2005 zijn")
    private int jaar;

    @Min(value = 1)
    @Max(value = 12)
    private int maand;

    @Min(value = 0, message = "Neerslag kan niet negatief zijn")
    private double neerslag;

    public Maand() {}

    public Maand(int jaar, int maand, double neerslag) {
        this.jaar = jaar;
        this.maand = maand;
        this.neerslag = neerslag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getNeerslag() {
        return neerslag;
    }

    public void setNeerslag(double neerslag) {
        this.neerslag = neerslag;
    }

    public int getMaand() {
        return maand;
    }

    public void setMaand(int maand) {
        this.maand = maand;
    }

    public int getJaar() {
        return jaar;
    }

    public void setJaar(int jaar) {
        this.jaar = jaar;
    }

    public String getSeizoen() {
        if (maand == 12 || maand == 1 || maand == 2) return "Winter";
        if (maand >= 3 && maand <= 5) return "Lente";
        if (maand >= 6 && maand <= 8) return "Zomer";
        return "Herfst";
    }

    public String getMaandNaam() {
        String[] maandNamen = {
                "Januari", "Februari", "Maart", "April",
                "Mei", "Juni", "Juli", "Augustus",
                "September", "Oktober", "November", "December"
        };

        if (maand >= 1 && maand <= 12) {
            return maandNamen[maand - 1];
        }
        return "Onbekend";
    }
}