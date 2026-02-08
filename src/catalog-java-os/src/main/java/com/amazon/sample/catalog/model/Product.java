package com.amazon.sample.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;

    private String name;

    private String description;

    private int price;

    private List<Tag> tags = new ArrayList<>();
}