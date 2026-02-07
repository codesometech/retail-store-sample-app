package com.amazon.sample.catalog.controller;

import com.amazon.sample.catalog.model.CatalogSizeResponse;
import com.amazon.sample.catalog.model.Product;
import com.amazon.sample.catalog.model.Tag;
import com.amazon.sample.catalog.service.CatalogService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchText) {

        List<String> tagList = parseTags(tags);

        Page<Product> productPage = catalogService.getProducts(tagList, searchText, order, page, size);
        return ResponseEntity.ok(productPage.getContent());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return catalogService.getProduct(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/size")
    public ResponseEntity<CatalogSizeResponse> getCatalogSize(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String searchText) {
        
        List<String> tagList = parseTags(tags);
        long count = catalogService.countProducts(tagList, searchText);
        return ResponseEntity.ok(new CatalogSizeResponse((int) count));
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Tag>> getTags() {
        return ResponseEntity.ok(catalogService.getTags());
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
