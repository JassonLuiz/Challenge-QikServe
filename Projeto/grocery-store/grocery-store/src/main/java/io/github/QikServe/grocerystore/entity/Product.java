package io.github.QikServe.grocerystore.entity;

import io.github.QikServe.grocerystore.DTO.PromotionDTO;

import java.util.List;

public class Product {
    private String id;
    private String name;
    private int price;
    private List<PromotionDTO> promotions;

    public Product(String id, String name, int price, List<PromotionDTO> promotions) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.promotions = promotions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<PromotionDTO> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<PromotionDTO> promotions) {
        this.promotions = promotions;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", promotions=" + promotions +
                '}';
    }
}
