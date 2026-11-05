package tv.metaxy.dashboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for Android TV remote/controller key interaction.
 * ViewPort is implemented by the REAL DashboardActivity and a local JVM fake.
 * This class does not manufacture Android input, open apps or control Home.
 */
public final class RemoteUiSession {
    public static final int PASS_THROUGH = 0;
    public static final int CONSUMED = 1;
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int KEY_BACK = 4;
    public static final int KEY_HOME = 3;
    public static final int KEY_DPAD_UP = 19;
    public static final int KEY_DPAD_DOWN = 20;
    public static final int KEY_DPAD_LEFT = 21;
    public static final int KEY_DPAD_RIGHT = 22;
    public static final int KEY_DPAD_CENTER = 23;
    public static final int KEY_ENTER = 66;
    public static final int KEY_BUTTON_A = 96;
    public static final int KEY_BUTTON_B = 97;

    // All bounded; no background polling. Android KeyEvent timestamps are
    // uptime milliseconds, NOT wall clock time.
    static final int MAX_HELD_KEYS = 24;
    static final int MAX_PENDING_DEVICES = 8;
    static final long STALE_KEY_MILLIS = 2500L;
    static final long LAUNCH_REARM_MILLIS = 3500L;

    public interface ViewPort {
        int focusedIndex();
        boolean isEnabled(int index);
        boolean requestFocus(int index);
        /** True ONLY if external Activity launch succeeded; false on failure. */
        boolean activate(int index);
        void exitDashboard();
    }

    private static final class HeldKey {
        final int deviceId;
        final int keyCode;
        final long downTime;
        long lastEventTime;
        HeldKey(int deviceId, int keyCode, long downTime, long lastEventTime) {
            this.deviceId = deviceId;
            this.keyCode = keyCode;
            this.downTime = downTime;
            this.lastEventTime = lastEventTime;
        }
    }
    private static final class Confirmation {
        final Set<Integer> keys = new HashSet<Integer>();
        // Bind OK/Enter/A to the tile under focus on first press. Never
        // activate a different tile because another controller moved focus.
        final int focusedAtDown;
        boolean canceled;
        Confirmation(int focus) { focusedAtDown = focus; }
    }
    private final ViewPort port;
    private final int columns,targetCount;
    private final Map<Long, HeldKey> heldKeys = new HashMap<Long, HeldKey>();
    private final Map<Integer, Confirmation> confirmations =
            new HashMap<Integer, Confirmation>();
    private boolean exitRequested;
    private boolean launchRequested;
    private long lastLaunchEventTime = -1L;

    public RemoteUiSession(ViewPort port) {
        this(port,3);
    }

    public RemoteUiSession(ViewPort port,int columns) {this(port,columns,8);}
    public RemoteUiSession(ViewPort port,int columns,int targetCount) {
        if(targetCount!=8&&targetCount!=10)throw new IllegalArgumentException("Invalid target count");
        this.targetCount=targetCount;
        if (port == null) throw new IllegalArgumentException("port required");
        if(columns!=2&&columns!=3)throw new IllegalArgumentException("Invalid column count");
        this.port = port;
        this.columns=columns;
    }

    /** Lifecycle boundary. A fresh screen may accept a new click or exit. */
    public void resetHeldKeys() {
        heldKeys.clear();
        confirmations.clear();
        exitRequested = false;
        launchRequested = false;
        lastLaunchEventTime = -1L;
    }

    /** Hotplug callback: drop only the affected controller's input state. */
    public void resetDevice(int deviceId) {
        Iterator<Map.Entry<Long, HeldKey>> it = heldKeys.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().deviceId == deviceId) it.remove();
        }
        confirmations.remove(deviceId);
    }

    /**
     * Do not discard selection when window not focusable yet. Caller retries
     * on window-focus gained. Disabled remembered tiles fall back safely.
     */
    public int restore(int remembered) {
        boolean[] enabled = enabledTargets();
        int current = port.focusedIndex();
        // Prefer the explicitly remembered user selection on resume.
        // Framework focus may temporarily jump to the first View.
        int preferred = remembered >= 0 && remembered < enabled.length && enabled[remembered]
                ? remembered : current;
        int destination = RemoteFocusPolicy.restore(preferred, enabled);
        if (destination >= 0 && destination != current) {
            if (!port.requestFocus(destination)) return destination;
        }
        return destination;
    }

    /**
     * A real Android event supplies device ID, downTime and eventTime.
     * Never derive this from an arbitrary clock; no touch/remote injection.
     */
    public int dispatch(int deviceId, int keyCode, int action, int repeats,
                        boolean canceled, long downTime, long eventTime) {
        if (!handled(keyCode)) return PASS_THROUGH; // HOME remains system-owned
        if (downTime < 0 || eventTime < downTime) return CONSUMED;

        long key = (((long) deviceId) << 32) | (keyCode & 0xffffffffL);
        HeldKey existing = heldKeys.get(key);
        // Only the EXACT live key+downTime may be exempted from staleness:
        // its matching release or genuine held repeat proves it is alive.
        // Expiring another held key must never cancel this confirmation.
        boolean matching = existing != null && existing.downTime == downTime;
        boolean validRelease = action == ACTION_UP && matching;
        boolean validRepeat = action == ACTION_DOWN && repeats > 0 && matching;
        pruneExpired(eventTime, validRelease || validRepeat ? key : null,
                validRelease && isConfirm(keyCode) ? deviceId : null);
        existing = heldKeys.get(key);
        if (action == ACTION_UP) {
            // A stray KeyUp from a different gesture may not reset a newer key
            // or accidentally click an old confirm.
            if (existing == null || existing.downTime != downTime) return CONSUMED;
            heldKeys.remove(key);
            if (isConfirm(keyCode)) finishConfirmation(deviceId, keyCode,
                    canceled, eventTime);
            else if (isExit(keyCode) && !canceled && !exitRequested) {
                exitRequested = true;
                port.exitDashboard();
            }
            return CONSUMED;
        }
        if (action == ACTION_DOWN && repeats > 0) {
            if (existing != null && existing.downTime == downTime &&
                    eventTime >= existing.lastEventTime) {
                existing.lastEventTime = eventTime;
            }
            return CONSUMED;
        }
        if (action != ACTION_DOWN || canceled) return CONSUMED;

        // A device can reconnect with the same Android ID. A genuinely fresh
        // downTime must rearm without waiting for a missing ACTION_UP.
        if (existing != null) {
            if (downTime < existing.downTime ||
                    (downTime == existing.downTime &&
                     eventTime - existing.lastEventTime < STALE_KEY_MILLIS))
                return CONSUMED;
            heldKeys.remove(key);
            if (isConfirm(keyCode)) expireConfirmationKey(deviceId, keyCode);
        }
        if (heldKeys.size() >= MAX_HELD_KEYS) return CONSUMED;
        heldKeys.put(key, new HeldKey(deviceId, keyCode, downTime, eventTime));
        if (isConfirm(keyCode)) {
            Confirmation confirmation = confirmations.get(deviceId);
            if (confirmation == null) {
                if (confirmations.size() >= MAX_PENDING_DEVICES) {
                    heldKeys.remove(key);
                    return CONSUMED;
                }
                confirmation = new Confirmation(port.focusedIndex());
                confirmations.put(deviceId, confirmation);
            }
            confirmation.keys.add(keyCode);
        } else {
            switch (keyCode) {
                case KEY_DPAD_LEFT: move(RemoteFocusPolicy.LEFT); break;
                case KEY_DPAD_RIGHT: move(RemoteFocusPolicy.RIGHT); break;
                case KEY_DPAD_UP: move(RemoteFocusPolicy.UP); break;
                case KEY_DPAD_DOWN: move(RemoteFocusPolicy.DOWN); break;
                default: break; // Exit deferred until valid non-canceled up.
            }
        }
        return CONSUMED;
    }

    /**
     * Prune per-key, never per-device. A stale direction on a controller
     * cannot cancel a new OK on that same controller. No timers, no IO.
     */
    private void pruneExpired(long eventTime, Long exemptKey,
                              Integer releasingConfirmationDevice) {
        Iterator<Map.Entry<Long, HeldKey>> it = heldKeys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, HeldKey> entry = it.next();
            HeldKey state = entry.getValue();
            // An alias still physically held alongside this matching key-up
            // belongs to the same gesture. Do not expire it mid-release.
            Confirmation activeGroup = releasingConfirmationDevice == null
                    ? null : confirmations.get(releasingConfirmationDevice);
            boolean liveSibling = activeGroup != null &&
                    state.deviceId == releasingConfirmationDevice &&
                    isConfirm(state.keyCode) &&
                    activeGroup.keys.contains(state.keyCode);
            if (!liveSibling &&
                    (exemptKey == null || !exemptKey.equals(entry.getKey())) &&
                    eventTime >= state.lastEventTime &&
                    eventTime - state.lastEventTime > STALE_KEY_MILLIS) {
                it.remove();
                if (isConfirm(state.keyCode))
                    expireConfirmationKey(state.deviceId, state.keyCode);
            }
        }
    }

    private void expireConfirmationKey(int deviceId, int keyCode) {
        Confirmation group = confirmations.get(deviceId);
        if (group == null) return;
        // An expired confirmation alias aborts its own composite gesture.
        // An expired *direction* must not touch active confirmations.
        if (group.keys.remove(keyCode)) group.canceled = true;
        if (group.keys.isEmpty()) confirmations.remove(deviceId);
    }

    private void finishConfirmation(int deviceId, int keyCode,
                                    boolean canceled, long eventTime) {
        Confirmation group = confirmations.get(deviceId);
        if (group == null || !group.keys.remove(keyCode)) return;
        if (canceled) group.canceled = true;
        // Commit only after EVERY overlapping alias has finished. If any
        // alias is canceled, none of the composite gesture can launch.
        if (group.keys.isEmpty()) {
            confirmations.remove(deviceId);
            boolean launchLatchActive = launchRequested &&
                    (lastLaunchEventTime < 0 ||
                     eventTime < lastLaunchEventTime ||
                     eventTime - lastLaunchEventTime < LAUNCH_REARM_MILLIS);
            if (!group.canceled && !launchLatchActive) {
                int focused = port.focusedIndex();
                if (focused == group.focusedAtDown &&
                        focused >= 0 && focused < targetCount &&
                        port.isEnabled(focused)) {
                    // An unsuccessful startActivity must NOT lock the UI.
                    if (port.activate(focused)) {
                        launchRequested = true;
                        lastLaunchEventTime = eventTime;
                    }
                }
            }
        }
    }

    /**
     * Route a deliberate controller stick step through exactly the same
     * enabled-tile boundaries as physical remote D-pad keys. Android View
     * focusSearch can jump across disabled rows or into system tools.
     * ControllerMotionPolicy already enforces one step per deflection.
     */
    public void navigateJoystick(int direction) {
        if (direction < RemoteFocusPolicy.LEFT ||
                direction > RemoteFocusPolicy.DOWN) return;
        move(direction);
    }

    private void move(int direction) {
        int from = port.focusedIndex();
        int next = RemoteFocusPolicy.next(from, direction, enabledTargets(),columns);
        if (next >= 0 && next != from && port.requestFocus(next)
                && port.focusedIndex() == next) {
            // Any successful D-pad or joystick navigation invalidates
            // every still-held confirmation, including from other devices.
            // Returning to the original tile does not rearm that gesture.
            for (Confirmation group : confirmations.values())
                group.canceled = true;
        }
    }

    private boolean[] enabledTargets() {
        boolean[] enabled = new boolean[targetCount];
        for (int i = 0; i < enabled.length; i++) enabled[i] = port.isEnabled(i);
        return enabled;
    }
    private static boolean isConfirm(int key) {
        return key == KEY_DPAD_CENTER || key == KEY_ENTER || key == KEY_BUTTON_A;
    }
    private static boolean isExit(int key) {
        return key == KEY_BACK || key == KEY_BUTTON_B;
    }
    private static boolean handled(int key) {
        switch (key) {
            case KEY_DPAD_LEFT: case KEY_DPAD_RIGHT:
            case KEY_DPAD_UP: case KEY_DPAD_DOWN:
            case KEY_DPAD_CENTER: case KEY_ENTER: case KEY_BUTTON_A:
            case KEY_BACK: case KEY_BUTTON_B:
                return true;
            default: return false;
        }
    }
}
