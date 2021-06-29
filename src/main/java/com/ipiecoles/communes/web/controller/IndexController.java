package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    private CommuneRepository communeRepository;

    @GetMapping(value = "/")
    public String listeCommunes( @RequestParam(value = "page", defaultValue = "0") Integer page, // value n'est pas obligatoire
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(defaultValue = "codeInsee") String sortProperty,
                                 @RequestParam(defaultValue = "ASC") String sortDirection,
                                 final ModelMap model) {
        model.put("nbCommunes", communeRepository.count());
        //Affichage des communes de 1 à 10 => page = 0 et size = 10
        //Affichage des communes de 11 à 20 => page = 1 et size = 10
        //Affichage des communes de 41 à 60 => page = 2 et size = 20
        model.put("start", 1);//A remplacer par la valeur dynamique
        model.put("end", 10);//A remplacer par la valeur dynamique

        return "listeCommunes";
    }
}
