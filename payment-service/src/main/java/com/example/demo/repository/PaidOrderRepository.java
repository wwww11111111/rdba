package com.example.demo.repository;

import com.example.demo.entities.PaidOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaidOrderRepository extends JpaRepository<PaidOrder, Long> {
}
