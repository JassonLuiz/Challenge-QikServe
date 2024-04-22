package io.github.QikServe.grocerystore.Service;

import io.github.QikServe.grocerystore.DTO.CartItemDTO;
import io.github.QikServe.grocerystore.DTO.ItemCheckoutDTO;
import io.github.QikServe.grocerystore.DTO.PromotionDTO;
import io.github.QikServe.grocerystore.Exception.ResourceNotFoundException;
import io.github.QikServe.grocerystore.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

        private static final String WIREMOCKURL = "http://localhost:8081";

        private RestTemplate restTemplate;

        @Autowired
        public ProductService(RestTemplate restTemplate){
            this.restTemplate = restTemplate;
        }

        public Product[] getAllProducts(){
            Product[] products;
            try {
                products = restTemplate.getForObject(WIREMOCKURL + "/products", Product[].class);
            }catch (HttpClientErrorException.NotFound e){
                products = new Product[0];
            }
            return products;
        }

        public Product getOneProduct(String id){
            Product product;
            try {
                product = restTemplate.getForObject(WIREMOCKURL + "/products/" + id, Product.class);
            }catch (HttpClientErrorException.NotFound e){
                product = null;
            }
            return product;
        }

        public List<ItemCheckoutDTO> addToCart(List<CartItemDTO> items){
            List<ItemCheckoutDTO> itemCheckouts = new ArrayList<>();

            for (CartItemDTO item : items) {
                    Product product = restTemplate.getForObject(WIREMOCKURL + "/products/" + item.getProductId(), Product.class);
                    if(product == null){
                        throw  new ResourceNotFoundException("Product is not found for this ID!");
                    }

                    BigDecimal quantity = new BigDecimal(item.getQuantity());

                    BigDecimal productValue = new BigDecimal(product.getPrice());
                    BigDecimal itemTotal = productValue.multiply(quantity);
                    BigDecimal itemSavings = BigDecimal.ZERO;

                    for (PromotionDTO promotion : product.getPromotions()){
                        switch (promotion.getType()){
                            case FLAT_PERCENT:
                                BigDecimal[] resultFlatPercent = applyFlatPercentDiscount(itemTotal, productValue, quantity, promotion.getAmount(), itemSavings);
                                itemTotal = resultFlatPercent[0];
                                itemSavings = resultFlatPercent[1];
                                break;
                            case BUY_X_GET_Y_FREE:
                                BigDecimal[] resultBuyXGetYFree = applyBuyXGetYFreeDiscount(itemTotal, productValue, quantity, promotion.getRequiredQty(), itemSavings);
                                itemTotal = resultBuyXGetYFree[0];
                                itemSavings = resultBuyXGetYFree[1];
                                break;
                            case QTY_BASED_PRICE_OVERRIDE:
                                BigDecimal[] resultQtyBasedPriceOverride = applyQtyBasedPriceOverrideDiscount(itemTotal, productValue, quantity, promotion.getRequiredQty(), promotion.getPrice(), itemSavings);
                                itemTotal = resultQtyBasedPriceOverride[0];
                                itemSavings = resultQtyBasedPriceOverride[1];
                                break;
                            default:
                                throw new ResourceNotFoundException("Promotion type does not exist");
                        }
                    }

                    ItemCheckoutDTO dto = new ItemCheckoutDTO(product, quantity, itemTotal, itemSavings);
                    itemCheckouts.add(dto);
            }

            return itemCheckouts;
        }

        private BigDecimal[] applyFlatPercentDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, double discountPercent, BigDecimal itemSavings){
            BigDecimal discountAmount = productValue.multiply(BigDecimal.valueOf(discountPercent)).divide(BigDecimal.valueOf(100));
            BigDecimal totalDiscount = discountAmount.multiply(quantity);

            itemTotal = itemTotal.subtract(totalDiscount);
            itemSavings = itemSavings.add(totalDiscount);

            return new BigDecimal[]{itemTotal, itemSavings};
        }

        private BigDecimal[] applyBuyXGetYFreeDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, int requiredQty, BigDecimal itemSavings){
            BigDecimal qtFree = quantity.divide(new BigDecimal(requiredQty), 0, RoundingMode.DOWN);
            BigDecimal totalFreeItemsValue = productValue.multiply(qtFree);

            itemTotal = itemTotal.subtract(totalFreeItemsValue);
            itemSavings = itemSavings.add(totalFreeItemsValue);

            return new BigDecimal[]{itemTotal, itemSavings};
        }

        private BigDecimal[] applyQtyBasedPriceOverrideDiscount(BigDecimal itemTotal, BigDecimal productValue, BigDecimal quantity, int requiredQty, double promoPrice, BigDecimal itemSavings){
            int qtPromo = quantity.intValue() / requiredQty;
            int qtNonPromo = quantity.intValue() % requiredQty;

            BigDecimal totalDiscountedPrice  = new BigDecimal(qtPromo).multiply(BigDecimal.valueOf(promoPrice));

            if(qtNonPromo > 0){
                totalDiscountedPrice  = totalDiscountedPrice .add(productValue);
            }

            itemSavings = itemSavings.add(itemTotal.subtract(totalDiscountedPrice));
            itemTotal = itemTotal.subtract(itemSavings);

            return new BigDecimal[]{itemTotal, itemSavings};
        }
}
