package com.github.ArtemAndrew.PriceMonitoringBot.entity;

import java.util.Objects;


public final class Product {
    private final Long id;
    private final String name;
    private final Long price;

    public Product(Long productID, String productName, Long productPrice) {
        this.id = productID;
        this.name = productName;
        this.price = productPrice;
    }

    public Long productID() {
        return id;
    }

    public String productName() {
        return name;
    }

    public Long productPrice() {
        return price;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Product) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price);
    }

    @Override
    public String toString() {
        return "Product[" +
                "productID=" + id + ", " +
                "productName=" + name + ", " +
                "productPrice=" + price + ", " + ']';
    }

}
