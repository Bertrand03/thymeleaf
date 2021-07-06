package com.ipiecoles.communes.web.service;

import com.ipiecoles.communes.web.model.Role;
import com.ipiecoles.communes.web.model.User;
import com.ipiecoles.communes.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MyUserDetailsService implements UserDetailsService { // Pour pouvoir utiliser l'implementation de UserDetailsService il faut récupérer toutes les
//    méthodes de ce Service

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Récupérer en base l'utilisateur correspondant au nom passé en paramètre
        User user = userRepository.findByUserName(username);

        // Lever une exception si l'utilisateur n'est pas trouvé !
        if (user == null) {
            throw new UsernameNotFoundException(("Aucun utilisateur nommé" + username + "n'a été trouvé"));
        }
//        Construire un UserDetails à partir de l'utilisateur récupéré
        return buildSpringUserFromMyUser(user);
    }

    private UserDetails buildSpringUserFromMyUser(User user) {
        //Initialise la liste des droits de l'utilisateur à partir de la liste
        //des rôles présents en BDD pour cet utilisateur
        Set<GrantedAuthority> authorities = new HashSet<>();
        for(Role role : user.getRoles()){
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        //Instanciation du User Spring à partir des infos du user de la BDD
        org.springframework.security.core.userdetails.User springUser =
                new org.springframework.security.core.userdetails.User(user.getUserName(), user.getPassword(), authorities);
        return springUser;
    }
}
