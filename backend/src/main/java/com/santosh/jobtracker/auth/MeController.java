package com.santosh.jobtracker.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public Map<String, Object> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of(
                "email", auth.getPrincipal(),
                "userId", auth.getDetails()
        );
    }
}
