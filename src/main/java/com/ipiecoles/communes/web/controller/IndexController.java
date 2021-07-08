package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
public class IndexController {

    @Autowired
    private CommuneRepository communeRepository;

    @GetMapping(value = "/")
    public String listeCommunes( @RequestParam(value = "page", defaultValue = "0") Integer page, // value n'est pas obligatoire
                                 @RequestParam(defaultValue = "10") Integer size,
                                 @RequestParam(defaultValue = "codeInsee") String sortProperty,
                                 @RequestParam(defaultValue = "ASC") String sortDirection,
                                 @RequestParam(required = false) String search,
                                 final ModelMap model) {

        model.put("nbCommunes", communeRepository.count());
        //Constituer un PageRequest
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.fromString(sortDirection), sortProperty);
        Page<Commune> communes;
        Commune communeCodeInsee = new Commune();
        if(search == null || search.isEmpty()){
            //Appeler findAll si search est null
            communes = communeRepository.findAll(pageRequest);
        } else {
            //Appeler findByNomContainingIgnoreCase si search n'est pas null
            if (search.length() == 5){
                communeCodeInsee = communeRepository.findCommuneByCodeInsee(search);

            }
            communes = communeRepository.findByNomContainingIgnoreCase(search, pageRequest);

        }
        model.put("communes", communes);
        model.put("nbCommunes", communes.getTotalElements());
//        model.put("pageSizes", Arrays.asList("5", "10", "20", "50", "100"));
        model.put("pageSizes", Arrays.asList(5, 10, 20, 50, 100));
        //Affichage des communes de 1 à 10 => page = 0 et size = 10
        //Affichage des communes de 11 à 20 => page = 1 et size = 10
        //Affichage des communes de 41 à 60 => page = 2 et size = 20
        model.put("start", 1);//A remplacer par la valeur dynamique
        model.put("end", 10);//A remplacer par la valeur dynamique
        model.put("page", page);
        model.put("size", size);
        model.put("communeCodeInsee", communeCodeInsee);


        model.put("template", "listeCommunes");
        model.put("fragment", "listCom");

        return "main";
//        return "listeCommunes";
    }
}
