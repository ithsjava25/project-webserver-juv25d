package org.juv25d.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory, thread-safe session store with idle timeout.
 */
public final class SessionStore {
    private static final SessionStore INSTANCE = new SessionStore();

    // 30 min idle timeout by default (seconds)
    private volatile long idleTimeoutSeconds = 30L * 60L;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public static SessionStore getInstance() { return INSTANCE; }

    public void setIdleTimeoutSeconds(long seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("seconds must be > 0");
        this.idleTimeoutSeconds = seconds;
    }

    public long getIdleTimeoutSeconds() { return idleTimeoutSeconds; }

    public Session create(String user) {
        Objects.requireNonNull(user, "user");
        String id = newId();
        Session s = new Session(id, user);
        sessions.put(id, s);
        cleanupExpired();
        return s;
    }

    public @org.jspecify.annotations.Nullable Session get(String id) {
        if (id == null || id.isBlank()) return null;
        Session s = sessions.get(id);
        if (s == null) return null;
        if (isExpired(s)) {
            sessions.remove(id);
            return null;
        }
        s.touch();
        return s;
    }

    public void invalidate(String id) {
        if (id == null) return;
        sessions.remove(id);
    }

    public void cleanupExpired() {
        long now = Instant.now().getEpochSecond();
        long idle = idleTimeoutSeconds;
        for (Map.Entry<String, Session> e : sessions.entrySet()) {
            Session s = e.getValue();
            if ((now - s.getLastSeenEpochSeconds()) > idle) {
                sessions.remove(e.getKey());
            }
        }
    }

    private boolean isExpired(Session s) {
        long now = Instant.now().getEpochSecond();
        return (now - s.getLastSeenEpochSeconds()) > idleTimeoutSeconds;
    }

    private String newId() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
