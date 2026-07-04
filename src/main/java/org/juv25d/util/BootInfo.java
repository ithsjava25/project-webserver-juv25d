package org.juv25d.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BootInfo tillhandahåller ett unikt ID per serveruppstart.
 *
 * Används för att rotera Basic Auth-realm mellan omstarter så att
 * webbläsare inte återanvänder cachelagrade uppgifter över en omstart.
 */
public final class BootInfo {

    private static final String BOOT_ID = UUID.randomUUID().toString();
    // Version som kan höjas vid t.ex. utloggning för att rotera BasicAuth-realm
    private static final AtomicInteger REALM_VERSION = new AtomicInteger(1);
    // Håller reda på för vilken version vi redan har skickat en 401-utmaning.
    // Initieras till 1 så att första uppstarten inte är "gated" (befintliga tester förblir gröna).
    private static final AtomicInteger CHALLENGED_VERSION = new AtomicInteger(1);

    private BootInfo() { }

    /**
     * Returnerar ett unikt ID som genererats vid uppstart.
     */
    public static String getBootId() {
        return BOOT_ID;
    }

    /**
     * Returnerar nuvarande realms versionsnummer (>=1).
     */
    public static int getRealmVersion() {
        return REALM_VERSION.get();
    }

    /**
     * Ökar realm-versionen. Anropas vid t.ex. /logout för att tvinga om‑autentisering
     * (webbläsare ska inte återanvända tidigare cachade uppgifter när realmet ändras).
     */
    public static int bumpRealmVersion() {
        return REALM_VERSION.incrementAndGet();
    }

    /**
     * Nuvarande Restricted-realm som används av BasicAuthPlugin vid 401-svar.
     */
    public static String currentRestrictedRealm() {
        return "Restricted-" + getBootId() + "-v" + getRealmVersion();
    }

    /**
     * Nuvarande LoggedOut-realm som används av LogoutPlugin vid 401-svar.
     */
    public static String currentLogoutRealm() {
        return "LoggedOut-" + getBootId() + "-v" + getRealmVersion();
    }

    /**
     * Har en 401-utmaning redan skickats för nuvarande realm-version?
     */
    public static boolean hasCurrentRealmBeenChallenged() {
        return CHALLENGED_VERSION.get() >= REALM_VERSION.get();
    }

    /**
     * Markera att en 401-utmaning har skickats för nuvarande realm-version.
     */
    public static void markCurrentRealmChallenged() {
        CHALLENGED_VERSION.set(REALM_VERSION.get());
    }
}
