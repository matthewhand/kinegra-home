package tv.metaxy.dashboard;

/**
 * Pure, offline-testable accessibility and tile presentation rules.
 * Android handles sp scaling itself; cards need additional vertical room
 * so the label, description and availability remain visible at large fonts.
 */
public final class DashboardPresentationPolicy {
    private DashboardPresentationPolicy() { }

    /**
     * Never shrink TV touch targets below their original dp height.
     * Cap pathological configuration values so malformed density or scale
     * cannot overflow Android integer layout dimensions.
     */
    public static int minTileHeightDp(int originalDp, float fontScale) {
        if (originalDp <= 0 || originalDp > 1024)
            throw new IllegalArgumentException("Invalid tile height");
        return (int) Math.ceil(originalDp * safeScale(fontScale));
    }

    /**
     * Keep the 3 + 3 + 2 focus graph, but allow horizontal scroll rather
     * than squeezing text when font scaling or narrow viewports demand it.
     */
    public static int rowWidthDp(int screenWidthDp, int outerPaddingDp,
                                 int cells, float fontScale) {
        if (cells < 1 || cells > 3)
            throw new IllegalArgumentException("Invalid row cell count");
        if (outerPaddingDp < 0 || outerPaddingDp > 512)
            throw new IllegalArgumentException("Invalid TV padding");
        int width = screenWidthDp > 0 ? Math.min(screenWidthDp, 8192) : 960;
        int minimumCell = (int) Math.ceil(
                240 + 64 * (safeScale(fontScale) - 1f)) + 8;
        return Math.max(1, Math.max(width - 2 * outerPaddingDp,
                minimumCell * cells));
    }

    /** Stack title and live clock when they cannot safely share one row. */
    public static boolean stackHeader(int screenWidthDp, float fontScale) {
        return (screenWidthDp > 0 && screenWidthDp < 780)
                || safeScale(fontScale) > 1.3f;
    }

    /**
     * Keep the complete day/date on wide, normally-scaled TVs; use a
     * shorter format where the heading and live time would compete or
     * large font settings would make the date/time unreadable.
     */
    public static String clockFormat12Hour(int screenWidthDp, float fontScale) {
        if ((screenWidthDp > 0 && screenWidthDp < 480)
                || safeScale(fontScale) >= 1.8f) return "h:mm a";
        return stackHeader(screenWidthDp, fontScale)
                ? "d MMM h:mm a" : "EEE d MMM  h:mm a";
    }

    public static String clockFormat24Hour(int screenWidthDp, float fontScale) {
        if ((screenWidthDp > 0 && screenWidthDp < 480)
                || safeScale(fontScale) >= 1.8f) return "HH:mm";
        return stackHeader(screenWidthDp, fontScale)
                ? "d MMM HH:mm" : "EEE d MMM  HH:mm";
    }

    private static float safeScale(float fontScale) {
        return Float.isNaN(fontScale) || Float.isInfinite(fontScale)
                ? 1f : Math.max(1f, Math.min(2.5f, fontScale));
    }

    /** Complete spoken cue even when the visible label wraps. */
    public static String accessibilityLabel(String name, boolean enabled,
                                            boolean focused) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Missing target name");
        if (!enabled) return name + ", unavailable, not installed or cannot be opened";
        return name + (focused ? ", selected, press OK to open"
                               : ", installed, press OK to open");
    }

    /** Internal menus are always available but are not installed applications. */
    public static String menuStatus(boolean focused) {
        return focused ? "SELECTED \u2022 Press OK to browse"
                       : "BROWSE \u2022 Apps and About";
    }

    public static String menuAccessibilityLabel(String name, boolean focused) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Missing menu name");
        return name + (focused ? ", selected, press OK to browse"
                               : ", browse apps and About");
    }

    /** A focused tile has an explicit on-screen confirmation cue. */
    public static String status(boolean enabled, boolean focused) {
        if (!enabled) return "Unavailable / not installed or cannot open";
        return focused ? "SELECTED \u2022 Press OK to open"
                       : "Installed \u2022 Ready to open";
    }
}
