package tv.metaxy.dashboard;

import java.util.HashSet;
import java.util.Set;

/**
 * One deliberate LEFT-STICK excursion -> at most one focus step, per device.
 *
 * Android synthesizes D-pad KeyEvents from some joystick hat axes. Therefore
 * hat motion MUST NOT also be converted into native View focus here.
 * Actual D-pad keys remain owned by Android's ordinary View dispatch.
 * No triggers are sampled, inputs are not recorded, no background listeners.
 */
public final class ControllerMotionPolicy {
    public static final int NONE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;

    static final int MAX_TRACKED_DEVICES = 8;
    private static final float ACTIVATE = 0.68f;
    private static final float RELEASE = 0.35f;
    private static final float HAT_ACTIVE = 0.50f;
    private final Set<Integer> latched = new HashSet<>();

    /**
     * Returns a single focus direction for a fresh left stick deflection.
     * AXIS_HAT_X/Y are suppression-only: native D-pad key navigation owns them.
     */
    public int step(int deviceId, float stickX, float stickY,
                    float hatX, float hatY) {
        if (deviceId < 0) return NONE;
        if (!finite(stickX) || !finite(stickY)
                || !finite(hatX) || !finite(hatY)) {
            // An invalid intermediate frame must not rearm a held stick:
            // a valid full-neutral sample or device reset is required.
            return NONE;
        }
        boolean neutral = Math.abs(stickX) <= RELEASE
                && Math.abs(stickY) <= RELEASE;
        if (latched.contains(deviceId)) {
            // A real neutral stick clears the latch even with HAT input.
            // Opposite directions, HAT motion and analog jitter do not.
            if (neutral) resetDevice(deviceId);
            return NONE;
        }
        // Android may synthesize DPAD keys from HAT axes. When the stick
        // is deflected at the same time, consume that entire excursion:
        // do not defer a second move when the HAT becomes neutral.
        if (Math.abs(hatX) >= HAT_ACTIVE || Math.abs(hatY) >= HAT_ACTIVE) {
            if (!neutral && (Math.abs(stickX) >= ACTIVATE ||
                    Math.abs(stickY) >= ACTIVATE)
                    && latched.size() < MAX_TRACKED_DEVICES) {
                latched.add(deviceId);
            }
            return NONE;
        }
        if (Math.abs(stickX) < ACTIVATE && Math.abs(stickY) < ACTIVATE)
            return NONE;
        // Refuse new devices when the bounded inventory is full; do not
        // evict a held device and thereby create a repeated navigation step.
        if (latched.size() >= MAX_TRACKED_DEVICES) return NONE;
        latched.add(deviceId);
        if (Math.abs(stickX) >= Math.abs(stickY)) {
            return stickX >= 0 ? RIGHT : LEFT;
        }
        return stickY >= 0 ? DOWN : UP;
    }

    private static boolean finite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    /** Call for this exact Android input device id on add/remove/change. */
    public void resetDevice(int deviceId) {
        latched.remove(deviceId);
    }

    /** Call on activity pause/resume; no controller state leaks across scenes. */
    public void reset() {
        latched.clear();
    }

    int trackedDeviceCount() {
        return latched.size();
    }
}
