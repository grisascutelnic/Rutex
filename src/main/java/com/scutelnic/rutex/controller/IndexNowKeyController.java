package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.IndexNowService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexNowKeyController {

    private final IndexNowService indexNowService;

    public IndexNowKeyController(IndexNowService indexNowService) {
        this.indexNowService = indexNowService;
    }

    @GetMapping(value = "/{key:[A-Za-z0-9-]{8,128}}.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> verificationKey(@PathVariable String key) {
        if (!indexNowService.isValidKey(key)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(indexNowService.key());
    }
}
