package org.juv25d.auth;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * In-memory, thread-safe session store with idle timeout.
 */
public final class SessionStore {
    private static final SessionStore INSTANCE = new SessionStore();

    private SessionStore() {}

    // 30 min idle timeout by default (seconds)
    private volatile long idleTimeoutSeconds = 30L * 60L;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    // Background cleanup scheduler
    private volatile @org.jspecify.annotations.Nullable ScheduledExecutorService scheduler;
    private volatile @org.jspecify.annotations.Nullable ScheduledFuture<?> scheduledTask;

    public static SessionStore getInstance() { return INSTANCE; }

    public void setIdleTimeoutSeconds(long seconds) {
        if (seconds <= 0) throw new IllegalArgumentException("seconds must be > 0");
        this.idleTimeoutSeconds = seconds;
        // If scheduler is running, reschedule with new period
        rescheduleIfNeeded();
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

    /**
     * Starts a periodic cleanup task that removes expired sessions.
     * Idempotent: calling multiple times has no additional effect.
     */
    public synchronized void startCleanupScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            // already running; ensure period aligns with current timeout
            rescheduleIfNeeded();
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "session-store-cleanup");
                t.setDaemon(true);
                return t;
            }
        });
        long period = computePeriodSeconds();
        scheduledTask = scheduler.scheduleAtFixedRate(this::safeCleanup, period, period, TimeUnit.SECONDS);
    }

    /** Stops the periodic cleanup task, waiting briefly for termination. */
    public synchronized void stopCleanupScheduler() {
        if (scheduler == null) return;
        try {
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
                scheduledTask = null;
            }
            scheduler.shutdown();
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            scheduler = null;
        }
    }

    private void safeCleanup() {
        try {
            cleanupExpired();
        } catch (Throwable ignored) {
            // never let scheduled task die
        }
    }

    private long computePeriodSeconds() {
        long idle = idleTimeoutSeconds;
        long period = idle / 2L; // run twice as often as the idle timeout
        if (period <= 0L) period = 1L;
        // Cap to something reasonable in case idle is very large
        return Math.min(period, TimeUnit.MINUTES.toSeconds(5));
    }

    private synchronized void rescheduleIfNeeded() {
        if (scheduler == null || scheduler.isShutdown()) return;
        long newPeriod = computePeriodSeconds();
        // If there is no task or the period likely changed, reschedule.
        if (scheduledTask == null) {
            scheduledTask = scheduler.scheduleAtFixedRate(this::safeCleanup, newPeriod, newPeriod, TimeUnit.SECONDS);
        } else {
            // We cannot query existing period directly; conservatively reschedule to apply possible change.
            scheduledTask.cancel(false);
            scheduledTask = scheduler.scheduleAtFixedRate(this::safeCleanup, newPeriod, newPeriod, TimeUnit.SECONDS);
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
