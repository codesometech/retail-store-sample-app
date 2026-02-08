package com.amazon.sample.catalog.repository;

import com.amazon.sample.catalog.model.Tag;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TagRepository {

    private final OpenSearchClient client;
    private static final String INDEX_NAME = "tags";

    public TagRepository(OpenSearchClient client) {
        this.client = client;
    }

    public void saveAll(List<Tag> tags) throws IOException {
        for (Tag tag : tags) {
            client.index(i -> i
                .index(INDEX_NAME)
                .id(tag.getName())
                .document(tag)
            );
        }
    }

    public List<Tag> findAll() throws IOException {
        SearchResponse<Tag> response = client.search(s -> s
                .index(INDEX_NAME)
                .size(1000), 
                Tag.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }

    public long count() {
        try {
            return client.count(c -> c.index(INDEX_NAME)).count();
        } catch (IOException e) {
            return 0;
        }
    }

    public List<Tag> findAllByOrderByDisplayNameAsc() throws IOException {
        SearchResponse<Tag> response = client.search(s -> s
                .index(INDEX_NAME)
                .sort(so -> so.field(f -> f.field("displayName.keyword").order(org.opensearch.client.opensearch._types.SortOrder.Asc)))
                .size(1000), 
                Tag.class);

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
}