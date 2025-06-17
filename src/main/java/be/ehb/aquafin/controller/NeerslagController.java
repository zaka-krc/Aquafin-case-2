package be.ehb.aquafin.controller;

import be.ehb.aquafin.Service.NeerslagService;
import be.ehb.aquafin.model.Maand;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@Controller
public class NeerslagController {

    @Autowired
    private NeerslagService service;

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String home(Model model) {
        service.laadData();

        // Gebruik huidige jaar of 2024, welke eerder is
        int jaar = Math.min(2024, java.time.LocalDate.now().getYear());

        // Haal risico data op
        Map<String, Object> risico = service.checkRisico(jaar);
        model.addAttribute("risico", risico);
        model.addAttribute("jaar", jaar);

        // Haal alle maandelijkse data op voor 2024
        List<Maand> maanden2024 = service.getMaandenVoorJaar(2024);
        model.addAttribute("maanden", maanden2024);

        // Haal alle beschikbare jaren op
        List<Integer> beschikbareJaren = service.getBeschikbareJaren();
        model.addAttribute("beschikbareJaren", beschikbareJaren);

        return "dashboard";
    }

    @GetMapping("/jaar/{jaar}")
    public String jaar(@PathVariable int jaar, Model model) {
        Map<String, Object> risico = service.checkRisico(jaar);
        model.addAttribute("risico", risico);
        model.addAttribute("jaar", jaar);

        // Haal maandelijkse data op voor specifiek jaar
        List<Maand> maanden = service.getMaandenVoorJaar(jaar);
        model.addAttribute("maanden", maanden);

        // Haal alle beschikbare jaren op
        List<Integer> beschikbareJaren = service.getBeschikbareJaren();
        model.addAttribute("beschikbareJaren", beschikbareJaren);

        return "dashboard";
    }

    @GetMapping("/voorspelling")
    public String voorspelling(@RequestParam(required = false) Integer maand,
                               Model model) {

        // Default: toon alle maanden
        if (maand == null) maand = 0;

        model.addAttribute("selectedMaand", maand);

        // Genereer voorspelling voor 2025
        Map<String, Object> voorspelling = service.genereerMaandVoorspelling(2025, maand);
        model.addAttribute("voorspelling", voorspelling);

        return "voorspelling";
    }

    @GetMapping("/data")
    public String data(Model model) {
        List<Maand> alleMaanden = service.getAlle();
        model.addAttribute("alle", alleMaanden);

        // Voeg statistieken toe
        model.addAttribute("totaalRecords", alleMaanden.size());

        return "data";
    }

    @PostMapping("/data")
    public String voegToe(@RequestParam int jaar,
                          @RequestParam int maand,
                          @RequestParam double neerslag) {
        service.save(new Maand(jaar, maand, neerslag));
        return "redirect:/data";
    }

    @PostMapping("/sync/{jaar}")
    @ResponseBody
    public String syncApiData(@PathVariable int jaar) {
        try {
            service.syncAPIData(jaar);
            return "Sync voltooid voor jaar " + jaar;
        } catch (Exception e) {
            return "Fout bij synchroniseren: " + e.getMessage();
        }
    }
}
