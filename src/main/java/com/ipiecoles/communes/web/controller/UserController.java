package com.ipiecoles.communes.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @GetMapping("/login")
    public String login(final ModelMap model,
                        RedirectAttributes attributes
                        ){
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Connexion réussie");
        return "login";
    }

    @GetMapping("/successfulConnection")
    public String msgAfterLogin(RedirectAttributes attributes) {
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Connexion réussie");
        return "redirect:/";
    }

    @GetMapping("/errorConnection")
    public String msgErrorLogin(RedirectAttributes attributes) {
        attributes.addFlashAttribute("type", "danger");
        attributes.addFlashAttribute("message", "Connexion a échoué");
        return "redirect:/login";
    }


}
