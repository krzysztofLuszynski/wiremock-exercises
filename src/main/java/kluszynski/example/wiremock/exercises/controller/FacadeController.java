package kluszynski.example.wiremock.exercises.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FacadeController {

    @GetMapping("/facade")
    public String get() {
        return "Hello world";
    }
}
