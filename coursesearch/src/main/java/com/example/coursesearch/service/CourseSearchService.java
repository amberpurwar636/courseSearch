package com.example.coursesearch.service;

import co.elastic.clients.json.JsonData;
import com.example.coursesearch.model.CourseDocument;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseSearchService {

    @Autowired
    private ElasticsearchOperations operations;

    public SearchHits<CourseDocument> search(String q,
            Integer minAge,
            Integer maxAge,
            String category,
            String type,
            Double minPrice,
            Double maxPrice,
            Instant startDate,
            String sort,
            int page,
            int size) {

        List<Query> filters = new ArrayList<>();

        if (minAge != null) {
            filters.add(RangeQuery.of(r -> r.field("minAge").gte(JsonData.of(minAge)))._toQuery());
        }
        if (maxAge != null) {
            filters.add(RangeQuery.of(r -> r.field("maxAge").lte(JsonData.of(maxAge)))._toQuery());
        }
        if (category != null) {
            filters.add(TermQuery.of(t -> t.field("category.keyword").value(category))._toQuery());
        }
        if (type != null) {
            filters.add(TermQuery.of(t -> t.field("type.keyword").value(type))._toQuery());
        }
        if (minPrice != null) {
            filters.add(RangeQuery.of(r -> r.field("price").gte(JsonData.of(minPrice)))._toQuery());
        }
        if (maxPrice != null) {
            filters.add(RangeQuery.of(r -> r.field("price").lte(JsonData.of(maxPrice)))._toQuery());
        }
        if (startDate != null) {
            filters.add(
                    RangeQuery.of(r -> r.field("nextSessionDate").gte(JsonData.of(startDate.toString())))._toQuery());
        }

        Query mainQuery;
        if (q != null) {
            mainQuery = MultiMatchQuery.of(m -> m.fields("title", "description").query(q))._toQuery();
        } else {
            // If no search query, use a match_all query or similar to return all results
            // (or handle as per business logic)
            // For now, setting to null, but consider a match_all if no 'q' means all
            // results.
            mainQuery = null; // Or Query.of(q -> q.matchAll(m -> m)); for a match_all query
        }

        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        if (mainQuery != null) {
            boolQuery.must(mainQuery);
        }
        boolQuery.filter(filters);

        SortOptions sortOptions = SortOptions.of(s -> s
                .field(f -> f
                        .field("nextSessionDate")
                        .order(SortOrder.Asc)));
        if ("priceAsc".equals(sort)) {
            sortOptions = SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
        } else if ("priceDesc".equals(sort)) {
            sortOptions = SortOptions.of(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
        }

        NativeQuery query = NativeQuery.builder()
                .withQuery(qb -> qb.bool(boolQuery.build()))
                .withSort(List.of(sortOptions)) // Changed from withSorts to withSort and wrapped in List.of()
                .withPageable(PageRequest.of(page, size))
                .build();

        return operations.search(query, CourseDocument.class);
    }
}



