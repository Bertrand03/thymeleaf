package com.ipiecoles.communes.web.controller;

import com.ipiecoles.communes.web.model.Commune;
import com.ipiecoles.communes.web.repository.CommuneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller // A ne pas confondre avec le RestController
//@RequestMapping("/communes") // Pour éviter de remettre "communes" partout en debut d'url
public class CommuneController {

    private static final double DEGRE_LAT_KM = 111d;
    private static final double DEGRE_LONG_KM = 77d;

    @Autowired
    private CommuneRepository communeRepository;

    //Accessible uniquement aux utilisateurs connectés
    //@PreAuthorize("isAuthenticated()") // 2e solution de security - Sera à mettre à chaque méthode souhaitée
//    Accessible uniquement à un rôle particulier
    //@Secured({"ROLE_ADMIN", "ROLE_USER"})
    @GetMapping("/communes/{codeInsee}")
    public String getCommune(
            @PathVariable String codeInsee,
            @RequestParam(defaultValue = "10") Integer perimetre,
            final ModelMap model,
            RedirectAttributes attributes)
    {
        Optional<Commune> commune = communeRepository.findById(codeInsee);
        if(commune.isEmpty()){
            //Gère une exception
            throw new EntityNotFoundException("Impossible de trouver la commune de code INSEE " + codeInsee);
            //model.put("message", "Impossible de trouver la commune de code INSEE " + codeInsee); //Message d'erreur simple sans passer par une gestion d'erreur via GlobalExceptionHandler
            //return "error";//template error qui affiche un message d'erreur
        }
        //Récupérer les communes proches de celle-ci
        model.put("commune", commune.get());
        model.put("perimetre", perimetre);
        model.put("communesProches", this.findCommunesProches(commune.get(), perimetre, model));
        model.put("newCommune", false);

        return "detail";
    }

    @PostMapping(value = "/communes", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveNewCommune(@Valid Commune commune,
                                 //Juste après le paramètre marqué @Valid
                                 final BindingResult result,
                                 RedirectAttributes attributes,
                                 final ModelMap model){
        //Ajouter un certain nombre de contrôles...
        //S'il n'y a pas d'erreurs de validation sur le paramètre commune
        if (!result.hasErrors()) {
            commune = communeRepository.save(commune);
            model.put("commune", commune);
            attributes.addFlashAttribute("type", "success"); //attributes.addFlashAttribute quand on est redirigés vers une autre page
            attributes.addFlashAttribute("message", "Enregistrement de la commune " + commune.getNom() + " effectuée !");
            return "redirect:/communes/" + commune.getCodeInsee();
        }
        //S'il y a des erreurs...
        //Possibilité 1 : Rediriger l'utilisateur vers la page générique d'erreur
        //Possibilité 2 : Laisse sur la même page en affichant les erreurs pour chaque champ
        model.addAttribute("type", "danger"); //model.addAttribute quand on reste sur la même page
        model.addAttribute("message", "Erreur lors de la sauvegarde de la commune. Veuillez vérifier les champs en rouge");
        model.put("newCommune", true);
        return "detail";
    }

    @PostMapping(value = "/communes/{codeInsee}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String saveExistingCommune(
            @Valid Commune commune,
            //Juste après le paramètre marqué @Valid
            final BindingResult result,
            @PathVariable String codeInsee,
            final ModelMap model,
            RedirectAttributes attributes){
        //Ajouter un certain nombre de contrôles...
        // S'il n'y a pas d'erreur de validation sur le paramètre commune
        if (!result.hasErrors()) {
            commune = communeRepository.save(commune);

            if (commune.getCodeInsee().isEmpty()) {
                throw new EntityNotFoundException("Le code INSEE de la commune est obligatoire");
            }

            attributes.addFlashAttribute("type", "success");
            attributes.addFlashAttribute("message", "Modification de la commune " + commune.getNom() + " effectuée !");
            return "redirect:/communes/" + commune.getCodeInsee();
        }
        //S'il y a des erreurs...
        //Possibilité 1 : Rediriger l'utilisateur vers la page générique d'erreur
        //Possibilité 2 : Laisse sur la même page en affichant les erreurs pour chaque champ
        model.addAttribute("type", "danger");
        model.addAttribute("message", "Erreur lors de la sauvegarde de la commune");
        // TODO - Utiliser les getFieldsError pour préciser les erreurs
        return "detail";
    }

    @GetMapping("/communes/new")
    public String newCommune(
            final ModelMap model){
        model.put("commune", new Commune());
        model.put("newCommune", true);
        return "detail";
    }

    @GetMapping("/communes/{codeInsee}/delete")
    public String deleteCommune(
            @PathVariable String codeInsee,
            RedirectAttributes attributes)
    {

        Commune commune = communeRepository.findCommuneByCodeInsee(codeInsee);
        communeRepository.deleteById(codeInsee);
        attributes.addFlashAttribute("type", "success");
        attributes.addFlashAttribute("message", "Suppression de la commune " + commune.getNom() + " effectuée");
        return "redirect:/";
    }

    /**
     * Récupère une liste des communes dans un périmètre autour d'une commune
     * @param commune La commune sur laquelle porte la recherche
     * @param perimetreEnKm Le périmètre de recherche en kilomètre
     * @return La liste des communes triées de la plus proche à la plus lointaine
     */
    private List<Commune> findCommunesProches(Commune commune, Integer perimetreEnKm, ModelMap model) {
        //Si le périmètre est valide on affiche le résultat de la méthode
        if (perimetreEnKm <= 20) {
            Double latMin, latMax, longMin, longMax, degreLat, degreLong;
            //1 degré latitude = 111km, 1 degré longitude = 77km
            degreLat = perimetreEnKm/DEGRE_LAT_KM;
            degreLong = perimetreEnKm/DEGRE_LONG_KM;
            latMin = commune.getLatitude() - degreLat;
            latMax = commune.getLatitude() + degreLat;
            longMin = commune.getLongitude() - degreLong;
            longMax = commune.getLongitude() + degreLong;
            List<Commune> communesProches = communeRepository.findByLatitudeBetweenAndLongitudeBetween(latMin, latMax, longMin, longMax);
            return communesProches.stream().
                    filter(commune1 -> !commune1.getNom().equals(commune.getNom()) && commune1.getDistance(commune.getLatitude(), commune.getLongitude()) <= perimetreEnKm).
                    sorted(Comparator.comparing(o -> o.getDistance(commune.getLatitude(), commune.getLongitude()))).
                    collect(Collectors.toList());

        } else {
            model.addAttribute("type", "danger");
            model.addAttribute("message", "Le périmètre de recherche ne peut pas dépasser les 20 km");
            return null;
        }

    }

}
