package com.amazon.sample.catalog.config;

import com.amazon.sample.catalog.model.Product;
import com.amazon.sample.catalog.model.Tag;
import com.amazon.sample.catalog.repository.ProductRepository;
import com.amazon.sample.catalog.repository.TagRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(ProductRepository productRepository, TagRepository tagRepository, ObjectMapper objectMapper) {
        return args -> {
            loadTags(tagRepository, objectMapper);
            loadProducts(productRepository, tagRepository, objectMapper);
        };
    }

    private void loadTags(TagRepository tagRepository, ObjectMapper objectMapper) throws IOException {
        if (tagRepository.count() > 0) {
            return;
        }

        try (InputStream inputStream = new ClassPathResource("data/tags.json").getInputStream()) {
            List<Tag> tags = objectMapper.readValue(inputStream, new TypeReference<List<Tag>>() {});
            tagRepository.saveAll(tags);
            System.out.println("Loaded " + tags.size() + " tags.");
        }
    }

    private void loadProducts(ProductRepository productRepository, TagRepository tagRepository, ObjectMapper objectMapper) throws IOException {
        if (productRepository.count() > 0) {
            return;
        }

        try (InputStream inputStream = new ClassPathResource("data/products.json").getInputStream()) {
            List<ProductData> productDataList = objectMapper.readValue(inputStream, new TypeReference<List<ProductData>>() {});
            
            Map<String, Tag> tagMap = tagRepository.findAll().stream()
                    .collect(Collectors.toMap(Tag::getName, Function.identity()));

            List<Product> products = new ArrayList<>();
            for (ProductData data : productDataList) {
                Product product = new Product();
                product.setId(data.getId());
                product.setName(data.getName());
                product.setDescription(data.getDescription());
                product.setPrice(data.getPrice());
                
                List<Tag> tags = new ArrayList<>();
                if (data.getTags() != null) {
                    for (String tagName : data.getTags()) {
                        Tag tag = tagMap.get(tagName);
                        if (tag != null) {
                            tags.add(tag);
                        }
                    }
                }
                product.setTags(tags);
                products.add(product);
            }
            productRepository.saveAll(products);
            System.out.println("Loaded " + products.size() + " products.");
        }
    }
    
    @Data
    static class ProductData {
        private String id;
        private String name;
        private String description;
        private int price;
        private List<String> tags;
    }
}
