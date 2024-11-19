package com.example.xddd.repositories;

import com.example.xddd.entities.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsRepository extends CrudRepository<Item, Long> {
    List<Item> findByCategoryId(Integer categoryId);
}