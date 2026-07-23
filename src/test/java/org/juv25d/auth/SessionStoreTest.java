package org.juv25d.auth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

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

        // Sleep longer than idle timeout plus one scheduler period (idle/2) to allow cleanup to run
        Thread.sleep(4000);

        // Now a single verification access should return null because cleanup already removed it
        assertNull(store.get(id), "Expected session to be expired and removed by background cleanup");
    }
}
