package com.example.polls.controller;


import org.springframework.web.bind.annotation.*;


/**
 * Created by rajeevkumarsingh on 20/11/17.
 */

@RestController
@RequestMapping("/api/_status/healthz")
public class StatusController {

    @GetMapping
    public String healthCheck() {
        return "UP";
    }

}
