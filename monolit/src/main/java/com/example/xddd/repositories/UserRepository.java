package com.example.xddd.repositories;

import com.example.xddd.entities.User;
import com.example.xddd.security.ERole;
import com.example.xddd.security.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

    @Repository
    public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByLogin(String login);

        Optional<User> findById(Long id);

        default Optional<Role> findRoleByLogin(final String login) {
            final var user = findByLogin(login);
            return user.map(user1 -> user1.getRole().iterator().next());
        }

        Boolean existsByLogin(String login);

        List<User> findAllByRoleName(ERole role);

    }