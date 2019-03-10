package com.crescendo.application.controller;

import com.crescendo.application.model.ReviewParam;
import com.crescendo.application.service.YelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cws/yelpService")
public class YelpController {

    @Autowired
    private YelpService yelpService;

    @PostMapping(path = "get", consumes = "application/json", produces = "application/json")
    public ResponseEntity getYelpReviews(@RequestBody ReviewParam reviewParam) {
        return ResponseEntity.ok(yelpService.getReview(reviewParam));
    }

}
