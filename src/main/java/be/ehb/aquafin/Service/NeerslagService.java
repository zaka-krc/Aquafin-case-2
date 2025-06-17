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
