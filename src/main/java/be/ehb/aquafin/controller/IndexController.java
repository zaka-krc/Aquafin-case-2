package be.ehb.aquafin.controller;

import be.ehb.aquafin.model.Maand;
import be.ehb.aquafin.model.MaandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private MaandDAO neerslagRepo;

    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String showIndex(ModelMap map) {

        // Haal alle data op
        List<Maand> alleData = neerslagRepo.findAllByOrderByJaarDescMaandAsc();

        // Bereken seizoenstotalen
        Map<String, Double> seizoenTotalen = new HashMap<>();

        for (Maand m : alleData) {
            String key = m.getJaar() + " " + m.getSeizoen();
            seizoenTotalen.merge(key, m.getNeerslag(), Double::sum);
        }

        map.addAttribute("seizoen_totalen", seizoenTotalen);

        return "index";
    }
}
