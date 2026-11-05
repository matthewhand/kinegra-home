package tv.metaxy.proof;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.InputDevice;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextClock;
import android.widget.TextView;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.TypedValue;
import java.util.ArrayList;
import java.util.List;
import tv.metaxy.dashboard.LaunchRegistry;
import tv.metaxy.dashboard.RemoteFocusPolicy;
import tv.metaxy.dashboard.RemoteUiSession;
import tv.metaxy.dashboard.ControllerMotionPolicy;
import tv.metaxy.dashboard.DashboardPresentationPolicy;
import tv.metaxy.dashboard.ModalControllerPolicy;
import tv.metaxy.dashboard.DiagnosticPresentation;

/** Real, D-pad accessible TV launcher: starts installed apps only on a user click. */
public final class DashboardActivity extends Activity {
    private static final int BG = Color.rgb(7, 16, 25);
    private static final int PANEL = Color.rgb(18, 37, 48);
    private static final int MUTED = Color.rgb(172, 192, 207);
    private static final int ACCENT = Color.rgb(107, 227, 184);
    private static final String FOCUS_STATE_KEY = "shieldarchy.focus.index";
    private final List<Runnable> refreshers = new ArrayList<>();
    private final List<View> focusTargets = new ArrayList<>();
    private int rememberedFocusIndex = -1;
    private boolean entryFocusPending;
    private boolean launchStarted;
    private final RemoteUiSession remoteUi = new RemoteUiSession(
            new RemoteUiSession.ViewPort() {
        @Override public int focusedIndex() {
            return focusTargets.indexOf(getCurrentFocus());
        }
        @Override public boolean isEnabled(int index) {
            return index >= 0 && index < focusTargets.size()
                    && focusTargets.get(index).isEnabled();
        }
        @Override public boolean requestFocus(int index) {
            if (index < 0 || index >= focusTargets.size()) return false;
            View target = focusTargets.get(index);
            if (!target.isEnabled()) return false;
            boolean changed = target.requestFocus();
            if (changed) rememberedFocusIndex = index;
            return changed;
        }
        @Override public boolean activate(int index) {
            if (index < 0 || index >= focusTargets.size()) return false;
            View selected = focusTargets.get(index);
            if (!selected.isEnabled()) return false;
            launchStarted = false;
            selected.performClick();
            // Only a successful startActivity may hold the launch latch.
            return launchStarted;
        }
        @Override public void exitDashboard() { finish(); }
    },2,10);
    private TextView message;
    private ControllerIcons controllerIcons;private View originalHomeButton,settingsHeaderButton;
    private final android.os.Handler tipHandler=new android.os.Handler();private int tipIndex;private long tipResumeAt;
    private static final String[] TIPS={"Tip: Hold Back for 1+ seconds to open pane controls.","Tip: Back + OK swaps views. Controller: Select + Start.","Tip: Shield opens the built-in NVIDIA launcher.","Tip: Left / Right adjusts the split slider. OK applies.","Tip: Resizing a browser can reload its page; switching focus keeps its size.","Tip: About / Donate includes feedback, updates and device details."};
    private final Runnable rotateTip=new Runnable(){public void run(){if(message!=null&&android.os.SystemClock.uptimeMillis()>=tipResumeAt){tipIndex=(tipIndex+1)%TIPS.length;message.setText(TIPS[tipIndex]);}tipHandler.postDelayed(this,8000);}};
    private final ControllerMotionPolicy joystickFocus = new ControllerMotionPolicy();
    private final tv.metaxy.dashboard.ControllerHatPolicy hatFocus = new tv.metaxy.dashboard.ControllerHatPolicy();
    private InputManager inputManager;
    private boolean inputListenerRegistered;
    private final InputManager.InputDeviceListener deviceListener =
            new InputManager.InputDeviceListener() {
        @Override public void onInputDeviceAdded(int deviceId) {
            joystickFocus.resetDevice(deviceId);
            hatFocus.resetDevice(deviceId);
            remoteUi.resetDevice(deviceId);
            refreshInputIcons();
        }
        @Override public void onInputDeviceRemoved(int deviceId) {
            joystickFocus.resetDevice(deviceId);
            hatFocus.resetDevice(deviceId);
            remoteUi.resetDevice(deviceId);
            refreshInputIcons();
        }
        @Override public void onInputDeviceChanged(int deviceId) {
            joystickFocus.resetDevice(deviceId);
            hatFocus.resetDevice(deviceId);
            remoteUi.resetDevice(deviceId);
            refreshInputIcons();
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        // Font-scale and TV configuration changes recreate the Activity.
        // Restore the logical selection before building its eight views.
        if (state != null) {
            rememberedFocusIndex = state.getInt(FOCUS_STATE_KEY, -1);
        }
        inputManager = (InputManager) getSystemService(INPUT_SERVICE);
        if (RemoteUiSession.KEY_BUTTON_A != KeyEvent.KEYCODE_BUTTON_A
                || RemoteUiSession.KEY_BUTTON_B != KeyEvent.KEYCODE_BUTTON_B
                || RemoteUiSession.KEY_HOME != KeyEvent.KEYCODE_HOME) {
            throw new IllegalStateException("Android remote keycode mismatch");
        }
        buildDashboard();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        // Android need not preserve focus for our anonymous programmatic
        // Views; save the logical index, not a stale View or scroll offset.
        int focusedIndex = focusTargets.indexOf(getCurrentFocus());
        if (focusedIndex >= 0 && focusTargets.get(focusedIndex).isEnabled()) {
            rememberedFocusIndex = focusedIndex;
        }
        super.onSaveInstanceState(outState);
        outState.putInt(FOCUS_STATE_KEY, rememberedFocusIndex);
    }

    @Override public void onResume() {
        super.onResume();
        CanvasTaskHistory.trim(this);
        if (inputManager != null && !inputListenerRegistered) {
            inputManager.registerInputDeviceListener(deviceListener, null);
            inputListenerRegistered = true;
        }
        joystickFocus.reset();
        hatFocus.reset();
        if(controllerIcons!=null)controllerIcons.resetActivity();
        remoteUi.resetHeldKeys();
        for (Runnable update : refreshers) update.run();
        selectSplitOnEntry();
        refreshInputIcons();
        tipHandler.removeCallbacks(rotateTip);tipHandler.postDelayed(rotateTip,8000);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && focusTargets.size() == 10) {
            if(entryFocusPending)postEntryFocus();
            else rememberedFocusIndex = remoteUi.restore(rememberedFocusIndex);
        }
    }

    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);selectSplitOnEntry();}
    private void selectSplitOnEntry(){entryFocusPending=true;rememberedFocusIndex=0;postEntryFocus();}
    private void postEntryFocus(){getWindow().getDecorView().post(()->{if(!entryFocusPending||isFinishing()||isDestroyed()||!hasWindowFocus()||focusTargets.isEmpty())return;rememberedFocusIndex=0;if(focusTargets.get(0).requestFocus())entryFocusPending=false;});}

    @Override protected void onPause() {
        int focusedIndex = focusTargets.indexOf(getCurrentFocus());
        if (focusedIndex >= 0) rememberedFocusIndex = focusedIndex;
        if (inputManager != null && inputListenerRegistered) {
            inputManager.unregisterInputDeviceListener(deviceListener);
            inputListenerRegistered = false;
        }
        joystickFocus.reset();
        hatFocus.reset();
        if(controllerIcons!=null)controllerIcons.resetActivity();
        remoteUi.resetHeldKeys();
        tipHandler.removeCallbacks(rotateTip);
        super.onPause();
    }

    /**
     * Actual Android Activity uses the SAME focus/activation/key-dispatch
     * coordinator as offline local UI harness. HOME and unknown keys pass
     * to the system unchanged. One press/release -> at most one action.
     */
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        inputActivity(event.getDevice(),event.getAction()==KeyEvent.ACTION_DOWN);
        int direction=dpadDirection(event.getKeyCode());
        if(isAsusJoystickDevice(event.getDevice())&&direction!=0){if(event.isCanceled())hatFocus.resetDevice(event.getDeviceId());else if(hatFocus.key(event.getDeviceId(),direction,event.getAction()))return true;}
        int result = remoteUi.dispatch(event.getDeviceId(), event.getKeyCode(),
                event.getAction(), event.getRepeatCount(), event.isCanceled(),
                event.getDownTime(), event.getEventTime());
        if (result == RemoteUiSession.CONSUMED) return true;
        return super.dispatchKeyEvent(event);
    }

    /** Hat-only D-pads need explicit navigation because this handler consumes motion. */
    @Override public boolean onGenericMotionEvent(MotionEvent event) {
        int source = event.getSource();
        if ((source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
            int deviceId = event.getDeviceId();
            InputDevice device = InputDevice.getDevice(deviceId);
            if (!isAsusJoystickDevice(device)) {
                joystickFocus.resetDevice(deviceId);
                // Non-ASUS joystick motions must not navigate the
                // Dashboard through a platform fallback either.
                // Ordinary remote/gamepad D-pad KeyEvents are unaffected.
                return true;
            }
            // Handle hat-only D-pads, coalescing drivers that also emit keys.
            // The stick policy suppresses hats; the hat policy handles their edges.
            float x=event.getAxisValue(MotionEvent.AXIS_X),y=event.getAxisValue(MotionEvent.AXIS_Y),hx=event.getAxisValue(MotionEvent.AXIS_HAT_X),hy=event.getAxisValue(MotionEvent.AXIS_HAT_Y);
            inputActivity(device,Math.max(Math.abs(x),Math.abs(y))>.2f||Math.max(Math.abs(hx),Math.abs(hy))>=.5f);
            int stick=joystickFocus.step(deviceId,x,y,hx,hy),hat=hatFocus.motion(deviceId,hx,hy);
            int step=hat!=ControllerMotionPolicy.NONE?hat:stick;
            if (step != ControllerMotionPolicy.NONE) {
                // Translate controller input explicitly: these two pure
                // policies must not depend on coincidental enum values.
                int direction;
                switch (step) {
                    case ControllerMotionPolicy.LEFT:
                        direction = RemoteFocusPolicy.LEFT; break;
                    case ControllerMotionPolicy.RIGHT:
                        direction = RemoteFocusPolicy.RIGHT; break;
                    case ControllerMotionPolicy.UP:
                        direction = RemoteFocusPolicy.UP; break;
                    case ControllerMotionPolicy.DOWN:
                        direction = RemoteFocusPolicy.DOWN; break;
                    default: return true;
                }
                remoteUi.navigateJoystick(direction);
            }
            // Prevent Android generic-motion fallback from repeating a
            // held stick or handling hat/trigger motion a second time.
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private static int dpadDirection(int key){switch(key){case KeyEvent.KEYCODE_DPAD_LEFT:return 1;case KeyEvent.KEYCODE_DPAD_RIGHT:return 2;case KeyEvent.KEYCODE_DPAD_UP:return 3;case KeyEvent.KEYCODE_DPAD_DOWN:return 4;default:return 0;}}
    private void inputActivity(InputDevice device,boolean active){if(!active||controllerIcons==null||device==null)return;String name=device.getName().toLowerCase(java.util.Locale.ROOT);int source=device.getSources();boolean keypad=device.getVendorId()==0x1189&&device.getProductId()==0x8890;boolean gamepad=!keypad&&((source&InputDevice.SOURCE_GAMEPAD)==InputDevice.SOURCE_GAMEPAD||(source&InputDevice.SOURCE_JOYSTICK)==InputDevice.SOURCE_JOYSTICK);if(name.contains("remote"))controllerIcons.activity(false);else if(gamepad)controllerIcons.activity(true);else if(!device.isVirtual()&&(source&InputDevice.SOURCE_DPAD)==InputDevice.SOURCE_DPAD)controllerIcons.activity(false);}

    /** LCTECH 1189:8890 and all virtual devices are NOT ASUS. */
    private static boolean isAsusJoystickDevice(InputDevice device) {
        return device != null
                && !device.isVirtual()
                && device.getVendorId() == 0x0b05
                && device.getProductId() == 0x4500
                && (device.getSources() & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK;
    }

    private void refreshInputIcons(){if(controllerIcons!=null)controllerIcons.setContentDescription("Remote and gamepad controls. Icons briefly highlight received input, not connection state.");}

    private int dp(float units) {
        return (int) (units * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable panel(boolean focused) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(focused ? Color.rgb(29, 69, 74) : PANEL);
        shape.setCornerRadius(dp(15));
        shape.setStroke(dp(focused ? 3 : 1),
                focused ? ACCENT : Color.rgb(50, 79, 91));
        return shape;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextSize(sp);
        label.setTextColor(color);
        if (bold) label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return label;
    }

    /**
     * Keep logical 3+3+2 remote focus rows while giving small layouts a
     * horizontal viewport. Android scrolls a newly focused descendant
     * into view, without making the viewport an extra focus target.
     */
    private LinearLayout addResponsiveRow(LinearLayout content, int cells) {
        HorizontalScrollView viewport = new HorizontalScrollView(this);
        viewport.setFillViewport(true);
        viewport.setHorizontalScrollBarEnabled(false);
        viewport.setFocusable(false);
        viewport.setFocusableInTouchMode(false);
        content.addView(viewport, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int widthDp = DashboardPresentationPolicy.rowWidthDp(
                getResources().getConfiguration().screenWidthDp, 30, cells,
                getResources().getConfiguration().fontScale);
        viewport.addView(row, new HorizontalScrollView.LayoutParams(
                dp(widthDp), ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private void buildDashboard() {
        refreshers.clear();
        focusTargets.clear();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        scroll.setClipToPadding(false);
        scroll.setPadding(dp(30), dp(22), dp(30), dp(24));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        boolean stackedHeader=DashboardPresentationPolicy.stackHeader(getResources().getConfiguration().screenWidthDp,getResources().getConfiguration().fontScale)||getResources().getConfiguration().screenWidthDp<900;
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.VERTICAL);content.addView(header);
        TextView heading=text("KINEGRA HOME",32,ACCENT,true);heading.setMaxLines(1);heading.setEllipsize(TextUtils.TruncateAt.END);heading.setContentDescription("Kinegra Home");heading.setAutoSizeTextTypeUniformWithConfiguration(18,32,2,TypedValue.COMPLEX_UNIT_SP);
        TextClock clock = new TextClock(this);
        clock.setFormat12Hour(DashboardPresentationPolicy.clockFormat12Hour(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().fontScale));
        clock.setFormat24Hour(DashboardPresentationPolicy.clockFormat24Hour(
                getResources().getConfiguration().screenWidthDp,
                getResources().getConfiguration().fontScale));
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(20);
        clock.setMaxLines(1);
        clock.setEllipsize(TextUtils.TruncateAt.END);
        clock.setAutoSizeTextTypeUniformWithConfiguration(
                14, 20, 1, TypedValue.COMPLEX_UNIT_SP);
        LinearLayout headerTools=new LinearLayout(this);headerTools.setOrientation(LinearLayout.HORIZONTAL);headerTools.setGravity(Gravity.CENTER_VERTICAL);
        controllerIcons=new ControllerIcons(this);headerTools.addView(controllerIcons,new LinearLayout.LayoutParams(dp(92),dp(48)));
        originalHomeButton=headerButton("Shield",()->{Intent home=StockHome.intent(this);if(home==null){notice("Shield launcher is unavailable; use Android settings to choose Home");return;}try{startActivity(home);launchStarted=true;}catch(ActivityNotFoundException|SecurityException e){notice("Could not open the Shield launcher");}});
        LinearLayout.LayoutParams homeSpace=new LinearLayout.LayoutParams(-2,dp(48));homeSpace.setMargins(dp(10),0,0,0);ActionIcon homeGlyph=new ActionIcon(ActionIcon.HOME);homeGlyph.setBounds(0,0,dp(22),dp(22));((Button)originalHomeButton).setCompoundDrawables(homeGlyph,null,null,null);((Button)originalHomeButton).setCompoundDrawablePadding(dp(6));headerTools.addView(originalHomeButton,homeSpace);
        settingsHeaderButton=settingsCog();LinearLayout.LayoutParams gearSpace=new LinearLayout.LayoutParams(dp(48),dp(48));gearSpace.setMargins(dp(10),0,0,0);headerTools.addView(settingsHeaderButton,gearSpace);
        if(stackedHeader){heading.setGravity(Gravity.CENTER);clock.setGravity(Gravity.CENTER);header.addView(heading,new LinearLayout.LayoutParams(-1,-2));header.addView(clock,new LinearLayout.LayoutParams(-1,-2));LinearLayout.LayoutParams toolsRow=new LinearLayout.LayoutParams(-2,-2);toolsRow.gravity=Gravity.RIGHT;header.addView(headerTools,toolsRow);}
        else{android.widget.FrameLayout bar=new android.widget.FrameLayout(this);header.addView(bar,new LinearLayout.LayoutParams(-1,dp(62)));android.widget.FrameLayout.LayoutParams left=new android.widget.FrameLayout.LayoutParams(-2,-2,Gravity.LEFT|Gravity.CENTER_VERTICAL);bar.addView(heading,left);android.widget.FrameLayout.LayoutParams middle=new android.widget.FrameLayout.LayoutParams(-2,-2,Gravity.CENTER);bar.addView(clock,middle);android.widget.FrameLayout.LayoutParams right=new android.widget.FrameLayout.LayoutParams(-2,-2,Gravity.RIGHT|Gravity.CENTER_VERTICAL);bar.addView(headerTools,right);}
        StockHome.intent(this);

        TextView workspaces = text("WORKSPACES", 17, ACCENT, true);
        workspaces.setPadding(0, dp(6), 0, dp(7));content.addView(workspaces);
        LinearLayout primary=addResponsiveRow(content,2);
        placeHomeCard(primary,homeCard("Split View","Two apps side by side · adjustable ratio",-ActionIcon.SPLIT,()->openWorkspace("split")));
        placeHomeCard(primary,homeCard("Picture in Picture","A large view with a second app in a corner",-ActionIcon.PIP,()->openWorkspace("pip")));
        TextView recents=text("RECENT APPS",17,ACCENT,true);recents.setPadding(0,dp(12),0,dp(7));content.addView(recents);
        LinearLayout recentRow=null;
        for(int n=0;n<4;n++){
            final int slot=n;if(n%2==0)recentRow=addResponsiveRow(content,2);
            HomeCard card=homeCard("No recent app","Open an app from All apps to add it here",android.R.drawable.ic_menu_recent_history,()->{String pkg=RecentApps.at(this,slot);if(pkg!=null)launchPackage(pkg,appLabel(pkg));});
            placeHomeCard(recentRow,card);
            Runnable update=()->{String pkg=RecentApps.at(this,slot);boolean available=pkg!=null&&launchIntent(pkg)!=null;card.setEnabled(available);card.title.setText(available?appLabel(pkg):"No recent app");card.copy.setText(available?"Open app":"Choose an app from All apps");try{card.icon.setImageDrawable(available?getPackageManager().getApplicationIcon(pkg):getDrawable(android.R.drawable.ic_menu_recent_history));}catch(Exception ignored){}card.setContentDescription(card.title.getText()+", "+card.copy.getText());};
            refreshers.add(update);update.run();
        }
        TextView toolsHeading=text("APPS & HELP",17,ACCENT,true);toolsHeading.setPadding(0,dp(12),0,dp(7));content.addView(toolsHeading);
        LinearLayout tools=addResponsiveRow(content,2);
        placeHomeCard(tools,homeCard("All apps","Browse installed TV and Android apps",-ActionIcon.APPS,this::showInstalledApps));
        placeHomeCard(tools,homeCard("About / Donate","Device, controls, feedback, updates and GitHub",-ActionIcon.ABOUT,this::showAbout));

        focusTargets.add(originalHomeButton);focusTargets.add(settingsHeaderButton);
        message = text(TIPS[0],15,MUTED,false);message.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_NONE);
        message.setPadding(dp(3), dp(15), 0, 0);
        content.addView(message);
        setContentView(scroll);
        boolean[] enabled = new boolean[10];
        for (int i = 0; i < enabled.length; i++) {
            enabled[i] = focusTargets.get(i).isEnabled();
        }
        int initial = RemoteFocusPolicy.restore(rememberedFocusIndex, enabled);
        if (initial >= 0) focusTargets.get(initial).requestFocus();
    }

    private android.widget.ImageButton settingsCog(){android.widget.ImageButton b=new android.widget.ImageButton(this);b.setImageDrawable(new ActionIcon(ActionIcon.SETTINGS));b.setScaleType(ImageView.ScaleType.FIT_CENTER);b.setPadding(dp(11),dp(11),dp(11),dp(11));b.setBackground(panel(false));b.setContentDescription("Settings");b.setTooltipText("Settings");b.setFocusable(true);b.setFocusableInTouchMode(true);b.setOnTouchListener((v,e)->{if(e.getActionMasked()==MotionEvent.ACTION_DOWN)v.requestFocusFromTouch();return false;});b.setOnFocusChangeListener((v,f)->{b.setBackground(panel(f));if(f)rememberedFocusIndex=focusTargets.indexOf(v);});b.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_SETTINGS));launchStarted=true;}catch(ActivityNotFoundException|SecurityException e){notice("Settings unavailable");}});return b;}

    private Button headerButton(String title,Runnable action){Button b=dialogButton(title);b.setTextSize(15);b.setMinHeight(dp(44));b.setMinimumWidth(dp(88));b.setContentDescription(title);b.setOnClickListener(v->action.run());b.setOnFocusChangeListener((v,f)->{b.setBackground(panel(f));if(f)rememberedFocusIndex=focusTargets.indexOf(v);});return b;}

    private final class HomeCard extends LinearLayout {
        final ImageView icon;final TextView title,copy;
        HomeCard(String heading,String sub,int image,Runnable click){
            super(DashboardActivity.this);setOrientation(HORIZONTAL);setGravity(Gravity.CENTER_VERTICAL);setPadding(dp(18),dp(10),dp(18),dp(10));setBackground(panel(false));setFocusable(true);setFocusableInTouchMode(true);setClickable(true);setOnTouchListener((v,e)->{if(e.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)v.requestFocusFromTouch();return false;});
            icon=new ImageView(DashboardActivity.this);if(image<0)icon.setImageDrawable(new ActionIcon(-image));else icon.setImageResource(image);icon.setContentDescription(null);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(42),dp(42));ip.setMargins(0,0,dp(16),0);addView(icon,ip);
            LinearLayout words=new LinearLayout(DashboardActivity.this);words.setOrientation(VERTICAL);addView(words,new LinearLayout.LayoutParams(0,-2,1));title=text(heading,22,Color.WHITE,true);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);words.addView(title);copy=text(sub,15,MUTED,false);copy.setMaxLines(2);copy.setEllipsize(TextUtils.TruncateAt.END);words.addView(copy);
            setMinimumHeight(dp(DashboardPresentationPolicy.minTileHeightDp(88,getResources().getConfiguration().fontScale)));setContentDescription(heading+", "+sub);setOnClickListener(v->click.run());setOnFocusChangeListener((v,f)->{setBackground(panel(f));if(f)rememberedFocusIndex=focusTargets.indexOf(v);});focusTargets.add(this);
        }
    }
    private HomeCard homeCard(String title,String copy,int image,Runnable action){return new HomeCard(title,copy,image,action);}
    private void placeHomeCard(LinearLayout row,HomeCard card){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(4),dp(4),dp(4),dp(5));row.addView(card,p);}
    private String appLabel(String pkg){try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception e){return "Unavailable app";}}
    private void openWorkspace(String family){try{startActivity(new Intent(this,PairActivity.class).putExtra("family",family));launchStarted=true;}catch(ActivityNotFoundException|SecurityException e){notice("Workspace is unavailable");}}
    private void showInstalledApps(){
        java.util.LinkedHashMap<String,String> found=new java.util.LinkedHashMap<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(android.content.pm.ResolveInfo r:getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)){String pkg=r.activityInfo.packageName;if(!pkg.equals(getPackageName())&&r.activityInfo.exported&&r.activityInfo.enabled&&r.activityInfo.applicationInfo.enabled&&launchIntent(pkg)!=null){String title=r.loadLabel(getPackageManager()).toString().replace('\n',' ').replace('\r',' ').trim();found.put(pkg,title.length()>90?title.substring(0,90):title);}}
        ArrayList<String> packages=new ArrayList<>(found.keySet());java.util.Collections.sort(packages,(a,b)->found.get(a).compareToIgnoreCase(found.get(b)));String[] names=new String[packages.size()];for(int n=0;n<names.length;n++)names[n]=found.get(packages.get(n));
        if(names.length==0){notice("No launchable apps found");return;}
        AlertDialog d=new AlertDialog.Builder(this).setTitle("All apps").setSingleChoiceItems(names,-1,(dialog,index)->{dialog.dismiss();launchPackage(packages.get(index),names[index]);}).setNegativeButton("Back",null).create();
        tv.metaxy.dashboard.ChoiceKeyPolicy keys=new tv.metaxy.dashboard.ChoiceKeyPolicy();d.setOnKeyListener((dialog,key,e)->{int at=d.getListView().getSelectedItemPosition();int result=keys.handle(e.getDeviceId(),key,e.getAction(),e.getRepeatCount(),e.isCanceled(),at);if(result==2&&at>=0){d.dismiss();launchPackage(packages.get(at),names[at]);}if(result==3)d.dismiss();return result!=0;});d.setOnShowListener(dialog->{d.getListView().setSelection(0);d.getListView().requestFocus();});d.show();
    }

    private View appTile(final LaunchRegistry.Target target) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.HORIZONTAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(16), dp(8), dp(12), dp(8));
        tile.setBackground(panel(false));
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setClickable(true);

        ImageView image = new ImageView(this);
        image.setContentDescription(null);
        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(dp(48), dp(48));
        imageParams.setMargins(0, 0, dp(12), 0);
        tile.addView(image, imageParams);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setGravity(Gravity.CENTER_VERTICAL);
        tile.addView(copy, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView title = text(target.title, 21, Color.WHITE, true);
        title.setMaxLines(2);
        title.setEllipsize(TextUtils.TruncateAt.END);
        copy.addView(title);
        TextView description = text(target.description, 15, MUTED, false);
        description.setMaxLines(2);
        description.setEllipsize(TextUtils.TruncateAt.END);
        copy.addView(description);
        TextView availability = text("Checking installed app...", 15, ACCENT, true);
        availability.setPadding(0, dp(6), 0, 0);
        copy.addView(availability);
        tile.setOnFocusChangeListener((view, focused) -> {
            view.setBackground(panel(focused));
            if (focused) rememberedFocusIndex = focusTargets.indexOf(view);
            availability.setText(isMoreApps(target)
                    ? DashboardPresentationPolicy.menuStatus(focused)
                    : DashboardPresentationPolicy.status(tile.isEnabled(), focused));
            view.setContentDescription(isMoreApps(target)
                    ? DashboardPresentationPolicy.menuAccessibilityLabel(target.title, focused)
                    : DashboardPresentationPolicy.accessibilityLabel(target.title, tile.isEnabled(), focused));
        });

        Runnable refresh = () -> {
            Intent launch = launchIntent(target.packageName);
            boolean enabled = isMoreApps(target) || launch != null;
            tile.setEnabled(enabled);
            // Disabled status and focusability convey availability; dimming
            // the entire tile makes muted text illegible on a TV.
            tile.setBackground(panel(enabled && tile.hasFocus()));
            availability.setText(isMoreApps(target)
                    ? DashboardPresentationPolicy.menuStatus(tile.hasFocus())
                    : DashboardPresentationPolicy.status(enabled, tile.hasFocus()));
            availability.setTextColor(enabled ? ACCENT : MUTED);
            try {
                if (isMoreApps(target)) {
                    image.setImageResource(android.R.drawable.ic_menu_manage);
                } else {
                Drawable icon = getPackageManager().getApplicationIcon(target.packageName);
                image.setImageDrawable(icon);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
                image.setImageDrawable(null);
            }
            tile.setContentDescription(isMoreApps(target)
                    ? DashboardPresentationPolicy.menuAccessibilityLabel(target.title, tile.hasFocus())
                    : DashboardPresentationPolicy.accessibilityLabel(target.title, enabled, tile.hasFocus()));
        };
        refreshers.add(refresh);
        refresh.run();
        tile.setOnClickListener(v -> { if (isMoreApps(target)) showMoreApps(); else launchPackage(target.packageName, target.title); });
        focusTargets.add(tile);
        return tile;
    }

    private boolean isMoreApps(LaunchRegistry.Target target) {
        return LaunchRegistry.MORE_APPS_PACKAGE.equals(target.packageName);
    }

    private boolean hasMoreApps() {
        for (LaunchRegistry.Target target : LaunchRegistry.MORE_TARGETS)
            if (launchIntent(target.packageName) != null) return true;
        return false;
    }

    private void showMoreApps() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setBackground(panel(false));
        TextView title = text("More apps", 25, ACCENT, true);
        title.setPadding(0, 0, 0, dp(8));
        card.addView(title);
        TextView hint = text("Choose an app  ·  Back returns", 15, MUTED, false);
        hint.setPadding(0, 0, 0, dp(14));
        card.addView(hint);
        ScrollView dialogScroll = new ScrollView(this);
        dialogScroll.setFillViewport(false);
        dialogScroll.setFocusable(false);
        dialogScroll.addView(card);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogScroll).create();
        Button pair = dialogButton("Arrange two apps - tiled split / picture in picture");
        pair.setOnClickListener(v -> { dialog.dismiss(); startActivity(new Intent(this, PairActivity.class)); });
        card.addView(pair, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button first = pair;
        for (LaunchRegistry.Target target : LaunchRegistry.MORE_TARGETS) {
            boolean available = launchIntent(target.packageName) != null;
            Button choice = dialogButton(target.title + "  ·  " +
                    (available ? target.description : "Unavailable on this device"));
            choice.setContentDescription(target.title + ", " +
                    (available ? target.description : "unavailable on this device"));
            choice.setEnabled(available);
            choice.setAlpha(available ? 1.0f : 0.55f);
            choice.setOnClickListener(v -> {
                dialog.dismiss();
                launchPackage(target.packageName, target.title);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, dp(8));
            card.addView(choice, params);
            if (available && first == null) first = choice;
        }
        Button about = dialogButton("Split-screen status, About and GitHub");
        about.setContentDescription("Split-screen status, About Kinegra Home, connected controls and GitHub links");
        about.setOnClickListener(v -> { dialog.dismiss(); showAbout(); });
        card.addView(about, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button cancel = dialogButton("Cancel");
        cancel.setOnClickListener(v -> dialog.cancel());
        card.addView(cancel, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button initial = first != null ? first : about;
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                int width = Math.max(dp(220), Math.min(dp(640),
                        getResources().getDisplayMetrics().widthPixels - dp(48)));
                dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
            initial.requestFocus();
        });
        // Dialog owns its own window: Activity.dispatchKeyEvent does not
        // provide its usual controller A/B handling here.
        ModalControllerPolicy modalKeys = new ModalControllerPolicy();
        dialog.setOnKeyListener((ignored, keyCode, event) -> {
            View focused = dialog.getCurrentFocus();
            int result = modalKeys.handle(event.getDeviceId(), keyCode,
                    event.getAction(), event.getRepeatCount(), event.isCanceled(), focused);
            if (result == ModalControllerPolicy.ACTIVATE && focused instanceof Button
                    && focused.isEnabled()) focused.performClick();
            if (result == ModalControllerPolicy.CANCEL) dialog.cancel();
            return result != ModalControllerPolicy.PASS;
        });
        dialog.show();
    }

    /** Optional framework resources: absence is unknown, never assumed support. */
    private Boolean frameworkFlag(String name) {
        try {
            android.content.res.Resources resources = android.content.res.Resources.getSystem();
            int id = resources.getIdentifier(name, "bool", "android");
            return id == 0 ? null : resources.getBoolean(id);
        } catch (android.content.res.Resources.NotFoundException | SecurityException ignored) {
            return null;
        }
    }

    /** Give remote users stops through long diagnostics, including at large font scales. */
    private void focusableDiagnostic(TextView label) {
        label.setFocusable(true);
        label.setFocusableInTouchMode(true);
        label.setPadding(dp(12), dp(10), dp(12), dp(10));
        label.setBackground(panel(false));
        label.setOnFocusChangeListener((view, focused) -> view.setBackground(panel(focused)));
    }

    private void showAbout() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(24), dp(20), dp(24), dp(20));
        card.setBackground(panel(false));
        TextView title = text("About Kinegra Home", 25, ACCENT, true);
        card.addView(title);
        card.addView(text("D-pad scrolls details  ·  Back closes", 15, MUTED, false));
        String version = "Unavailable";
        try { version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; }
        catch (PackageManager.NameNotFoundException ignored) { }
        TextView details = text(DiagnosticPresentation.summary(version, Build.MANUFACTURER,
                Build.MODEL, Build.DEVICE, Build.HARDWARE, Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT), 17, Color.WHITE, false);
        details.setPadding(0, dp(10), 0, dp(10));
        details.setTextIsSelectable(false);
        focusableDiagnostic(details);
        card.addView(details);
        TextView firmware = text(DiagnosticPresentation.firmware(Build.VERSION.INCREMENTAL),
                16, MUTED, false);
        firmware.setPadding(0, 0, 0, dp(12));
        focusableDiagnostic(firmware);
        card.addView(firmware);
        TextView capabilities = text(DiagnosticPresentation.windowCapabilities(
                frameworkFlag("config_supportsMultiWindow"),
                frameworkFlag("config_supportsSplitScreenMultiWindow"),
                getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)),
                16, Color.WHITE, false);
        capabilities.setPadding(0, 0, 0, dp(12));
        focusableDiagnostic(capabilities);
        card.addView(capabilities);
        ArrayList<String> inputs = new ArrayList<>();
        ArrayList<String> softwareInputs = new ArrayList<>();
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device == null) continue;
            int sources = device.getSources();
            boolean gamepad = (sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK;
            boolean dpad = (sources & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD;
            String description = DiagnosticPresentation.input(device.getName(), device.getVendorId(),
                    device.getProductId(), device.isVirtual(), gamepad, dpad);
            List<String> group = DiagnosticPresentation.softwareInput(device.getName(), device.isVirtual())
                    ? softwareInputs : inputs;
            if (!description.isEmpty() && !group.contains(description)) group.add(description);
        }
        TextView devices = text(DiagnosticPresentation.inputs(inputs, softwareInputs), 16, MUTED, false);
        devices.setPadding(0, 0, 0, dp(12));
        focusableDiagnostic(devices);
        card.addView(devices);
        TextView support = text("Feedback, updates and support open the project's GitHub pages.",16,MUTED,false);support.setPadding(0,0,0,dp(8));card.addView(support);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setFocusable(false);scroll.addView(card);
        AlertDialog dialog=new AlertDialog.Builder(this).setView(scroll).create();
        addGithubLink(card,"Support / donate — GitHub profile","https://github.com/matthewhand");
        addGithubLink(card,"Feedback — issues","https://github.com/matthewhand/kinegra-home/issues");
        addGithubLink(card,"Releases & updates","https://github.com/matthewhand/kinegra-home/releases");
        Button licenses=dialogButton("Open-source licenses");licenses.setOnClickListener(v->showLicenses());card.addView(licenses);
        Button settings=dialogButton("Android TV settings");settings.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(Exception e){notice("Settings unavailable");}});card.addView(settings);
        Button close = dialogButton("Close");
        close.setOnClickListener(v -> dialog.dismiss());
        card.addView(close);
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                int width = Math.max(dp(220), Math.min(dp(640),
                        getResources().getDisplayMetrics().widthPixels - dp(48)));
                int height = Math.min(dp(700), getResources().getDisplayMetrics().heightPixels - dp(48));
                dialog.getWindow().setLayout(width, height);
            }
            details.requestFocus();
        });
        ModalControllerPolicy keys = new ModalControllerPolicy();
        dialog.setOnKeyListener((ignored, keyCode, event) -> {
            View focused = dialog.getCurrentFocus();
            int result = keys.handle(event.getDeviceId(), keyCode, event.getAction(),
                    event.getRepeatCount(), event.isCanceled(), focused);
            if (result == ModalControllerPolicy.ACTIVATE && focused instanceof Button
                    && focused.isEnabled()) focused.performClick();
            if (result == ModalControllerPolicy.CANCEL) dialog.cancel();
            return result != ModalControllerPolicy.PASS;
        });
        dialog.show();
    }

    private void showLicenses() {
        StringBuilder content = new StringBuilder();
        try {
            for (String name : new String[]{"PROJECT-LICENSE.txt", "ADBLIB-LICENSE.txt"}) {
                try (java.io.InputStream input = getAssets().open("licenses/" + name);
                     java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream()) {
                    byte[] buffer = new byte[2048]; int count;
                    while ((count = input.read(buffer)) != -1) {
                        if (output.size() + count > 128 * 1024) throw new java.io.IOException("Notice too large");
                        output.write(buffer, 0, count);
                    }
                    content.append(new String(output.toByteArray(), "UTF-8")).append("\n\n");
                }
            }
        } catch (java.io.IOException error) { notice("License text is unavailable in this build"); return; }
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(16), dp(20), dp(16)); card.setBackground(panel(false));
        card.addView(text("Open-source licenses", 24, ACCENT, true));
        card.addView(text("D-pad scrolls each paragraph · Back closes", 15, MUTED, false));
        TextView first = null;
        for (String paragraph : content.toString().split("\n[\t \r]*\n")) {
            if (paragraph.trim().isEmpty()) continue;
            TextView label = text(paragraph.trim(), 16, Color.WHITE, false);
            focusableDiagnostic(label); card.addView(label); if (first == null) first = label;
        }
        ScrollView scroll = new ScrollView(this); scroll.setFocusable(false); scroll.addView(card);
        AlertDialog dialog = new AlertDialog.Builder(this).setView(scroll).create();
        Button close = dialogButton("Close"); close.setOnClickListener(v -> dialog.dismiss()); card.addView(close);
        final TextView initial = first;
        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) dialog.getWindow().setLayout(
                Math.max(dp(220), Math.min(dp(680), getResources().getDisplayMetrics().widthPixels - dp(48))),
                Math.min(dp(700), getResources().getDisplayMetrics().heightPixels - dp(48)));
            if (initial != null) initial.requestFocus(); else close.requestFocus();
        });
        ModalControllerPolicy keys = new ModalControllerPolicy();
        dialog.setOnKeyListener((ignored, keyCode, event) -> {
            View focused = dialog.getCurrentFocus();
            int result = keys.handle(event.getDeviceId(), keyCode, event.getAction(), event.getRepeatCount(), event.isCanceled(), focused);
            if (result == ModalControllerPolicy.ACTIVATE && focused instanceof Button && focused.isEnabled()) focused.performClick();
            if (result == ModalControllerPolicy.CANCEL) dialog.cancel();
            return result != ModalControllerPolicy.PASS;
        });
        dialog.show();
    }

    private void addGithubLink(LinearLayout card,String label,String url){Button b=dialogButton(label);b.setOnClickListener(v->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException|SecurityException e){notice("No browser available to open GitHub");}});card.addView(b);}

    private Button dialogButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(18);
        button.setTextColor(Color.WHITE);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setPadding(dp(16), dp(6), dp(16), dp(6));
        button.setMinHeight(dp(62));
        button.setMaxLines(2);
        button.setEllipsize(TextUtils.TruncateAt.END);
        button.setFocusableInTouchMode(true);
        button.setBackground(panel(false));
        button.setOnFocusChangeListener((v, focused) -> v.setBackground(panel(focused)));
        button.setOnTouchListener((v,e)->{if(e.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)v.requestFocusFromTouch();return false;});
        return button;
    }

    private Intent launchIntent(String packageName) {
        if (!tv.metaxy.dashboard.HomeMenuPolicy.valid(packageName) || getPackageName().equals(packageName)) return null;
        PackageManager manager = getPackageManager();
        Intent intent = manager.getLeanbackLaunchIntentForPackage(packageName);
        if (intent == null) intent = manager.getLaunchIntentForPackage(packageName);
        if (intent == null || intent.getComponent() == null ||
                !packageName.equals(intent.getComponent().getPackageName())) return null;
        try { android.content.pm.ActivityInfo info=manager.getActivityInfo(intent.getComponent(),0);if(!info.exported||!info.enabled||!info.applicationInfo.enabled)return null; } catch(PackageManager.NameNotFoundException e){return null;}
        return intent;
    }

    private void launchPackage(String packageName, String label) {
        Intent intent = launchIntent(packageName);
        if (intent == null) {
            notice(label + " is not available on this Shield");
            return;
        }
        try {
            startActivity(intent);
            RecentApps.record(this,packageName);
            launchStarted = true;
        } catch (ActivityNotFoundException | SecurityException error) {
            notice("Cannot open " + label + " on this device");
        }
    }

    private void addSystemTool(LinearLayout row, String title, String sub, String action) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(dp(19), dp(8), dp(16), dp(8));
        button.setBackground(panel(false));
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setClickable(true);
        button.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(panel(focused));
            if (focused) rememberedFocusIndex = focusTargets.indexOf(v);
            v.setContentDescription(DashboardPresentationPolicy.accessibilityLabel(
                    title, v.isEnabled(), focused));
        });
        TextView toolTitle = text(title, 20, Color.WHITE, true);
        toolTitle.setMaxLines(2);
        toolTitle.setEllipsize(TextUtils.TruncateAt.END);
        button.addView(toolTitle);
        TextView toolDescription = text(sub, 15, MUTED, false);
        toolDescription.setMaxLines(2);
        toolDescription.setEllipsize(TextUtils.TruncateAt.END);
        button.addView(toolDescription);
        button.setMinimumHeight(dp(DashboardPresentationPolicy.minTileHeightDp(
                85, getResources().getConfiguration().fontScale)));
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(4), dp(4), dp(4), dp(4));
        row.addView(button, params);
        focusTargets.add(button);
        Runnable refresh = () -> {
            boolean available = new Intent(action).resolveActivity(
                    getPackageManager()) != null;
            button.setEnabled(available);
            // Keep normal-contrast text even for disabled system tools.
            button.setBackground(panel(available && button.hasFocus()));
            button.setContentDescription(DashboardPresentationPolicy.accessibilityLabel(
                    title, available, button.hasFocus()));
        };
        refreshers.add(refresh);
        refresh.run();
        button.setOnClickListener(v -> {
            try {
                startActivity(new Intent(action));
                launchStarted = true;
            } catch (ActivityNotFoundException | SecurityException error) {
                notice(title + " is not available");
            }
        });
    }

    private void notice(String messageText) {
        tipResumeAt=android.os.SystemClock.uptimeMillis()+15000;
        if (message != null) message.setText(messageText);
        Toast.makeText(this, messageText, Toast.LENGTH_SHORT).show();
    }
}
