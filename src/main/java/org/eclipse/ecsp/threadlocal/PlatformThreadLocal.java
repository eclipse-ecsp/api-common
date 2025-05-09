package org.eclipse.ecsp.threadlocal;

/**
 * PlatformThreadLocal is a utility class to manage the platformId in a ThreadLocal variable.
 */
public class PlatformThreadLocal {

    private PlatformThreadLocal() {
        // Private constructor to prevent instantiation
    }

    private static final ThreadLocal<String> PLATFORM_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Sets the platformId in the ThreadLocal.
     *
     * @param platformId the platformId to set
     */
    public static void setPlatformId(String platformId) {
        PLATFORM_ID_THREAD_LOCAL.set(platformId);
    }

    /**
     * Retrieves the platformId from the ThreadLocal.
     *
     * @return the platformId, or null if not set
     */
    public static String getPlatformId() {
        return PLATFORM_ID_THREAD_LOCAL.get();
    }

    /**
     * Clears the platformId from the ThreadLocal.
     */
    public static void clear() {
        PLATFORM_ID_THREAD_LOCAL.remove();
    }
}
