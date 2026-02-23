package org.juv25d.logging;

public class LogContext {
    private static final ThreadLocal<String> connectionId = new ThreadLocal<>();

    public static void setConnectionId(String id) {
        connectionId.set(id);
    }

    public static String getConnectionId() {
        return connectionId.get();
    }

    public static void clear() {
        connectionId.remove();
    }
}
