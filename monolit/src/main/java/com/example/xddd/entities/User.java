package com.example.xddd.entities;

import com.example.xddd.security.Role;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "login")
        })
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "password")
    private String password;

    @Column(name = "balance")
    private Long balance;


//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @ManyToMany
    @JoinTable(name = "userroles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> role = new ArrayList<>();


    public User() {

    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

}