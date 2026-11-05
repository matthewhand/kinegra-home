package tv.metaxy.dashboard;

/** Independent negative-input regression suite against production session. */
public final class test_adversarial_remote_ui {
    private static int checks=0;
    private static void eq(long expected,long actual) {
        checks++; if (expected!=actual) throw new AssertionError(
                "assertion="+checks+" expected="+expected+" actual="+actual);
    }
    private static final class Port implements RemoteUiSession.ViewPort {
        int focused=0,clicks,exits;
        boolean refuse;
        boolean[] enabled={true,true,true,true,true,true,true,true};
        @Override public int focusedIndex(){return focused;}
        @Override public boolean isEnabled(int i){return i>=0&&i<8&&enabled[i];}
        @Override public boolean requestFocus(int i){
            if(refuse||!isEnabled(i))return false; focused=i;return true;
        }
        @Override public boolean activate(int i){
            if (!isEnabled(i)||focused!=i)throw new AssertionError("invalid click");
            clicks++;
            return true;
        }
        @Override public void exitDashboard(){exits++;}
    }
    private static int call(RemoteUiSession s,int device,int code,int action,
                            int repeat,boolean canceled,long down,long event){
        return s.dispatch(device,code,action,repeat,canceled,down,event);
    }
    private static void down(RemoteUiSession s,int d,int key,long t){
        eq(RemoteUiSession.CONSUMED,call(s,d,key,0,0,false,t,t));
    }
    private static void up(RemoteUiSession s,int d,int key,long t,long when){
        eq(RemoteUiSession.CONSUMED,call(s,d,key,1,0,false,t,when));
    }
    private static void hotplugAndMissingReleases(){
        Port p=new Port();RemoteUiSession s=new RemoteUiSession(p);
        int right=RemoteUiSession.KEY_DPAD_RIGHT;
        down(s,7,right,1000);eq(1,p.focused);
        down(s,7,right,1000);eq(1,p.focused); // exact duplicate down
        call(s,7,right,0,12,false,1000,1200);eq(1,p.focused); // held-repeat
        // A different physical Shield remote is not blocked by ASUS ID7.
        down(s,21,right,1230);eq(2,p.focused);
        up(s,7,right,1000,1260);eq(2,p.focused);
        // Device 7 key-up cannot rearm still-held remote 21.
        down(s,21,right,1230);eq(2,p.focused);
        // A real NEW downTime rearms a key missing its KeyUp.
        down(s,21,right,1700);eq(2,p.focused); // grid row edge
        int left=RemoteUiSession.KEY_DPAD_LEFT;
        down(s,7,left,1800);eq(1,p.focused);
        s.resetDevice(7);
        down(s,7,left,1950);eq(0,p.focused); // re-plug
        up(s,7,left,1800,1960);eq(0,p.focused); // stale old up ignored
        // The same key on one device rearms after an inactive 2.5s
        // without auto-repeat frames. Never rearm held repeatCount > 0.
        down(s,30,right,2000);eq(1,p.focused);
        call(s,30,right,0,4,false,2000,5000);eq(1,p.focused);
        call(s,30,right,0,0,false,2000,5100);
        eq(1,p.focused); // repeat at 5000 proves key is still held
        call(s,30,right,0,0,false,2000,7600);
        eq(2,p.focused); // silence >2500ms permits bounded rearm
        call(s,30,right,0,2,false,2000,8000);eq(2,p.focused);
        s.resetHeldKeys();
        down(s,21,left,9000);eq(1,p.focused);
        up(s,21,left,9000,9001);
    }
    private static void confirmationAliasesAndCancellation(){
        Port p=new Port();RemoteUiSession s=new RemoteUiSession(p);
        int ok=RemoteUiSession.KEY_DPAD_CENTER,enter=RemoteUiSession.KEY_ENTER,
                a=RemoteUiSession.KEY_BUTTON_A;
        down(s,4,ok,1000);eq(0,p.clicks);
        down(s,4,enter,1001);eq(0,p.clicks);
        up(s,4,ok,1000,1020);eq(0,p.clicks); // wait for both aliases
        up(s,4,enter,1001,1021);eq(1,p.clicks); // aliases, one launch
        down(s,4,a,2000);up(s,4,a,2000,2010);eq(1,p.clicks);
        s.resetHeldKeys(); // simulated app leave/return
        down(s,4,a,3000);eq(1,p.clicks);
        eq(1,call(s,4,a,1,0,true,3000,3001));eq(1,p.clicks);
        down(s,4,ok,4000);down(s,4,enter,4001);
        eq(1,call(s,4,ok,1,0,true,4000,4010));
        up(s,4,enter,4001,4011);eq(1,p.clicks); // cancel whole gesture
        down(s,4,enter,5000); // distinct valid new gesture
        up(s,4,enter,5000,5001);eq(2,p.clicks);
        s.resetHeldKeys();
        down(s,8,ok,6000);
        eq(0,call(s,8,RemoteUiSession.KEY_HOME,0,0,false,6000,6000));
        up(s,8,ok,6000,6010);eq(3,p.clicks);
    }
    private static void focusRestoreAndNoAccidentalActivation(){
        Port p=new Port();RemoteUiSession s=new RemoteUiSession(p);
        p.focused=-1;p.refuse=true;
        eq(5,s.restore(5));eq(-1,p.focused); // F4 retains saved choice
        p.refuse=false;
        eq(5,s.restore(5));eq(5,p.focused); // window focus regained
        p.enabled[5]=false;p.focused=-1;
        eq(0,s.restore(5));eq(0,p.focused); // removed app fallback
        p.enabled[0]=false;p.enabled[1]=false;
        p.focused=-1;
        eq(2,s.restore(-1));eq(2,p.focused);
        p.enabled[2]=false;p.enabled[3]=false;p.enabled[4]=false;
        p.enabled[6]=false;p.enabled[7]=false;
        p.focused=-1;
        eq(-1,s.restore(2));eq(-1,p.focused);
        down(s,11,RemoteUiSession.KEY_DPAD_CENTER,5000);
        up(s,11,RemoteUiSession.KEY_DPAD_CENTER,5000,5001);
        eq(0,p.clicks);
    }
    private static void exitsAndInvalidRelease(){
        Port p=new Port();RemoteUiSession s=new RemoteUiSession(p);
        int back=RemoteUiSession.KEY_BACK,b=RemoteUiSession.KEY_BUTTON_B;
        down(s,1,back,1000);eq(0,p.exits);
        down(s,1,b,1001);eq(0,p.exits);
        eq(1,call(s,1,back,1,0,true,1000,1010));eq(0,p.exits);
        up(s,1,b,1001,1011);eq(1,p.exits);
        down(s,1,back,2000);up(s,1,back,2000,2010);eq(1,p.exits);
        s.resetHeldKeys();
        down(s,1,back,3000);
        up(s,1,back,3000,3010);eq(2,p.exits);
        down(s,1,back,4000); // stray old up cannot finish new key
        up(s,1,back,3000,4010);eq(2,p.exits);
        up(s,1,back,4000,4020);eq(2,p.exits);
        eq(0,call(s,1,RemoteUiSession.KEY_HOME,0,0,false,9000,9000));
    }
    private static void boundedStaleDeviceInventory() {
        Port p=new Port();RemoteUiSession s=new RemoteUiSession(p);
        int up=RemoteUiSession.KEY_DPAD_UP;
        for (int id=100;id<140;id++) {
            call(s,id,up,0,0,false,1000,1000); // 40 stuck controllers
        }
        // No unbounded allocation or key eviction triggers a repeated
        // focus move for a still-held controller. New remote can operate
        // after the failed devices age out.
        call(s,900,RemoteUiSession.KEY_DPAD_RIGHT,0,0,false,5000,5000);
        eq(1,p.focused);
        up(s,900,RemoteUiSession.KEY_DPAD_RIGHT,5000,5001);
        call(s,900,RemoteUiSession.KEY_DPAD_LEFT,0,0,false,5100,5100);
        eq(0,p.focused);
    }
    public static void main(String[] args){
        hotplugAndMissingReleases();
        confirmationAliasesAndCancellation();
        focusRestoreAndNoAccidentalActivation();
        exitsAndInvalidRelease();
        boundedStaleDeviceInventory();
        System.out.println("ADVERSARIAL_REMOTE_UI_PASS assertions="+checks);
    }
}
