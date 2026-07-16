package org.juv25d.util;

import org.juv25d.http.HttpRequest;
import org.jspecify.annotations.Nullable;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CookieUtils {

    public static @Nullable String readCookie(HttpRequest req, String name) {
        if (req == null || name == null) return null;
        String cookieHeader = null;
        for (var e : req.headers().entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase("Cookie")) {
                cookieHeader = e.getValue();
                break;
            }
        }
        if (cookieHeader == null || cookieHeader.isBlank()) return null;
        String[] parts = cookieHeader.split(";\\s*");
        for (String part : parts) {
            int i = part.indexOf('=');
            if (i <= 0) continue;
            String k = part.substring(0, i).trim();
            if (!k.equals(name)) continue;
            String v = part.substring(i + 1).trim();
            try {
                return URLDecoder.decode(v, StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                return v;
            }
        }
        return null;
    }
}
