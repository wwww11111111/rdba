package com.example.xddd.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    private LocalDate date;
    private String region;
    private String cityStreetHouse;
    private int apartment;
    private int floor;
    private boolean lift;
    private String phoneNumber;
    private boolean terminalPayment;
    private boolean furnitureAssembly;
    private String status;
}
