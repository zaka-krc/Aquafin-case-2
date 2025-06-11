package be.ehb.aquafin.Service;

import be.ehb.aquafin.model.Maand;
import be.ehb.aquafin.model.MaandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NeerslagService {

    @Autowired
    private MaandDAO dao;

    public List<Maand> getAlle() {
        return (List<Maand>) dao.findAll();
    }

    public List<Maand> getJaar(int jaar) {
        return dao.findByJaar(jaar);
    }

    public void save(Maand maand) {
        dao.save(maand);
    }

    // Seizoen check - simpel
    public Map<String, Object> checkRisico(int jaar) {
        Map<String, Object> result = new HashMap<>();
        List<Maand> data = dao.findByJaar(jaar);

        double winter = getMaanden(data, Arrays.asList(12, 1, 2)); // Dec, Jan, Feb
        double lente = getMaanden(data, Arrays.asList(3, 4, 5));   // Mrt, Apr, Mei
        double zomer = getMaanden(data, Arrays.asList(6, 7, 8));   // Jun, Jul, Aug
        double herfst = getMaanden(data, Arrays.asList(9, 10, 11)); // Sep, Okt, Nov

        result.put("winter", winter > 300 ? "RISICO" : "OK");
        result.put("lente", lente > 250 ? "RISICO" : "OK");
        result.put("zomer", zomer > 260 ? "RISICO" : "OK");
        result.put("herfst", herfst > 280 ? "RISICO" : "OK");

        result.put("winterMm", winter);
        result.put("lenteMm", lente);
        result.put("zomerMm", zomer);
        result.put("herfstMm", herfst);

        return result;
    }

    private double getMaanden(List<Maand> data, List<Integer> maanden) {
        double totaal = 0;
        for (Maand m : data) {
            if (maanden.contains(m.getMaand())) {
                totaal += m.getNeerslag();
            }
        }
        return totaal;
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
