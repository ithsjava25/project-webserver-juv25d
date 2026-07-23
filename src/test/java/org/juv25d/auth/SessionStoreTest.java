package org.juv25d.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SessionStoreTest {

    private long originalTimeout;

    @BeforeEach
    void setUp() {
        originalTimeout = SessionStore.getInstance().getIdleTimeoutSeconds();
    }

    @AfterEach
    void tearDown() {
        SessionStore.getInstance().stopCleanupScheduler();
        SessionStore.getInstance().setIdleTimeoutSeconds(originalTimeout);
    }

    @Test
    void sessionExpiresAndIsRemovedWithoutSubsequentLogin() throws Exception {
        SessionStore store = SessionStore.getInstance();
        store.setIdleTimeoutSeconds(2); // 2 seconds idle timeout
        store.startCleanupScheduler();

        Session s = store.create("alice");
        String id = s.getId();

        // Verify scheduled eviction without triggering lazy eviction via get(id):
        // Poll the internal sessions map membership directly until a deadline.
        Map<String, ?> sessions = accessSessionsMap(store);
        long idle = store.getIdleTimeoutSeconds();
        long period = Math.max(1, idle / 2);
        Instant deadline = Instant.now().plus(Duration.ofSeconds(idle + period + 1)); // bounded timeout tied to config
        boolean removedByScheduler = false;
        while (Instant.now().isBefore(deadline)) {
            if (!sessions.containsKey(id)) {
                removedByScheduler = true;
                break;
            }
            Thread.sleep(40); // short polling interval
        }

        assertTrue(removedByScheduler, "Expected background scheduler to evict expired session within timeout");

        // Optional: confirm public API also reflects removal without causing lazy eviction
        assertNull(store.get(id), "Expected session to be expired and removed by background cleanup");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> accessSessionsMap(SessionStore store) throws Exception {
        Field f = SessionStore.class.getDeclaredField("sessions");
        f.setAccessible(true);
        return (Map<String, ?>) f.get(store);
    }
}
