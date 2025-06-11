package be.ehb.aquafin.controller;

import be.ehb.aquafin.model.MaandDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private MaandDAO maandDAO;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("maandData", maandDAO.findAll());
        return "dashboard";
    }
}