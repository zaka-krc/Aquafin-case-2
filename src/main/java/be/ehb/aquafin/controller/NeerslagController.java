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

    @GetMapping("/dashboard")
    public String home(Model model) {
        service.laadData();

        Map<String, Object> risico2024 = service.checkRisico(2024);
        model.addAttribute("risico", risico2024);
        model.addAttribute("jaar", 2024);

        return "dashboard";
    }

    @GetMapping("/jaar/{jaar}")
    public String jaar(@PathVariable int jaar, Model model) {
        Map<String, Object> risico = service.checkRisico(jaar);
        model.addAttribute("risico", risico);
        model.addAttribute("jaar", jaar);

        return "dasboard";
    }

    @GetMapping("/voorspelling")
    public String voorspelling() {
        return "voorspelling";
    }

    @PostMapping("/voorspelling")
    public String berekenVoorspelling(@RequestParam int jaar,
                                      @RequestParam(required = false) Double jan,
                                      @RequestParam(required = false) Double feb,
                                      @RequestParam(required = false) Double mrt,
                                      @RequestParam(required = false) Double apr,
                                      @RequestParam(required = false) Double mei,
                                      @RequestParam(required = false) Double jun,
                                      @RequestParam(required = false) Double jul,
                                      @RequestParam(required = false) Double aug,
                                      @RequestParam(required = false) Double sep,
                                      @RequestParam(required = false) Double okt,
                                      @RequestParam(required = false) Double nov,
                                      @RequestParam(required = false) Double dec,
                                      Model model) {

        // Voorspelling berekenen
        double winter = nul(dec) + nul(jan) + nul(feb);
        double lente = nul(mrt) + nul(apr) + nul(mei);
        double zomer = nul(jun) + nul(jul) + nul(aug);
        double herfst = nul(sep) + nul(okt) + nul(nov);

        String winterRisico = winter > 300 ? "GEVAAR!" : "OK";
        String lenteRisico = lente > 250 ? "GEVAAR!" : "OK";
        String zomerRisico = zomer > 260 ? "GEVAAR!" : "OK";
        String herfstRisico = herfst > 280 ? "GEVAAR!" : "OK";

        model.addAttribute("jaar", jaar);
        model.addAttribute("winter", winter);
        model.addAttribute("lente", lente);
        model.addAttribute("zomer", zomer);
        model.addAttribute("herfst", herfst);
        model.addAttribute("winterRisico", winterRisico);
        model.addAttribute("lenteRisico", lenteRisico);
        model.addAttribute("zomerRisico", zomerRisico);
        model.addAttribute("herfstRisico", herfstRisico);

        return "voorspelling";
    }

    private double nul(Double waarde) {
        return waarde == null ? 0.0 : waarde;
    }

    @GetMapping("/data")
    public String data(Model model) {
        model.addAttribute("alle", service.getAlle());
        return "data";
    }

    @PostMapping("/data")
    public String voegToe(@RequestParam int jaar,
                          @RequestParam int maand,
                          @RequestParam double mm) {
        service.save(new Maand(jaar, maand, mm));
        return "redirect:/data";
    }
}
