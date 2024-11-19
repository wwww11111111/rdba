package com.example.xddd.services;


import com.example.xddd.entities.User;
import com.example.xddd.repositories.UserRepository;
import com.example.xddd.security.UserDetailsClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserDetailsServiceClass implements UserDetailsService {
//    @Autowired
//    UserRepositoryXmlImpl xmlrepo;

    @Autowired
    UserRepository userRepository;



    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByLogin(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }

        return UserDetailsClass.build(user.get());
    }
}
