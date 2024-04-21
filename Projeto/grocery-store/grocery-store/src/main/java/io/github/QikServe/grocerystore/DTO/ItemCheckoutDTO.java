package io.github.QikServe.grocerystore.DTO;

import io.github.QikServe.grocerystore.entity.Product;

import java.math.BigDecimal;

public class ItemCheckoutDTO {
    private Product product;

    private BigDecimal quantity;
    private BigDecimal itemTotal;
    private BigDecimal savings;

    public ItemCheckoutDTO(Product product, BigDecimal quantity, BigDecimal itemTotal, BigDecimal savings) {
        this.product = product;
        this.quantity = quantity;
        this.itemTotal = itemTotal;
        this.savings = savings;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setTotalAmount(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    public BigDecimal getSavings() {
        return savings;
    }

    public void setSavings(BigDecimal savings) {
        this.savings = savings;
    }
}
