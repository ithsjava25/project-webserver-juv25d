package org.juv25d.logging;

import org.jspecify.annotations.Nullable;

public class LogContext {
    private static final ThreadLocal<String> connectionId = new ThreadLocal<>();

    public static void setConnectionId(String id) {
        connectionId.set(id);
    }

    @Nullable public static String getConnectionId() {
        return connectionId.get();
    }

    public static void clear() {
        connectionId.remove();
    }
}
