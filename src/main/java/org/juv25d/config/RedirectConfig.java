package org.juv25d.config;

import org.juv25d.filter.RedirectRule;

import java.util.List;

public class RedirectConfig {

    public List<RedirectRule> rules() {
        return List.of(
            new RedirectRule("/old-page", "/new-page", 301),
            new RedirectRule("/temp", "https://example.com/temporary", 302),
            new RedirectRule("/docs/*", "/documentation/", 301)
        );
    }
}
