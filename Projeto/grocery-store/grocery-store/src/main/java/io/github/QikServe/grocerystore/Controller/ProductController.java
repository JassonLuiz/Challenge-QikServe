package io.github.QikServe.grocerystore.Controller;

import io.github.QikServe.grocerystore.DTO.CartItemDTO;
import io.github.QikServe.grocerystore.DTO.ItemCheckoutDTO;
import io.github.QikServe.grocerystore.Service.ProductService;
import io.github.QikServe.grocerystore.entity.Product;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Endpoints for Managing Products")
public class ProductController {

        @Autowired
        private ProductService productService;

        @GetMapping
        @Operation(summary = "Finds all Products", description = "Finds all Products", tags = { "Products" }, responses = {
                @ApiResponse(description = "Success", responseCode = "200", content = {
                        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Product.class))) }),
                @ApiResponse(description = "No content", responseCode = "204", content = @Content),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content), })
        public ResponseEntity<Product[]> getAllProducts(){
            Product[] products = productService.getAllProducts();
            if(products.length == 0){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(products);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Find one Product", description = "Find one Product", tags = { "Products" }, responses = {
                @ApiResponse(description = "Success", responseCode = "200", content = {
                        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Product.class))) }),
                @ApiResponse(description = "No content", responseCode = "204", content = @Content),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content), })
        public ResponseEntity<Product> getOneProduct(@PathVariable String id){
            Product product = productService.getOneProduct(id);
            if(product == null){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(product);
        }

        @PostMapping("/addToCart")
        @Operation(summary = "Add a list of Products to cart", description = "Add a list of Products to cart", tags = { "Products" }, responses = {
                @ApiResponse(description = "Success", responseCode = "200", content = {
                        @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Product.class))) }),
                @ApiResponse(description = "No content", responseCode = "204", content = @Content),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Not Found", responseCode = "404", content = @Content),
                @ApiResponse(description = "Internal Error", responseCode = "500", content = @Content), })
        public ResponseEntity<List<ItemCheckoutDTO>> addToCart(@RequestBody List<CartItemDTO> items){
            List<ItemCheckoutDTO> itemCheckouts = productService.addToCart(items);
            if(itemCheckouts.isEmpty()){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(itemCheckouts);
        }
}
