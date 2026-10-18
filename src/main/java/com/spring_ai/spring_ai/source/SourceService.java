package com.spring_ai.spring_ai.source;

import com.spring_ai.spring_ai.source.dto.SourceRequest;
import com.spring_ai.spring_ai.source.dto.SourceResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;


@Service
public class SourceService {

    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Transactional
    public SourceResponse create(SourceRequest request) {
        if (request.name() == null
                || request.name().isBlank()
                || request.name().trim().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name is required and must be at most 200 characters"
            );
        }

        if (request.sourceType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Source type is required"
            );
        }

        String feedUrl = validateUrl(request.feedUrl());

        if (sourceRepository.existsByFeedUrl(feedUrl)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This feed URL is already registered"
            );
        }

        boolean active =
                request.active() == null || request.active();

        Source source = new Source(
                request.name().trim(),
                request.sourceType(),
                feedUrl,
                active
        );

        return toResponse(sourceRepository.save(source));
    }

    @Transactional(readOnly = true)
    public List<SourceResponse> findAll() {
        return sourceRepository.findAll()
                .stream()
                .map(SourceService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SourceResponse> findActive() {
        return sourceRepository.findByActiveTrue()
                .stream()
                .map(SourceService::toResponse)
                .toList();
    }

    private String validateUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Feed URL is required"
            );
        }

        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();

            if (uri.getHost() == null
                    || scheme == null
                    || !(scheme.equalsIgnoreCase("http")
                    || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException();
            }

            return uri.toString();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Feed URL must be a valid HTTP or HTTPS URL"
            );
        }
    }

    private static SourceResponse toResponse(Source source) {
        return new SourceResponse(
                source.getId(),
                source.getName(),
                source.getSourceType(),
                source.getFeedUrl(),
                source.isActive(),
                source.getLastFetchedAt(),
                source.getCreatedAt(),
                source.getUpdatedAt()
        );
    }
}