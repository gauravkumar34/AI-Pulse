package com.spring_ai.spring_ai.source;

import com.spring_ai.spring_ai.source.dto.SourceRequest;
import com.spring_ai.spring_ai.source.dto.SourceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SourceResponse create(
            @RequestBody SourceRequest request) {
        return sourceService.create(request);
    }

    @GetMapping
    public List<SourceResponse> findAll() {
        return sourceService.findAll();
    }

    @GetMapping("/active")
    public List<SourceResponse> findActive() {
        return sourceService.findActive();
    }
}