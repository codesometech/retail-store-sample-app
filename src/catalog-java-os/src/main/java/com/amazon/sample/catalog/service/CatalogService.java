package com.amazon.sample.catalog.service;

import com.amazon.sample.catalog.model.Product;
import com.amazon.sample.catalog.model.Tag;
import com.amazon.sample.catalog.repository.ProductRepository;
import com.amazon.sample.catalog.repository.TagRepository;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final VectorStore vectorStore;

    public CatalogService(ProductRepository productRepository, TagRepository tagRepository, VectorStore vectorStore) {
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.vectorStore = vectorStore;
    }

    public Page<Product> getProducts(List<String> tags, String searchText, String order, int page, int size) {
        // ... (existing implementation)

        String sortField = "name.keyword";
        String sortOrder = "Asc";

        if ("price_asc".equals(order)) {
            sortField = "price";
            sortOrder = "Asc";
        } else if ("price_desc".equals(order)) {
            sortField = "price";
            sortOrder = "Desc";
        }

        int pageNumber = page > 0 ? page - 1 : 0;
        int from = pageNumber * size;

        try {
            SearchResponse<Product> response = productRepository.search(tags, searchText, sortField, sortOrder, from, size);
            List<Product> products = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
            
            long total = response.hits().total().value();
            
            return new PageImpl<>(products, PageRequest.of(pageNumber, size), total);
            
        } catch (IOException e) {
            throw new RuntimeException("Error searching products", e);
        }
    }

    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    public long countProducts(List<String> tags, String searchText) {
        try {
            return productRepository.count(tags, searchText);
        } catch (IOException e) {
             throw new RuntimeException("Error counting products", e);
        }
    }

    public List<Tag> getTags() {
        try {
            return tagRepository.findAllByOrderByDisplayNameAsc();
        } catch (IOException e) {
             throw new RuntimeException("Error fetching tags", e);
        }
    }

    public List<Product> searchProductsVector(String query) {
        return vectorStore.similaritySearch(SearchRequest.query(query).withTopK(10)).stream()
                .map(doc -> {
                    String productId = (String) doc.getMetadata().get("productId");
                    return getProduct(productId).orElse(null);
                })
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }
}