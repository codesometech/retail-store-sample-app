package com.amazon.sample.catalog.service;

import com.amazon.sample.catalog.model.Product;
import com.amazon.sample.catalog.model.Tag;
import com.amazon.sample.catalog.repository.ProductRepository;
import com.amazon.sample.catalog.repository.TagRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CatalogService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;

    public CatalogService(ProductRepository productRepository, TagRepository tagRepository) {
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
    }

    public Page<Product> getProducts(List<String> tags, String searchText, String order, int page, int size) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tags != null && !tags.isEmpty()) {
                Join<Product, Tag> tagsJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagsJoin.get("name").in(tags));
                query.distinct(true);
            }

            if (StringUtils.hasText(searchText)) {
                String likePattern = "%" + searchText.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by("name").ascending();
        if ("price_asc".equals(order)) {
            sort = Sort.by("price").ascending();
        } else if ("price_desc".equals(order)) {
            sort = Sort.by("price").descending();
        }

        // API page is 1-based, Spring Data is 0-based
        int pageNumber = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNumber, size, sort);

        return productRepository.findAll(spec, pageable);
    }

    public Optional<Product> getProduct(String id) {
        return productRepository.findById(id);
    }

    public long countProducts(List<String> tags, String searchText) {
        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (tags != null && !tags.isEmpty()) {
                Join<Product, Tag> tagsJoin = root.join("tags", JoinType.INNER);
                predicates.add(tagsJoin.get("name").in(tags));
                query.distinct(true);
            }

            if (StringUtils.hasText(searchText)) {
                String likePattern = "%" + searchText.toLowerCase() + "%";
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.count(spec);
    }

    public List<Tag> getTags() {
        return tagRepository.findAllByOrderByDisplayNameAsc();
    }
}
