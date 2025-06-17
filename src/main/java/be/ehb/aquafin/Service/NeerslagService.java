package be.ehb.aquafin.Service;

import be.ehb.aquafin.model.Maand;
import be.ehb.aquafin.model.MaandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NeerslagService {

    @Autowired
    private MaandDAO dao;

    @Autowired
    private WeerAPIService apiService;

    public List<Maand> getAlle() {
        return (List<Maand>) dao.findAll();
    }

    // Nieuwe methode: Genereer voorspelling per maand voor 2025
    public Map<String, Object> genereerMaandVoorspelling(int voorspelJaar, int maand) {
        Map<String, Object> voorspelling = new HashMap<>();
        List<Map<String, Object>> maandVoorspellingen = new ArrayList<>();

        // Haal historische data op (laatste 5 jaar)
        int huidigJaar = java.time.LocalDate.now().getYear();
        int startJaar = Math.max(2020, huidigJaar - 5);

        // Als maand = 0, bereken voor alle maanden
        if (maand == 0) {
            for (int m = 1; m <= 12; m++) {
                Map<String, Object> maandData = berekenMaandVoorspelling(m, startJaar, huidigJaar);
                maandVoorspellingen.add(maandData);
            }

            // Bereken seizoensoverzicht
            List<Map<String, Object>> seizoenen = berekenSeizoensTotalen(maandVoorspellingen);
            voorspelling.put("seizoenen", seizoenen);

            // Bereken jaartotaal
            double totaalJaar = maandVoorspellingen.stream()
                    .mapToDouble(m -> (Double) m.get("voorspelling"))
                    .sum();
            voorspelling.put("totaalJaar", Math.round(totaalJaar * 10.0) / 10.0);
            voorspelling.put("totaalJaarRisico", totaalJaar > 1000 ? "GEVAAR!" : "OK");

        } else {
            // Bereken voor specifieke maand
            Map<String, Object> maandData = berekenMaandVoorspelling(maand, startJaar, huidigJaar);
            maandVoorspellingen.add(maandData);
        }

        voorspelling.put("maanden", maandVoorspellingen);
        voorspelling.put("aantalJarenData", Math.min(5, huidigJaar - startJaar));

        return voorspelling;
    }

    private Map<String, Object> berekenMaandVoorspelling(int maand, int startJaar, int huidigJaar) {
        Map<String, Object> result = new HashMap<>();
        List<Double> historischeData = new ArrayList<>();

        // Verzamel historische data voor deze maand
        for (int jaar = startJaar; jaar < huidigJaar; jaar++) {
            List<Maand> maandData = getMaandenVoorJaar(jaar);

            maandData.stream()
                    .filter(m -> m.getMaand() == maand)
                    .findFirst()
                    .ifPresent(m -> historischeData.add(m.getNeerslag()));
        }

        // Bereken statistieken
        double gemiddelde = historischeData.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Bereken trend
        double trend = berekenTrend(historischeData);
        double voorspeldeWaarde = gemiddelde + (trend * 1); // 1 jaar vooruit voor 2025
        voorspeldeWaarde = Math.max(0, voorspeldeWaarde);

        // Bepaal grenswaarde per maand (gebaseerd op seizoen)
        double grenswaarde = bepaalMaandGrenswaarde(maand);

        // Bereken trend percentage
        double trendPercentage = gemiddelde > 0 ? (trend / gemiddelde) * 100 : 0;
        String trendString = (trendPercentage >= 0 ? "+" : "") +
                Math.round(trendPercentage) + "%";

        // Maak result object
        result.put("maandNummer", maand);
        result.put("naam", getMaandNaam(maand));
        result.put("voorspelling", Math.round(voorspeldeWaarde * 10.0) / 10.0);
        result.put("gemiddelde", Math.round(gemiddelde * 10.0) / 10.0);
        result.put("trend", trendString);
        result.put("grenswaarde", grenswaarde);
        result.put("risico", voorspeldeWaarde > grenswaarde ? "GEVAAR!" : "OK");
        result.put("seizoen", getSeizoenVoorMaand(maand));

        return result;
    }

    private double bepaalMaandGrenswaarde(int maand) {
        // Grenswaarden per maand gebaseerd op seizoensnormen
        // Winter: 300mm/3 = 100mm per maand
        // Lente: 250mm/3 = 83mm per maand
        // Zomer: 260mm/3 = 87mm per maand
        // Herfst: 280mm/3 = 93mm per maand

        switch(maand) {
            case 12: case 1: case 2:  // Winter
                return 100.0;
            case 3: case 4: case 5:   // Lente
                return 83.0;
            case 6: case 7: case 8:   // Zomer
                return 87.0;
            case 9: case 10: case 11: // Herfst
                return 93.0;
            default:
                return 90.0;
        }
    }

    private String getMaandNaam(int maand) {
        String[] maandNamen = {
                "Januari", "Februari", "Maart", "April",
                "Mei", "Juni", "Juli", "Augustus",
                "September", "Oktober", "November", "December"
        };
        return (maand >= 1 && maand <= 12) ? maandNamen[maand - 1] : "Onbekend";
    }

    private String getSeizoenVoorMaand(int maand) {
        if (maand == 12 || maand == 1 || maand == 2) return "Winter";
        if (maand >= 3 && maand <= 5) return "Lente";
        if (maand >= 6 && maand <= 8) return "Zomer";
        if (maand >= 9 && maand <= 11) return "Herfst";
        return "";
    }

    private List<Map<String, Object>> berekenSeizoensTotalen(List<Map<String, Object>> maandVoorspellingen) {
        List<Map<String, Object>> seizoenen = new ArrayList<>();

        // Winter
        Map<String, Object> winter = new HashMap<>();
        winter.put("naam", "Winter");
        double winterTotaal = maandVoorspellingen.stream()
                .filter(m -> Arrays.asList(12, 1, 2).contains(m.get("maandNummer")))
                .mapToDouble(m -> (Double) m.get("voorspelling"))
                .sum();
        winter.put("totaal", Math.round(winterTotaal * 10.0) / 10.0);
        winter.put("grenswaarde", 300);
        winter.put("risico", winterTotaal > 300 ? "GEVAAR!" : "OK");
        seizoenen.add(winter);

        // Lente
        Map<String, Object> lente = new HashMap<>();
        lente.put("naam", "Lente");
        double lenteTotaal = maandVoorspellingen.stream()
                .filter(m -> Arrays.asList(3, 4, 5).contains(m.get("maandNummer")))
                .mapToDouble(m -> (Double) m.get("voorspelling"))
                .sum();
        lente.put("totaal", Math.round(lenteTotaal * 10.0) / 10.0);
        lente.put("grenswaarde", 250);
        lente.put("risico", lenteTotaal > 250 ? "GEVAAR!" : "OK");
        seizoenen.add(lente);

        // Zomer
        Map<String, Object> zomer = new HashMap<>();
        zomer.put("naam", "Zomer");
        double zomerTotaal = maandVoorspellingen.stream()
                .filter(m -> Arrays.asList(6, 7, 8).contains(m.get("maandNummer")))
                .mapToDouble(m -> (Double) m.get("voorspelling"))
                .sum();
        zomer.put("totaal", Math.round(zomerTotaal * 10.0) / 10.0);
        zomer.put("grenswaarde", 260);
        zomer.put("risico", zomerTotaal > 260 ? "GEVAAR!" : "OK");
        seizoenen.add(zomer);

        // Herfst
        Map<String, Object> herfst = new HashMap<>();
        herfst.put("naam", "Herfst");
        double herfstTotaal = maandVoorspellingen.stream()
                .filter(m -> Arrays.asList(9, 10, 11).contains(m.get("maandNummer")))
                .mapToDouble(m -> (Double) m.get("voorspelling"))
                .sum();
        herfst.put("totaal", Math.round(herfstTotaal * 10.0) / 10.0);
        herfst.put("grenswaarde", 280);
        herfst.put("risico", herfstTotaal > 280 ? "GEVAAR!" : "OK");
        seizoenen.add(herfst);

        return seizoenen;
    }

    private double berekenTrend(List<Double> data) {
        if (data.size() < 2) return 0.0;

        // Simpele lineaire trend berekening
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int n = data.size();

        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += data.get(i);
            sumXY += i * data.get(i);
            sumX2 += i * i;
        }

        // Bereken helling (trend per jaar)
        double helling = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

        return helling;
    }

    public List<Maand> getJaar(int jaar) {
        return dao.findByJaar(jaar);
    }

    public void save(Maand maand) {
        dao.save(maand);
    }

    // Nieuwe methode: haal maanden op voor een specifiek jaar
    public List<Maand> getMaandenVoorJaar(int jaar) {
        if (jaar >= 2024) {
            // Voor recente jaren, gebruik API data
            return getAPIDataAlsNeerslagList(jaar);
        } else {
            // Voor oudere jaren, gebruik database
            return dao.findByJaar(jaar);
        }
    }

    // Nieuwe methode: haal alle beschikbare jaren op
    public List<Integer> getBeschikbareJaren() {
        List<Integer> jaren = new ArrayList<>();

        // Haal jaren uit database
        List<Maand> alleMaanden = getAlle();
        Set<Integer> uniqueJaren = alleMaanden.stream()
                .map(Maand::getJaar)
                .collect(Collectors.toSet());

        jaren.addAll(uniqueJaren);

        // Voeg huidige jaar toe als het nog niet bestaat
        int huidigJaar = java.time.LocalDate.now().getYear();
        if (!jaren.contains(huidigJaar)) {
            jaren.add(huidigJaar);
        }

        // Sorteer van nieuw naar oud
        Collections.sort(jaren, Collections.reverseOrder());

        return jaren;
    }

    // Updated checkRisico methode met correcte keys
    public Map<String, Object> checkRisico(int jaar) {
        Map<String, Object> result = new HashMap<>();
        List<Maand> data;

        // Voor recente jaren (2024+): gebruik API data
        if (jaar >= 2024) {
            System.out.println("Ophalen van real-time API data voor " + jaar);
            data = getAPIDataAlsNeerslagList(jaar);
        } else {
            // Voor oudere jaren: gebruik database
            data = dao.findByJaar(jaar);
        }

        double winter = getMaanden(data, Arrays.asList(12, 1, 2));
        double lente = getMaanden(data, Arrays.asList(3, 4, 5));
        double zomer = getMaanden(data, Arrays.asList(6, 7, 8));
        double herfst = getMaanden(data, Arrays.asList(9, 10, 11));

        // Gebruik de juiste keys die de controller verwacht
        result.put("winter", winter);
        result.put("lente", lente);
        result.put("zomer", zomer);
        result.put("herfst", herfst);

        result.put("winterRisico", winter > 300 ? "GEVAAR!" : "OK");
        result.put("lenteRisico", lente > 250 ? "GEVAAR!" : "OK");
        result.put("zomerRisico", zomer > 260 ? "GEVAAR!" : "OK");
        result.put("herfstRisico", herfst > 280 ? "GEVAAR!" : "OK");

        return result;
    }

    private List<Maand> getAPIDataAlsNeerslagList(int jaar) {
        List<Maand> apiData = new ArrayList<>();
        Map<Integer, Double> jaarData = apiService.getJaarData(jaar);

        for (Map.Entry<Integer, Double> entry : jaarData.entrySet()) {
            apiData.add(new Maand(jaar, entry.getKey(), entry.getValue()));
        }

        return apiData;
    }

    private double getMaanden(List<Maand> data, List<Integer> maanden) {
        double totaal = 0;
        for (Maand n : data) {
            if (maanden.contains(n.getMaand())) {
                totaal += n.getNeerslag();
            }
        }
        return Math.round(totaal * 10.0) / 10.0; // Afronden op 1 decimaal
    }

    // Synchroniseer API data naar database
    public void syncAPIData(int jaar) {
        Map<Integer, Double> apiData = apiService.getJaarData(jaar);

        for (Map.Entry<Integer, Double> entry : apiData.entrySet()) {
            int maand = entry.getKey();
            double mm = entry.getValue();

            // Check of data al bestaat
            List<Maand> bestaande = dao.findByJaarAndMaand(jaar, maand);
            if (bestaande.isEmpty()) {
                // Nieuwe data toevoegen
                dao.save(new Maand(jaar, maand, mm));
                System.out.println("API data toegevoegd: " + jaar + "-" + maand + " = " + mm + "mm");
            } else {
                // Bestaande data updaten
                Maand bestaand = bestaande.get(0);
                bestaand.setNeerslag(mm);
                dao.save(bestaand);
                System.out.println("API data geupdate: " + jaar + "-" + maand + " = " + mm + "mm");
            }
        }
    }

    // Historische data laden
    public void laadData() {
        if (dao.count() > 0) return; // Al geladen

        // 2023 data
        save(new Maand(2023, 1, 61));
        save(new Maand(2023, 2, 54));
        save(new Maand(2023, 3, 68));
        save(new Maand(2023, 4, 60));
        save(new Maand(2023, 5, 87));
        save(new Maand(2023, 6, 71));
        save(new Maand(2023, 7, 93));
        save(new Maand(2023, 8, 77));
        save(new Maand(2023, 9, 68));
        save(new Maand(2023, 10, 100));
        save(new Maand(2023, 11, 100));
        save(new Maand(2023, 12, 105));

        // 2024 data
        save(new Maand(2024, 1, 61));
        save(new Maand(2024, 2, 58));
        save(new Maand(2024, 3, 66));
        save(new Maand(2024, 4, 61));
        save(new Maand(2024, 5, 75));
        save(new Maand(2024, 6, 77));
        save(new Maand(2024, 7, 101));
        save(new Maand(2024, 8, 88));
        save(new Maand(2024, 9, 60));
        save(new Maand(2024, 10, 96));
        save(new Maand(2024, 11, 97));
        save(new Maand(2024, 12, 114));
    }
}
