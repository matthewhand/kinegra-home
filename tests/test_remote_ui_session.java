package tv.metaxy.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Offline local UI harness for PRODUCTION RemoteUiSession.
 * No Android runtime or emulator. Simulates 8 focusable View surfaces,
 * KeyEvent down/up/repeats, click -> exact registered app, pause/resume,
 * unavailable intents and system-Home pass-through.
 */
public final class test_remote_ui_session {
    private static int checks;
    private static void eq(int want, int got) {
        checks++;
        if (want != got)
            throw new AssertionError("assertion " + checks + " expected="
                    + want + " actual=" + got);
    }
    private static void yes(boolean pass) {
        checks++;
        if (!pass) throw new AssertionError("assertion " + checks + " false");
    }
    static final class FakeUi implements RemoteUiSession.ViewPort {
        final boolean[] enabled = new boolean[8];
        final List<String> launches = new ArrayList<String>();
        int focus = -1;
        int exits;
        int focusChanges;
        int clicks;
        boolean denyFocus;
        FakeUi() {
            for (int i = 0; i < enabled.length; i++) enabled[i] = true;
        }
        @Override public int focusedIndex() { return focus; }
        @Override public boolean isEnabled(int i) { return enabled[i]; }
        @Override public boolean requestFocus(int i) {
            if (!enabled[i] || denyFocus) return false;
            focus = i; focusChanges++;
            return true;
        }
        @Override public boolean activate(int i) {
            if (!enabled[i] || i != focus)
                throw new AssertionError("illegal click index " + i);
            clicks++;
            if (i < LaunchRegistry.TARGETS.length) {
                String pkg = LaunchRegistry.TARGETS[i].packageName;
                if (!LaunchRegistry.isAllowed(pkg)) throw new AssertionError("allowlist");
                launches.add(pkg);
            } else {
                launches.add(i == 6 ? "android.settings.SETTINGS"
                                    : "android.settings.DREAM_SETTINGS");
            }
            return true;
        }
        @Override public void exitDashboard() { exits++; }
        void setUnavailable(int index) { enabled[index] = false; }
    }
    static final class Harness {
        final FakeUi view = new FakeUi();
        final RemoteUiSession session = new RemoteUiSession(view);
        int remembered = -1;
        void resume() {
            session.resetHeldKeys();
            remembered = session.restore(remembered);
        }
        void pause() {
            if (view.focus >= 0) remembered = view.focus;
            session.resetHeldKeys();
        }
        long clock = 10000L;
        final Map<Integer,Long> pressedSince = new HashMap<Integer,Long>();
        int down(int key) {
            if (!pressedSince.containsKey(key)) pressedSince.put(key,clock+=500L);
            long t=pressedSince.get(key);
            return session.dispatch(100,key,RemoteUiSession.ACTION_DOWN,0,
                    false,t,clock++);
        }
        int repeat(int key) {
            long t=pressedSince.containsKey(key)?pressedSince.get(key):clock;
            return session.dispatch(100,key,RemoteUiSession.ACTION_DOWN,3,
                    false,t,clock++);
        }
        int up(int key) {
            long t=pressedSince.containsKey(key)?pressedSince.remove(key):clock;
            return session.dispatch(100,key,RemoteUiSession.ACTION_UP,0,
                    false,t,clock++);
        }
        int canceledDown(int key) {
            return session.dispatch(100,key,RemoteUiSession.ACTION_DOWN,0,
                    true,clock,clock++);
        }
        void press(int key) {
            int before=view.clicks;
            eq(RemoteUiSession.CONSUMED,down(key));
            eq(RemoteUiSession.CONSUMED,up(key));
            // A successfully launched app will pause Dashboard and later
            // resume it; model that lifecycle before another shortcut.
            if (view.clicks>before) {pause();resume();}
        }
    }
    private static void openingAndRealShortcuts() {
        Harness h = new Harness();
        h.resume();
        eq(0,h.view.focus); // YouTube
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        eq(1,h.view.clicks);
        yes(h.view.launches.get(0).equals("com.google.android.youtube.tv"));
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(1,h.view.focus); // Omega Prime
        h.press(RemoteUiSession.KEY_ENTER);
        yes(h.view.launches.get(1).equals("au.private.omegaprime.tegra"));
        h.pause();
        h.resume();
        eq(1,h.view.focus); // from app
        h.press(RemoteUiSession.KEY_BUTTON_A);
        eq(3,h.view.clicks);
        yes(h.view.launches.get(2).equals("au.private.omegaprime.tegra"));
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(3).equals("com.plexapp.android"));
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(4).equals("eu.chainfire.tv.sideloadlauncher"));
        h.press(RemoteUiSession.KEY_DPAD_LEFT);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(5).equals("com.nvidia.tegrazone3"));
        h.press(RemoteUiSession.KEY_DPAD_LEFT);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(6).equals("com.valvesoftware.steamlink"));
    }
    private static void navigationAndMissingTiles() {
        Harness h = new Harness();
        h.view.setUnavailable(0); // YouTube missing
        h.resume();
        eq(1,h.view.focus); // Omega, not a disabled tile
        h.press(RemoteUiSession.KEY_DPAD_LEFT);
        eq(1,h.view.focus); // no left option
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        eq(4,h.view.focus);
        h.view.setUnavailable(4); // app removed while away
        h.pause();
        h.resume();
        eq(1,h.view.focus); // restores first actually available
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        eq(3,h.view.focus); // nearest enabled in adjacent row
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(0).equals("com.valvesoftware.steamlink"));
        h.view.setUnavailable(6);
        h.view.setUnavailable(7);
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        eq(3,h.view.focus); // cannot accidentally open Settings
        h.view.setUnavailable(3);
        h.view.setUnavailable(5);
        h.view.setUnavailable(1);
        h.view.setUnavailable(2);
        h.pause();
        h.view.focus = -1;
        h.resume();
        eq(-1,h.view.focus); // all unavailable
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        eq(1,h.view.clicks); // still only Steam Link
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(-1,h.view.focus);
    }
    private static void keyRepeatsAndCancel() {
        Harness h = new Harness();
        h.resume();
        eq(0,h.view.focus);
        eq(RemoteUiSession.CONSUMED,h.down(RemoteUiSession.KEY_DPAD_RIGHT));
        eq(1,h.view.focus);
        eq(RemoteUiSession.CONSUMED,h.repeat(RemoteUiSession.KEY_DPAD_RIGHT));
        eq(RemoteUiSession.CONSUMED,h.down(RemoteUiSession.KEY_DPAD_RIGHT));
        eq(1,h.view.focus);
        h.up(RemoteUiSession.KEY_DPAD_RIGHT);
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(2,h.view.focus);
        eq(RemoteUiSession.CONSUMED,h.canceledDown(RemoteUiSession.KEY_DPAD_LEFT));
        eq(2,h.view.focus);
        h.up(RemoteUiSession.KEY_DPAD_LEFT);
        h.press(RemoteUiSession.KEY_DPAD_LEFT);
        eq(1,h.view.focus);
        // No repeated launch from held OK.
        h.down(RemoteUiSession.KEY_DPAD_CENTER);
        eq(0,h.view.clicks); // activation deferred until valid release
        h.repeat(RemoteUiSession.KEY_DPAD_CENTER);
        h.down(RemoteUiSession.KEY_DPAD_CENTER);
        eq(0,h.view.clicks);
        h.up(RemoteUiSession.KEY_DPAD_CENTER);
        eq(1,h.view.clicks);
        h.pause(); h.resume();
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        eq(2,h.view.clicks);
        // B/Back only exit once per press, not on held repeats.
        h.down(RemoteUiSession.KEY_BUTTON_B);
        eq(0,h.view.exits);
        h.repeat(RemoteUiSession.KEY_BUTTON_B);
        h.down(RemoteUiSession.KEY_BUTTON_B);
        eq(0,h.view.exits);
        h.up(RemoteUiSession.KEY_BUTTON_B);
        eq(1,h.view.exits);
        h.press(RemoteUiSession.KEY_BACK);
        eq(1,h.view.exits); // idempotent pending exit
    }
    private static void androidSystemHomeAndFocusFailure() {
        Harness h = new Harness();
        h.resume();
        int before=h.view.focus;
        eq(RemoteUiSession.PASS_THROUGH,h.down(RemoteUiSession.KEY_HOME));
        eq(RemoteUiSession.PASS_THROUGH,h.up(RemoteUiSession.KEY_HOME));
        eq(RemoteUiSession.PASS_THROUGH,h.down(999));
        eq(before,h.view.focus);
        eq(0,h.view.exits);
        eq(0,h.view.clicks);
        h.view.denyFocus=true;
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(before,h.view.focus); // requestFocus rejected, no accidental click
        h.view.denyFocus=false;
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(1,h.view.focus);
        // Pressed key interrupted by app pause is reset on resume.
        h.down(RemoteUiSession.KEY_DPAD_LEFT);
        eq(0,h.view.focus);
        h.pause();h.resume();
        eq(RemoteUiSession.CONSUMED,h.down(RemoteUiSession.KEY_DPAD_RIGHT));
        eq(1,h.view.focus);
        h.up(RemoteUiSession.KEY_DPAD_RIGHT);
    }
    private static void systemSettingsAreDeliberate() {
        Harness h = new Harness();
        h.resume();
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        eq(3,h.view.focus);
        h.press(RemoteUiSession.KEY_DPAD_DOWN);
        eq(6,h.view.focus);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(0).equals("android.settings.SETTINGS"));
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(7,h.view.focus);
        h.press(RemoteUiSession.KEY_DPAD_CENTER);
        yes(h.view.launches.get(1).equals("android.settings.DREAM_SETTINGS"));
        h.press(RemoteUiSession.KEY_DPAD_RIGHT);
        eq(7,h.view.focus); // no wrap beyond settings
        h.press(RemoteUiSession.KEY_BACK);
        eq(1,h.view.exits);
    }
    private static void offlineInventoryFuzz() {
        for (int mask=0;mask<256;mask++) {
            Harness h = new Harness();
            for (int i=0;i<8;i++) h.view.enabled[i]=(mask&(1<<i))!=0;
            h.resume();
            for (int key=RemoteUiSession.KEY_DPAD_UP;
                 key<=RemoteUiSession.KEY_DPAD_CENTER;key++)
 {
                int n=h.down(key);
                eq(RemoteUiSession.CONSUMED,n);
                yes(h.view.focus==-1 ||
                    (h.view.focus>=0 && h.view.focus<8 &&
                     h.view.enabled[h.view.focus]));
                h.up(key);
            }
        }
    }
    /** Controller stick steps share the exact remote focus graph. */
    private static void joystickFollowsRemotePolicy() {
        for (int mask=0;mask<256;mask++) {
            for (int from=0;from<8;from++) {
                for (int direction=RemoteFocusPolicy.LEFT;
                        direction<=RemoteFocusPolicy.DOWN;direction++) {
                    FakeUi view=new FakeUi();
                    for (int i=0;i<8;i++)
                        view.enabled[i]=(mask & (1<<i)) != 0;
                    view.focus=from;
                    RemoteUiSession session=new RemoteUiSession(view);
                    int expected=RemoteFocusPolicy.next(from,direction,view.enabled);
                    session.navigateJoystick(direction);
                    eq(expected>=0 && expected!=from ? expected : from,
                            view.focus);
                    eq(0,view.clicks);
                    eq(0,view.exits);
                }
            }
        }
        FakeUi view=new FakeUi();
        view.focus=5;
        RemoteUiSession session=new RemoteUiSession(view);
        session.navigateJoystick(-1);
        session.navigateJoystick(999);
        eq(5,view.focus); // invalid step never navigates
        view.enabled[6]=false;
        view.enabled[7]=false;
        session.navigateJoystick(RemoteFocusPolicy.DOWN);
        eq(5,view.focus); // cannot leap to unavailable system tools
        view.denyFocus=true;
        session.navigateJoystick(RemoteFocusPolicy.LEFT);
        eq(5,view.focus); // failed Android focus request leaves selection
    }

    public static void main(String[] args) {
        joystickFollowsRemotePolicy();
        openingAndRealShortcuts();
        navigationAndMissingTiles();
        keyRepeatsAndCancel();
        androidSystemHomeAndFocusFailure();
        systemSettingsAreDeliberate();
        offlineInventoryFuzz();
        System.out.println("REMOTE_UI_SESSION_INTERACTION_PASS assertions="+checks);
    }
}
