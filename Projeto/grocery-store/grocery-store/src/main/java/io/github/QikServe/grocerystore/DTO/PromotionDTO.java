package io.github.QikServe.grocerystore.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.QikServe.grocerystore.entity.PromotionType;

public class PromotionDTO {
    private String id;
    private PromotionType type;

    @JsonProperty("required_qty")
    private int requiredQty;

    @JsonProperty("free_qty")
    private int freeQty;
    private double price;

    private int amount;

    public PromotionDTO(String id, PromotionType type, int requiredQty, int freeQty, double price, int amount) {
        this.id = id;
        this.type = type;
        this.requiredQty = requiredQty;
        this.freeQty = freeQty;
        this.price = price;
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PromotionType getType() {
        return type;
    }

    public void setType(PromotionType type) {
        this.type = type;
    }

    public int getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(int requiredQty) {
        this.requiredQty = requiredQty;
    }

    public int getFreeQty() {
        return freeQty;
    }

    public void setFreeQty(int freeQty) {
        this.freeQty = freeQty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
