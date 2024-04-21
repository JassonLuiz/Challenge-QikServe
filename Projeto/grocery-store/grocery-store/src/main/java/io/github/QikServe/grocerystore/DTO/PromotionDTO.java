package io.github.QikServe.grocerystore.DTO;

import io.github.QikServe.grocerystore.entity.PromotionType;

public class PromotionDTO {
    private String id;
    private PromotionType type;
    private int requiredQty;

    private int freeQty;
    private double amount;

    public PromotionDTO(String id, PromotionType type, int requiredQty, int freeQty, int amount) {
        this.id = id;
        this.type = type;
        this.requiredQty = requiredQty;
        this.freeQty = freeQty;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
