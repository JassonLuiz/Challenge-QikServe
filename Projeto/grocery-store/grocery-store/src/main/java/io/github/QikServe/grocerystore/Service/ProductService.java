package io.github.QikServe.grocerystore.Service;

import io.github.QikServe.grocerystore.DTO.CartItemDTO;
import io.github.QikServe.grocerystore.DTO.ItemCheckoutDTO;
import io.github.QikServe.grocerystore.DTO.PromotionDTO;
import io.github.QikServe.grocerystore.Exception.ResourceNotFoundException;
import io.github.QikServe.grocerystore.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Value("${wiremock.url}")
    private String wireMockUrl;

    private RestTemplate restTemplate;

    @Autowired
    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Executes a GET or POST request using the RestTemplate to obtain an object of the specified type.
     * @param url The URL to which the request will be sent.
     * @param responseType The class that represents the expected type of the response.
     * @return The type of the response object.
     * @param <T> The response object obtained after the request.
     */
    private <T> T executeRestTemplate(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }


    /**
     * Get all service products.
     *
     * @return An array of products obtained from the service.
     */
    public Product[] getAllProducts() {
        Product[] products;
        try {
            products = executeRestTemplate(wireMockUrl + "/products", Product[].class);
        } catch (HttpClientErrorException.NotFound e) {
            products = new Product[0];
        }
        return products;
    }

    /**
     * Gets a single product by its service ID.
     * @param id The ID of the product to get.
     * @return The product corresponding to the specified ID.
     */
    public Product getOneProduct(String id) {
        Product product;
        try {
            product = executeRestTemplate(wireMockUrl + "/products/" + id, Product.class);
        } catch (HttpClientErrorException.NotFound e) {
            product = null;
        }
        return product;
    }

    /**
     * Add items to your shopping cart and calculate the total value and savings for each item.
     * @param items A list of items to add to the shopping cart.
     * @return A list of ItemCheckoutDTO objects containing details of each item added to the cart.
     */
    public List<ItemCheckoutDTO> addToCart(List<CartItemDTO> items) {
        List<ItemCheckoutDTO> itemCheckouts = new ArrayList<>();

        for (CartItemDTO item : items) {
            Product product = executeRestTemplate(wireMockUrl + "/products/" + item.getProductId(), Product.class);
            if (product == null) {
                throw new ResourceNotFoundException("Product is not found for this ID!");
            }

            BigDecimal quantity = new BigDecimal(item.getQuantity());

            BigDecimal productPrice = new BigDecimal(product.getPrice());
            BigDecimal itemTotal = productPrice.multiply(quantity);
            BigDecimal itemSavings = BigDecimal.ZERO;

            for (PromotionDTO promotion : product.getPromotions()) {
                BigDecimal[] result = applyPromotion(itemTotal, productPrice, quantity, promotion, itemSavings);
                itemTotal = result[0];
                itemSavings = result[1];
            }

            ItemCheckoutDTO dto = new ItemCheckoutDTO(product, quantity, itemTotal, itemSavings);
            itemCheckouts.add(dto);
        }

        return itemCheckouts;
    }

    /**
     * Applies a promotion to the total price of the item and calculates the associated savings, based on the type of promotion provided.
     * @param itemTotal The total value of the item before discount.
     * @param productPrice The unit value of the product.
     * @param quantity The quantity of the product.
     * @param promotion The promotion to be applied.
     * @param itemSavings The total savings accumulated to date.
     * @return An array containing the item's new total price after discounting and the updated savings total.
     * @throws ResourceNotFoundException If the promotion type provided is not supported.
     */
    private BigDecimal[] applyPromotion(BigDecimal itemTotal, BigDecimal productPrice, BigDecimal quantity, PromotionDTO promotion, BigDecimal itemSavings){
        return switch (promotion.getType()) {
            case FLAT_PERCENT -> applyFlatPercentDiscount(itemTotal, productPrice, quantity, promotion.getAmount(), itemSavings);
            case BUY_X_GET_Y_FREE -> applyBuyXGetYFreeDiscount(itemTotal, productPrice, quantity, promotion.getRequiredQty(), itemSavings);
            case QTY_BASED_PRICE_OVERRIDE -> applyQtyBasedPriceOverrideDiscount(itemTotal, productPrice, quantity, promotion.getRequiredQty(), promotion.getPrice(), itemSavings);
            default -> throw new ResourceNotFoundException("Promotion type does not exist");
        };
    }

    /**
     * Applies a fixed percentage discount to the total value of the item.
     * @param itemTotal The total value of the item before discount.
     * @param productValue The unit value of the product.
     * @param quantity The quantity of the product.
     * @param discountPercent The discount percentage to be applied.
     * @param itemSavings The total savings accumulated to date.
     * @return An array containing the new total value of the item after the discount and the updated savings total.
     */
    private BigDecimal[] applyFlatPercentDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, double discountPercent, BigDecimal itemSavings) {
        BigDecimal discountAmount = productValue.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
        BigDecimal totalDiscount = discountAmount.multiply(quantity);

        itemTotal = itemTotal.subtract(totalDiscount);
        itemSavings = itemSavings.add(totalDiscount);

        return new BigDecimal[]{itemTotal, itemSavings};
    }

    /**
     * Applies a "buy X, get Y free" discount to the total value of the item.
     * @param itemTotal The total value of the item before discount.
     * @param productValue The unit value of the product.
     * @param quantity The quantity of the product.
     * @param requiredQty The quantity required to activate the discount.
     * @param itemSavings The total savings accumulated to date.
     * @return An array containing the new total value of the item after the discount and the updated savings total.
     */
    private BigDecimal[] applyBuyXGetYFreeDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, int requiredQty, BigDecimal itemSavings) {
        BigDecimal qtFree = quantity.divide(new BigDecimal(requiredQty), 0, RoundingMode.DOWN);
        BigDecimal totalFreeItemsValue = productValue.multiply(qtFree);

        itemTotal = itemTotal.subtract(totalFreeItemsValue);
        itemSavings = itemSavings.add(totalFreeItemsValue);

        return new BigDecimal[]{itemTotal, itemSavings};
    }

    /**
     *
     * @param itemTotal The total value of the item before discount.
     * @param productValue The unit value of the product.
     * @param quantity The quantity of the product.
     * @param requiredQty The quantity required to activate the discount.
     * @param promoPrice  The promotional price to be applied when the minimum quantity is reached.
     * @param itemSavings The total savings accumulated to date.
     * @return An array containing the new total value of the item after the discount and the updated savings total.
     */
    private BigDecimal[] applyQtyBasedPriceOverrideDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, int requiredQty, double promoPrice, BigDecimal itemSavings) {
        int qtPromo = quantity.intValue() / requiredQty;
        int qtNonPromo = quantity.intValue() % requiredQty;

        BigDecimal totalDiscountedPrice = new BigDecimal(qtPromo).multiply(BigDecimal.valueOf(promoPrice));

        if (qtNonPromo > 0) {
            totalDiscountedPrice = totalDiscountedPrice.add(productValue);
        }

        itemSavings = itemSavings.add(itemTotal.subtract(totalDiscountedPrice));
        itemTotal = itemTotal.subtract(itemSavings);

        return new BigDecimal[]{itemTotal, itemSavings};
    }
}
