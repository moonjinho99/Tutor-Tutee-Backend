package com.example.tutoring.controller;

import com.example.tutoring.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Slf4j
@Controller
public class SearchController {
    @Autowired
    SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam("nickname") String nickname) {
        log.info("search api 진입");

        return searchService.search(nickname);
    }
}
