package com.amazon.sample.catalog.repository;

import com.amazon.sample.catalog.model.Product;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private final OpenSearchClient client;
    private static final String INDEX_NAME = "products";

    public ProductRepository(OpenSearchClient client) {
        this.client = client;
    }

    public void saveAll(List<Product> products) throws IOException {
        for (Product product : products) {
            client.index(i -> i
                .index(INDEX_NAME)
                .id(product.getId())
                .document(product)
            );
        }
    }

    public Optional<Product> findById(String id) {
        try {
            var response = client.get(g -> g.index(INDEX_NAME).id(id), Product.class);
            if (response.found()) {
                return Optional.ofNullable(response.source());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
    
    public long count() {
        try {
            return client.count(c -> c.index(INDEX_NAME)).count();
        } catch (IOException e) {
            return 0;
        }
    }

    public SearchResponse<Product> search(List<String> tags, String searchText, String sortField, String sortOrder, int from, int size) throws IOException {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (tags != null && !tags.isEmpty()) {
            // Should match at least one tag
            BoolQuery.Builder tagBool = new BoolQuery.Builder();
            for (String tag : tags) {
                tagBool.should(s -> s.term(t -> t.field("tags.name.keyword").value(v -> v.stringValue(tag))));
            }
            boolQuery.must(tagBool.build()._toQuery());
        }

        if (StringUtils.hasText(searchText)) {
            String lowerSearchText = searchText.toLowerCase();
            boolQuery.must(m -> m.bool(b -> b
                .should(s -> s.wildcard(w -> w.field("name").value("*" + lowerSearchText + "*")))
                .should(s -> s.wildcard(w -> w.field("description").value("*" + lowerSearchText + "*")))
            ));
        }

        Query query = boolQuery.build()._toQuery();

        return client.search(s -> s
                .index(INDEX_NAME)
                .query(query)
                .from(from)
                .size(size)
                .sort(so -> so.field(f -> f
                        .field(sortField)
                        .order(SortOrder.valueOf(sortOrder))
                )),
                Product.class
        );
    }
    
    public long count(List<String> tags, String searchText) throws IOException {
         BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        if (tags != null && !tags.isEmpty()) {
            BoolQuery.Builder tagBool = new BoolQuery.Builder();
            for (String tag : tags) {
                tagBool.should(s -> s.term(t -> t.field("tags.name.keyword").value(v -> v.stringValue(tag))));
            }
            boolQuery.must(tagBool.build()._toQuery());
        }

        if (StringUtils.hasText(searchText)) {
             String lowerSearchText = searchText.toLowerCase();
            boolQuery.must(m -> m.bool(b -> b
                .should(s -> s.wildcard(w -> w.field("name").value("*" + lowerSearchText + "*")))
                .should(s -> s.wildcard(w -> w.field("description").value("*" + lowerSearchText + "*")))
            ));
        }
        
        Query query = boolQuery.build()._toQuery();
        
        return client.count(c -> c.index(INDEX_NAME).query(query)).count();
    }
}