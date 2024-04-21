package io.github.QikServe.grocerystore.Controller;

import io.github.QikServe.grocerystore.DTO.CartItemDTO;
import io.github.QikServe.grocerystore.DTO.ItemCheckoutDTO;
import io.github.QikServe.grocerystore.Service.ProductService;
import io.github.QikServe.grocerystore.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

        @Autowired
        private ProductService productService;

        @GetMapping
        public ResponseEntity<?> getAllProducts(){
            Product[] products = productService.getAllProducts();
            if(products.length == 0){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(products);
        }

        @GetMapping("/{id}")
        public ResponseEntity<Product> getOneProduct(@PathVariable String id){
            Product product = productService.getOneProduct(id);
            if(product == null){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        }

        @PostMapping("/addToCart")
        public ResponseEntity<List<ItemCheckoutDTO>> addToCart(@RequestBody List<CartItemDTO> items){
            List<ItemCheckoutDTO> itemCheckouts = productService.addToCart(items);
            if(itemCheckouts.size() == 0){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(itemCheckouts);
        }
}
