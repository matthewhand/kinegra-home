package tv.metaxy.dashboard;

/** Reproduce off-target confirmation and HAT/left-stick input rearming. */
public final class test_input_hold_focus_guard {
    private static int assertions;
    private static void eq(int expected, int observed) {
        assertions++;
        if (expected != observed) throw new AssertionError(
                "check=" + assertions + " expected=" + expected + " observed=" + observed);
    }
    static final class Ui implements RemoteUiSession.ViewPort {
        int focus=0, launches=0, launchedIndex=-1, exits=0;
        final boolean[] enabled={true,true,true,true,true,true,true,true};
        public int focusedIndex(){return focus;}
        public boolean isEnabled(int i){return i>=0 && i<8 && enabled[i];}
        public boolean requestFocus(int i){if(!isEnabled(i))return false;focus=i;return true;}
        public boolean activate(int i){launches++;launchedIndex=i;return true;}
        public void exitDashboard(){exits++;}
    }
    private static void send(RemoteUiSession s,int id,int key,int action,long press,long now){
        eq(RemoteUiSession.CONSUMED,s.dispatch(id,key,action,0,false,press,now));
    }
    private static void confirmationIsNotRetargetedByAnotherDevice(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        send(s,10,RemoteUiSession.KEY_DPAD_CENTER,0,1000,1000);
        send(s,20,RemoteUiSession.KEY_DPAD_RIGHT,0,1010,1010);
        eq(1,ui.focus);
        send(s,20,RemoteUiSession.KEY_DPAD_RIGHT,1,1010,1020);
        send(s,10,RemoteUiSession.KEY_DPAD_CENTER,1,1000,1030);
        eq(0,ui.launches); // should not open unexpectedly focused app 1
        send(s,10,RemoteUiSession.KEY_DPAD_CENTER,0,1200,1200);
        send(s,10,RemoteUiSession.KEY_DPAD_CENTER,1,1200,1210);
        eq(1,ui.launches); eq(1,ui.launchedIndex);
    }
    private static void joystickFocusAlsoCancelsHeldConfirmation(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        send(s,2,RemoteUiSession.KEY_DPAD_CENTER,0,2000,2000);
        s.navigateJoystick(RemoteFocusPolicy.DOWN);
        eq(3,ui.focus);
        send(s,2,RemoteUiSession.KEY_DPAD_CENTER,1,2000,2010);
        eq(0,ui.launches);
    }
    private static void movedAwayAndBackIsStillCanceled(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        send(s,9,RemoteUiSession.KEY_DPAD_CENTER,0,3000,3000);
        send(s,14,RemoteUiSession.KEY_DPAD_RIGHT,0,3010,3010);
        send(s,14,RemoteUiSession.KEY_DPAD_RIGHT,1,3010,3020);
        send(s,14,RemoteUiSession.KEY_DPAD_LEFT,0,3030,3030);
        send(s,14,RemoteUiSession.KEY_DPAD_LEFT,1,3030,3040);
        eq(0,ui.focus);
        send(s,9,RemoteUiSession.KEY_DPAD_CENTER,1,3000,3050);
        eq(0,ui.launches); // returning to original focus cannot rearm held OK
    }
    private static void externalFocusAndUnavailableTileCannotActivate(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        send(s,3,RemoteUiSession.KEY_ENTER,0,4000,4000);
        ui.focus=2; // independent Android framework focus drift
        send(s,3,RemoteUiSession.KEY_ENTER,1,4000,4020);
        eq(0,ui.launches);
        ui.focus=0;
        send(s,3,RemoteUiSession.KEY_ENTER,0,4100,4100);
        ui.enabled[0]=false; // app becomes unavailable during held gesture
        send(s,3,RemoteUiSession.KEY_ENTER,1,4100,4120);
        eq(0,ui.launches);
        ui.enabled[0]=true;
        send(s,3,RemoteUiSession.KEY_ENTER,0,4200,4200);
        send(s,3,RemoteUiSession.KEY_ENTER,1,4200,4220);
        eq(1,ui.launches);
    }
    private static void unpluggedDeviceCannotCompleteAnOldConfirmation(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        send(s,5,RemoteUiSession.KEY_BUTTON_A,0,6000,6000);
        s.resetDevice(5); // disconnect cancels pending A press
        send(s,5,RemoteUiSession.KEY_BUTTON_A,1,6000,6010);
        eq(0,ui.launches);
        send(s,5,RemoteUiSession.KEY_BUTTON_A,0,6200,6200);
        send(s,5,RemoteUiSession.KEY_BUTTON_A,1,6200,6220);
        eq(1,ui.launches); // fresh post-reconnect gesture works
        eq(0,ui.launchedIndex);
    }
    private static void heldBackAndHomeRemainSystemSafe(){
        Ui ui=new Ui(); RemoteUiSession s=new RemoteUiSession(ui);
        eq(RemoteUiSession.PASS_THROUGH,s.dispatch(1,RemoteUiSession.KEY_HOME,
                0,0,false,5000,5000));
        eq(0,ui.exits);eq(0,ui.launches);
        send(s,1,RemoteUiSession.KEY_BACK,0,5100,5100);
        send(s,1,RemoteUiSession.KEY_BACK,1,5100,15100);
        eq(1,ui.exits); // long hold ends in one exit
        send(s,1,RemoteUiSession.KEY_BACK,0,15500,15500);
        send(s,1,RemoteUiSession.KEY_BACK,1,15500,15510);
        eq(1,ui.exits); // awaiting navigation lifecycle
    }
    private static void hatAndInvalidFramesNeverRearmHeldStick(){
        ControllerMotionPolicy p=new ControllerMotionPolicy();
        eq(ControllerMotionPolicy.RIGHT,p.step(42,.91f,0,0,0));
        eq(0,p.step(42,.91f,0,1,0)); // HAT does not reset held stick
        eq(0,p.step(42,.91f,0,0,0)); // no second move
        eq(0,p.step(42,Float.NaN,0,0,0)); // invalid frame cannot rearm
        eq(0,p.step(42,.91f,0,0,0));
        eq(0,p.step(42,0,0,0,0)); // neutral releases
        eq(ControllerMotionPolicy.RIGHT,p.step(42,.91f,0,0,0));
    }
    private static void hatSuppressionConsumesSimultaneousStick(){
        ControllerMotionPolicy p=new ControllerMotionPolicy();
        eq(0,p.step(7,.95f,0,1,0)); // native hat key owns movement
        eq(0,p.step(7,.95f,0,0,0)); // not deferred duplicate
        eq(0,p.step(7,0,0,0,0)); // neutral before next deliberate stick
        eq(ControllerMotionPolicy.LEFT,p.step(7,-.95f,0,0,0));
    }
    public static void main(String[] args) {
        if (args.length == 1 && "hat-repro".equals(args[0])) {
            hatAndInvalidFramesNeverRearmHeldStick();
            hatSuppressionConsumesSimultaneousStick();
            return;
        }
        confirmationIsNotRetargetedByAnotherDevice();
        joystickFocusAlsoCancelsHeldConfirmation();
        movedAwayAndBackIsStillCanceled();
        externalFocusAndUnavailableTileCannotActivate();
        heldBackAndHomeRemainSystemSafe();
        unpluggedDeviceCannotCompleteAnOldConfirmation();
        hatAndInvalidFramesNeverRearmHeldStick();
        hatSuppressionConsumesSimultaneousStick();
        System.out.println("INPUT_HOLD_FOCUS_GUARD_PASS assertions="+assertions);
    }
}
