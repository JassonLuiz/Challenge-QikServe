package io.github.QikServe.grocerystore.Service;

import io.github.QikServe.grocerystore.DTO.CartItemDTO;
import io.github.QikServe.grocerystore.DTO.ItemCheckoutDTO;
import io.github.QikServe.grocerystore.DTO.PromotionDTO;
import io.github.QikServe.grocerystore.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

        private final String WIREMOCKURL = "http://localhost:8081";

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
                        throw  new IllegalStateException("Product is null");
                    }

                    int quantity = item.getQuantity();

                    double productValue = product.getPrice();
                    double itemTotal = productValue * quantity;
                    double itemSavings = 0;

                    for (PromotionDTO promotion : product.getPromotions()){
                        switch (promotion.getType()){
                            case FLAT_PERCENT:
                                double discountAmount = (productValue * promotion.getAmount()) / 100;
                                itemTotal -= discountAmount * quantity;
                                itemSavings += discountAmount * quantity;
                                break;
                            case BUY_X_GET_Y_FREE:
                                int qtFree = quantity / promotion.getRequiredQty();

                                itemTotal -= qtFree * productValue;
                                itemSavings += qtFree * productValue;
                                break;
                            case QTY_BASED_PRICE_OVERRIDE:
                                int requiredQt = promotion.getRequiredQty();
                                double promoPrice = promotion.getAmount();

                                int qtPromo = quantity / requiredQt;
                                int qtNonPromo = quantity % requiredQt;

                                double totalDesc = qtPromo * promoPrice;

                                if(qtNonPromo > 0){
                                    totalDesc += productValue;
                                }

                                itemSavings += itemTotal - totalDesc;
                                itemTotal -= itemSavings;
                                break;
                            default:
                                throw new IllegalArgumentException("Promotion type does not exist");
                        }
                    }

                    ItemCheckoutDTO dto = new ItemCheckoutDTO(product, quantity, itemTotal, itemSavings);
                    itemCheckouts.add(dto);
            }

            return itemCheckouts;
        }
}
