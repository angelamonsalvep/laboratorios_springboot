package com.riwi.intro.controllers;

import com.riwi.intro.Event;
import com.riwi.intro.service.GreetingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Greeting", description = "This controller allow manage greets")
public class GreetingController {

    private final GreetingService service;

    public GreetingController(GreetingService service) {
        this.service = service;
    }

    @GetMapping("/greet")
    public String greet(@RequestBody() Event event) {
        return service.getPersonalizedGreeting(event.getName());
    }

    @GetMapping("/greet_v2")
    public String greetv2(@RequestParam(defaultValue = "Coder") String name) {
        return service.getPersonalizedGreeting(name);
    }
}
