package org.juv25d.auth;

import java.time.Instant;
import java.util.Objects;

/**
 * Simple server-side session model.
 */
public final class Session {
    private final String id;
    private final String user;
    private final long createdAtEpochSeconds;
    private volatile long lastSeenEpochSeconds;

    public Session(String id, String user) {
        this.id = Objects.requireNonNull(id, "id");
        this.user = Objects.requireNonNull(user, "user");
        long now = Instant.now().getEpochSecond();
        this.createdAtEpochSeconds = now;
        this.lastSeenEpochSeconds = now;
    }

    public String getId() { return id; }
    public String getUser() { return user; }
    public long getCreatedAtEpochSeconds() { return createdAtEpochSeconds; }
    public long getLastSeenEpochSeconds() { return lastSeenEpochSeconds; }
    public void touch() { this.lastSeenEpochSeconds = Instant.now().getEpochSecond(); }
}
