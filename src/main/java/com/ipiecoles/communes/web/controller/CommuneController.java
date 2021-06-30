package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller // A ne pas confondre avec le RestController
public class CommuneController {

    private static final double DEGRE_LAT_KM = 111d;
    private static final double DEGRE_LONG_KM = 77d;

    @Autowired
    private CommuneRepository communeRepository;

    @GetMapping("/communes/{codeInsee}")
    public String getCommune(
            @PathVariable String codeInsee,
            final ModelMap model)
    {
        Optional<Commune> commune = communeRepository.findById(codeInsee);
        model.put("commune", commune.get());
        return "detail";
    }

    @PostMapping(value = "/communes", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveNewCommune(Commune commune, final ModelMap model){
        //Ajouter un certain nombre de contrôles...
        commune = communeRepository.save(commune);
        model.put("commune", commune);
        return "detail";
    }

    @PostMapping(value = "/communes/{codeInsee}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveExistingCommune(
            Commune commune,
            @PathVariable String codeInsee,
            final ModelMap model){
        //Ajouter un certain nombre de contrôles...
        commune = communeRepository.save(commune);
        model.put("commune", commune);
        return "detail";
    }

    @GetMapping("/communes/new")
    public String newCommune(final ModelMap model){
        model.put("commune", new Commune());
        model.put("newCommune", true);
        return "detail";
    }

    /**
     * Récupère une liste des communes dans un périmètre autour d'une commune
     * @param commune La commune sur laquelle porte la recherche
     * @param perimetreEnKm Le périmètre de recherche en kilomètre
     * @return La liste des communes triées de la plus proche à la plus lointaine
     */
    private List<Commune> findCommunesProches(Commune commune, Integer perimetreEnKm) {
        Double latMin, latMax, longMin, longMax, degreLat, degreLong;
        //1 degré latitude = 111km, 1 degré longitude = 77km
        degreLat = perimetreEnKm/DEGRE_LAT_KM;
        degreLong = perimetreEnKm/DEGRE_LONG_KM;
        latMin = commune.getLatitude() - degreLat;
        latMax = commune.getLatitude() + degreLat;
        longMin = commune.getLongitude() - degreLong;
        longMax = commune.getLongitude() + degreLong;
        List<Commune> communesProches = communeRepository.findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, longMin, longMax);
        ;
        return communesProches.stream().
                filter(commune1 -> !commune1.getNom().equals(commune.getNom()) && commune1.getDistance(commune.getLatitude(), commune.getLongitude()) <= perimetreEnKm).
                sorted(Comparator.comparing(o -> o.getDistance(commune.getLatitude(), commune.getLongitude()))).
                collect(Collectors.toList());
    }

}
