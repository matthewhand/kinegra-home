package tv.metaxy.dashboard;

public final class test_controller_motion_policy {
    private static int checks;
    private static void eq(int want, int have) {
        checks++;
        if (want != have) throw new AssertionError(
                "assertion "+checks+" want="+want+" have="+have);
    }
    private static int step(ControllerMotionPolicy p,int id,float x,float y) {
        return p.step(id,x,y,0,0);
    }
    public static void main(String[] args) {
        ControllerMotionPolicy p = new ControllerMotionPolicy();
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.RIGHT,step(p,7,.83f,0));
        eq(0,step(p,7,.99f,0));  // no repeat for held stick
        eq(0,step(p,7,-.99f,0)); // opposite without recenter cannot repeat
        eq(0,step(p,7,.34f,.34f));
        eq(ControllerMotionPolicy.LEFT,step(p,7,-.9f,0));
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.DOWN,step(p,7,0,.8f));
        eq(0,step(p,7,0,.81f));
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.UP,step(p,7,0,-.95f));
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.RIGHT,step(p,7,.95f,.95f));
        for(int n=0;n<8;n++) eq(0,step(p,7,.95f,.95f)); // F1 regression
        eq(0,step(p,7,-.95f,.95f)); // cannot change direction still engaged
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.DOWN,step(p,7,.40f,.95f));
        eq(0,step(p,7,0,0));
        eq(ControllerMotionPolicy.LEFT,step(p,7,-.99f,-.99f));
        eq(0,step(p,7,0,0));
        // Native D-pad synthesised from hat motion is the ONLY hat navigator.
        eq(0,p.step(7,0,0,1,0));
        eq(0,p.step(7,0,0,0,-1));
        eq(0,p.step(7,.98f,0,1,0));
        eq(0,p.step(7,0,0,0,0));
        eq(ControllerMotionPolicy.RIGHT,step(p,7,.95f,0));
        eq(0,p.step(7,.95f,0,1,0)); // HAT suppresses, keeps stick latched
        eq(0,step(p,7,.95f,0)); // no duplicate while physical stick held
        eq(0,step(p,7,0,0)); // genuine neutral rearms
        eq(ControllerMotionPolicy.RIGHT,step(p,7,.95f,0));
        eq(0,step(p,7,0,0));
        // Per device: id 20 does not inherit id 10's held state.
        p.reset();
        eq(ControllerMotionPolicy.LEFT,step(p,10,-.93f,0));
        eq(ControllerMotionPolicy.RIGHT,step(p,20,.93f,0));
        eq(0,step(p,10,-.93f,0));
        eq(0,step(p,20,.93f,0));
        eq(2,p.trackedDeviceCount());
        p.resetDevice(10);
        eq(1,p.trackedDeviceCount());
        eq(ControllerMotionPolicy.DOWN,step(p,10,0,.83f));
        eq(0,step(p,20,.9f,0));
        p.resetDevice(20);
        eq(ControllerMotionPolicy.RIGHT,step(p,20,.9f,0));
        p.reset();
        eq(0,p.trackedDeviceCount());
        // Only full-neutral clears latch; a partially held Y still blocks.
        eq(ControllerMotionPolicy.RIGHT,step(p,1,.9f,0));
        eq(0,step(p,1,0,.4f));
        eq(0,step(p,1,0,.9f));
        eq(0,step(p,1,.2f,.2f));
        eq(ControllerMotionPolicy.UP,step(p,1,0,-.92f));
        // Invalid and NaN inputs do not move any focus.
        p.reset();
        eq(0,p.step(-1,.9f,0,0,0));
        eq(0,p.step(1,Float.NaN,0,0,0));
        eq(0,p.step(1,Float.POSITIVE_INFINITY,0,0,0));
        eq(0,p.step(1,0,0,Float.NaN,0));
        eq(0,step(p,1,.67f,0));
        eq(ControllerMotionPolicy.RIGHT,step(p,1,.69f,0));
        // Bound one held entry per physical device (8 maximum).
        p.reset();
        for(int i=0;i<8;i++) eq(ControllerMotionPolicy.LEFT,step(p,100+i,-.9f,0));
        eq(8,p.trackedDeviceCount());
        eq(0,step(p,109,.9f,0)); // 9th rejected; no eviction
        eq(0,step(p,100,-.9f,0));
        p.resetDevice(107);
        eq(ControllerMotionPolicy.RIGHT,step(p,109,.9f,0));
        eq(8,p.trackedDeviceCount());
        System.out.println("CONTROLLER_MOTION_POLICY_PASS assertions="+checks);
    }
}
