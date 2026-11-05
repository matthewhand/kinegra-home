package tv.metaxy.dashboard;

/**
 * Allowlisted real app shortcuts, not synthetic panes or process control.
 * Exactly these packages may be queried or launched by Shieldarchy.
 */
public final class LaunchRegistry {
    private LaunchRegistry() { }

    public static final class Target {
        public final String title;
        public final String packageName;
        public final String description;
        public Target(String title, String packageName, String description) {
            if (title == null || title.trim().isEmpty()
                    || packageName == null || !packageName.matches("[a-zA-Z0-9_.]+")
                    || description == null) {
                throw new IllegalArgumentException("Invalid launch target");
            }
            this.title = title;
            this.packageName = packageName;
            this.description = description;
        }
    }

    public static final Target[] TARGETS = {
        new Target("YouTube", "com.google.android.youtube.tv", "Watch video"),
        new Target("Kinegra Omega Prime", "au.private.omegaprime.tegra", "Arcade racing"),
        new Target("Plex", "com.plexapp.android", "Movies and television"),
        new Target("Steam Link", "com.valvesoftware.steamlink", "Stream PC games"),
        new Target("NVIDIA Games", "com.nvidia.tegrazone3", "Cloud and local games"),
        new Target("More apps", "eu.chainfire.tv.sideloadlauncher", "Camera, music and split-screen status")
    };

    public static final String MORE_APPS_PACKAGE = "eu.chainfire.tv.sideloadlauncher";
    public static final Target[] MORE_TARGETS = {
        new Target("Kinegra Cam", "local.shieldcam", "Camera and photos"),
        new Target("Kinegra ShieldAmp", "org.local.tegramilkdrop", "Music visualizer"),
        new Target("Sideload Launcher", MORE_APPS_PACKAGE, "Other installed apps")
    };

    public static boolean isAllowed(String packageName) {
        if (packageName == null) return false;
        for (Target target : TARGETS) {
            if (target.packageName.equals(packageName)) return true;
        }
        for (Target target : MORE_TARGETS) {
            if (target.packageName.equals(packageName)) return true;
        }
        return false;
    }
}
