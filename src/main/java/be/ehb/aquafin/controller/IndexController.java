package be.ehb.aquafin.controller;

import be.ehb.aquafin.model.Maand;
import be.ehb.aquafin.model.MaandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private MaandDAO neerslagDAO;

    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String toonOverzicht(ModelMap model,
                                @RequestParam(value = "jaar", required = false, defaultValue = "0") int jaar) {

        List<Maand> data;
        if (jaar > 0) {
            data = neerslagDAO.findByJaar(jaar);
        } else {
            data = neerslagDAO.findByJaarAndMaand();
        }

        // Bereken seizoentotalen en check risico
        Map<String, Double> seizoenTotalen = new HashMap<>();
        Map<String, Boolean> seizoenRisicos = new HashMap<>();

        for (Maand nm : data) {
            String key = nm.getJaar() + " - " + nm.getSeizoen();
            seizoenTotalen.merge(key, nm.getNeerslag(), Double::sum);
        }

        // Check risico's
        for (Map.Entry<String, Double> entry : seizoenTotalen.entrySet()) {
            String seizoen = entry.getKey().split(" - ")[1];
            double totaal = entry.getValue();
            boolean risico = checkRisico(seizoen, totaal);
            seizoenRisicos.put(entry.getKey(), risico);
        }

        model.addAttribute("seizoen_data", seizoenTotalen);
        model.addAttribute("seizoen_risico", seizoenRisicos);
        model.addAttribute("geselecteerd_jaar", jaar);

        return "index";
    }

    private boolean checkRisico(String seizoen, double totaal) {
        switch (seizoen) {
            case "Winter": return totaal > 300;
            case "Lente": return totaal > 250;
            case "Zomer": return totaal > 260;
            case "Herfst": return totaal > 280;
            default: return false;
        }
    }
}
