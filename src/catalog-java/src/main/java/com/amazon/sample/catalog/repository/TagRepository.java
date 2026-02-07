package com.amazon.sample.catalog.repository;

import com.amazon.sample.catalog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    // Orders by display name asc
    List<Tag> findAllByOrderByDisplayNameAsc();
}
