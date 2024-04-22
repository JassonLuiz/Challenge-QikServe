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
                                BigDecimal discountPercentage = BigDecimal.valueOf(promotion.getAmount()).divide(BigDecimal.valueOf(100));
                                BigDecimal discountAmount = productValue.multiply(discountPercentage);

                                itemTotal = itemTotal.subtract(discountAmount.multiply(quantity));
                                itemSavings = itemSavings.add(discountAmount.multiply(quantity));
                                break;
                            case BUY_X_GET_Y_FREE:
                                BigDecimal qtFree = quantity.divide(new BigDecimal(promotion.getRequiredQty()));

                                itemTotal = itemTotal.subtract(qtFree.multiply(productValue));
                                itemSavings = itemSavings.add(qtFree.multiply(productValue));
                                break;
                            case QTY_BASED_PRICE_OVERRIDE:
                                int requiredQt = promotion.getRequiredQty();
                                BigDecimal promoPrice = new BigDecimal(promotion.getPrice());

                                int qtPromo = quantity.intValue() / requiredQt;
                                int qtNonPromo = quantity.intValue() % requiredQt;

                                BigDecimal totalDesc = new BigDecimal(qtPromo).multiply(promoPrice);

                                if(qtNonPromo > 0){
                                    totalDesc.add(productValue);
                                }

                                itemSavings = itemSavings.add(itemTotal.subtract(totalDesc));
                                itemTotal = itemTotal.subtract(itemSavings);
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
}
