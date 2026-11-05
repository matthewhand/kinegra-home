package tv.metaxy.dashboard;
import java.util.HashSet;
public final class test_launch_registry {
    public static void main(String[] args) {
        HashSet<String> names = new HashSet<>();
        int count = 0;
        for (LaunchRegistry.Target t : LaunchRegistry.TARGETS) {
            if (!names.add(t.packageName)) throw new AssertionError("duplicate");
            if (!LaunchRegistry.isAllowed(t.packageName)) throw new AssertionError("missing");
            if (t.description.isEmpty()) throw new AssertionError("description");
            count++;
        }
        if (LaunchRegistry.MORE_TARGETS.length != 3
                || !LaunchRegistry.isAllowed("local.shieldcam")
                || !LaunchRegistry.isAllowed("org.local.tegramilkdrop")
                || !LaunchRegistry.isAllowed("eu.chainfire.tv.sideloadlauncher"))
            throw new AssertionError("More apps targets unavailable");
        if (count != 6) throw new AssertionError("targets="+count);
        if (LaunchRegistry.isAllowed("com.google.android.tvlauncher")
           || LaunchRegistry.isAllowed("not.in.registry")
           || LaunchRegistry.isAllowed(null)) throw new AssertionError("unexpected allowance");
        System.out.println("REAL_LAUNCH_REGISTRY_TEST_PASS " + count);
    }
}
