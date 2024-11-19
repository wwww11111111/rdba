package com.example.xddd.entities;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "items")
public class Item {
    @Id
    private Long id;
    private int number;
    private int categoryId;
    private String description;
    private Long price;

    public Item() {
    }

    public Item(Long id, int number, int categoryId, String description, Long price) {
        this.id = id;
        this.number = number;
        this.categoryId = categoryId;
        this.description = description;
        this.price = price;
    }

}