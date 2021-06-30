package com.ipiecoles.communes.web.repository;

import com.ipiecoles.communes.web.model.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommuneRepository extends JpaRepository<Commune, String> {

    @Query("from Commune c")
    List<Commune> findAllCommunesDB();

    @Query("select count(distinct c.codePostal) from Commune c")
    long countDistinctCodePostal();

    @Query("select count(distinct c.nom) from Commune c")
    long countDistinctAllCommunes();

    List<Commune> findByLatitudeBetweenAndLongitudeBetween(Double latMin, Double latMax, Double longMin, Double longMax);





}
