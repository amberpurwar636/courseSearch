package com.example.coursesearch.listener;

import com.example.coursesearch.model.CourseDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // No longer needed for explicit import here

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class CourseLoader implements ApplicationRunner {

    @Autowired
    private ElasticsearchOperations operations;

    // Autowire the ObjectMapper instance provided by Spring Boot
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        InputStream is = getClass().getResourceAsStream("/sample-courses.json");
        // Use the autowired ObjectMapper instead of creating a new one
        // ObjectMapper mapper = new ObjectMapper(); // Remove this line
        // mapper.registerModule(new JavaTimeModule()); // Remove this line as Spring's
        // ObjectMapper already has it

        List<CourseDocument> courses = objectMapper.readValue(is, new TypeReference<List<CourseDocument>>() {
        });
        courses.forEach(operations::save);
        System.out.println("Courses indexed into Elasticsearch.");
    }
}
