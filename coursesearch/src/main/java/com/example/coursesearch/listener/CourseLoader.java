package com.example.coursesearch.listener;

import com.example.coursesearch.model.CourseDocument;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        InputStream is = getClass().getResourceAsStream("/sample-courses.json");
        ObjectMapper mapper = new ObjectMapper();
        List<CourseDocument> courses = mapper.readValue(is, new TypeReference<List<CourseDocument>>() {});
        courses.forEach(operations::save);
        System.out.println("Courses indexed into Elasticsearch.");
    }
}
