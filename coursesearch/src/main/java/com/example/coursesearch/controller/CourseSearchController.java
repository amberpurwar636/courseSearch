package com.example.coursesearch.controller;

import com.example.coursesearch.model.CourseDocument;
import com.example.coursesearch.service.CourseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class CourseSearchController {

    @Autowired
    private CourseSearchService service;

    @GetMapping
    public Map<String, Object> search(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Integer minAge,
                                      @RequestParam(required = false) Integer maxAge,
                                      @RequestParam(required = false) String category,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false) Double minPrice,
                                      @RequestParam(required = false) Double maxPrice,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
                                      @RequestParam(defaultValue = "upcoming") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {

        SearchHits<CourseDocument> hits = service.search(q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("total", hits.getTotalHits());
        response.put("courses", hits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList()));
        return response;
    }
}

