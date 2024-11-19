package com.example.xddd.entities;

import javax.persistence.*;

@Entity
@Table(name = "reserved_items")
public class Cart {

    @Id
    @GeneratedValue
    private long id;
    private String ownerLogin;
    private long itemId;
    private int itemNumber;
    private String status;
    private Long orderId;

    public Cart(String login, long item_id, int number, String status, Long orderId) {
        this.ownerLogin = login;
        this.itemId = item_id;
        this.itemNumber = number;
        this.status = status;
        this.orderId = orderId;
    }

    public Cart() {

    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getId() {
        return id;
    }


    public String getOwnerLogin() {
        return ownerLogin;
    }

    public void setOwnerLogin(String login) {
        this.ownerLogin = login;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(int item_id) {
        this.itemId = item_id;
    }

    public int getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }
}
