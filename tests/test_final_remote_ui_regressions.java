package tv.metaxy.dashboard;

/** Negative tests of the unmocked production key-state coordinator. */
public final class test_final_remote_ui_regressions {
    private static int checks;
    private static void eq(long a,long b){
        checks++;if(a!=b)throw new AssertionError("check="+checks+" expected="+a+" got="+b);
    }
    static final class Ui implements RemoteUiSession.ViewPort {
        boolean success=true, deny=false;
        int focus=0,clicks,attempts,exits;
        final boolean[] enabled={true,true,true,true,true,true,true,true};
        public int focusedIndex(){return focus;}
        public boolean isEnabled(int i){return i>=0&&i<8&&enabled[i];}
        public boolean requestFocus(int i){
            if(deny||!isEnabled(i))return false;focus=i;return true;
        }
        public boolean activate(int i){
            if(focus!=i||!isEnabled(i))throw new AssertionError("disabled click");
            attempts++;if(success)clicks++;return success;
        }
        public void exitDashboard(){exits++;}
    }
    static int call(RemoteUiSession s,int d,int k,int action,int count,
                    boolean canceled,long start,long now){
        return s.dispatch(d,k,action,count,canceled,start,now);
    }
    static void down(RemoteUiSession s,int d,int k,long t){
        eq(1,call(s,d,k,0,0,false,t,t));
    }
    static void up(RemoteUiSession s,int d,int k,long t,long now){
        eq(1,call(s,d,k,1,0,false,t,now));
    }
    static void press(RemoteUiSession s,int d,int k,long t){
        down(s,d,k,t);up(s,d,k,t,t+20);
    }
    static void failedLaunchAndBoundedRetry(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        u.success=false;
        press(s,11,23,1000);press(s,11,23,1200);
        eq(2,u.attempts);eq(0,u.clicks); // failed intent can be retried
        u.success=true;press(s,11,23,2000);
        eq(3,u.attempts);eq(1,u.clicks);
        press(s,11,66,2100);eq(3,u.attempts); // duplicate alias can't relaunch
        press(s,11,23,6000);eq(4,u.attempts);eq(2,u.clicks);
        // Simulated successful leave/return: new gesture unblocked.
        s.resetHeldKeys();press(s,11,23,6100);eq(3,u.clicks);
    }
    static void overlappingAliasCancellation(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        down(s,11,23,1000);down(s,11,66,1001);
        up(s,11,23,1000,1030);
        eq(0,u.clicks); // don't click before all aliases released
        eq(1,call(s,11,66,1,0,true,1001,1040));
        eq(0,u.clicks); // cancellation of final alias undoes entire group
        down(s,11,23,2000);down(s,11,66,2001);
        eq(1,call(s,11,23,1,0,true,2000,2010));
        up(s,11,66,2001,2020);eq(0,u.clicks);
        down(s,11,23,3000);down(s,11,66,3001);
        up(s,11,66,3001,3010);eq(0,u.clicks);
        up(s,11,23,3000,3020);eq(1,u.clicks); // valid group, once
        // Another key after success but before Activity pause is suppressed.
        press(s,11,96,3050);eq(1,u.clicks);
        s.resetHeldKeys();
        press(s,11,96,4000);eq(2,u.clicks);
    }
    static void longHeldAliasesWithoutRepeat(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        down(s,5,23,1000);down(s,5,66,1001);
        up(s,5,23,1000,11000);
        eq(0,u.clicks); // first valid release waits for held alias
        up(s,5,66,1001,11001);eq(1,u.clicks);
        s.resetHeldKeys();
        down(s,5,23,13000);down(s,5,66,13001);
        up(s,5,23,13000,23000);
        eq(1,call(s,5,66,1,0,true,13001,23001));
        eq(1,u.clicks); // cancellation still wins
    }
    static void longHeldRepeatAndRelease(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        down(s,5,23,1000);
        eq(1,call(s,5,23,0,1,false,1000,2000));
        eq(1,call(s,5,23,0,2,false,1000,3000));
        up(s,5,23,1000,10001);eq(1,u.clicks); // 10 sec valid release
        s.resetHeldKeys();
        down(s,5,4,12000);
        eq(1,call(s,5,4,0,4,false,12000,17000));
        up(s,5,4,12000,22000);eq(1,u.exits);
        s.resetHeldKeys();
        down(s,5,23,24000);
        eq(1,call(s,5,23,1,0,true,24000,35000));eq(1,u.clicks);
        down(s,5,4,38000);
        eq(1,call(s,5,4,1,0,true,38000,42000));eq(1,u.exits);
    }
    static void staleDirectionalKeyDoesNotCancelFreshOk(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        down(s,9,22,1000);eq(1,u.focus);
        down(s,9,23,3000);
        up(s,9,23,3000,3550);
        eq(1,u.clicks); // stale Right must not cancel fresh OK
        s.resetHeldKeys();u.focus=0;
        down(s,9,23,5000);
        down(s,9,66,5200);
        // Stale OK aborts its ONLY alias gesture, not a new independent
        // gesture from another controller.
        press(s,12,23,8000);
        eq(2,u.clicks);
        up(s,9,66,5200,8200);eq(2,u.clicks);
        s.resetHeldKeys();
        down(s,9,22,10000);
        down(s,12,22,10001);
        eq(2,u.focus); // separate devices navigate independently
        s.resetDevice(9);
        down(s,9,21,11000);eq(1,u.focus);
    }
    static void focusRestoration(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        eq(5,s.restore(5));eq(5,u.focus); // remembered wins default view
        u.focus=0;u.deny=true;
        eq(4,s.restore(4));eq(0,u.focus); // keep selection on denial
        u.deny=false;eq(4,s.restore(4));eq(4,u.focus);
        u.enabled[4]=false;u.focus=-1;
        eq(0,s.restore(4));eq(0,u.focus); // removed remembered -> fallback
        u.focus=6;eq(6,s.restore(-1));eq(6,u.focus);
        for(int i=0;i<8;i++)u.enabled[i]=false;
        u.focus=-1;eq(-1,s.restore(6));eq(-1,u.focus);
    }
    static void exitAndBounds(){
        Ui u=new Ui();RemoteUiSession s=new RemoteUiSession(u);
        down(s,1,4,1000);down(s,1,97,1001);
        up(s,1,4,1000,1030);
        up(s,1,97,1001,1031);eq(1,u.exits);
        press(s,1,4,1300);eq(1,u.exits);
        eq(0,call(s,1,3,0,0,false,5000,5000)); // Home platform-owned
        s.resetHeldKeys();
        for(int id=20;id<75;id++)down(s,id,22,10000+id);
        // Full state cannot grow indefinitely; old devices age off.
        down(s,90,21,16000);eq(1,u.focus); // full row ended at 2, left moves to 1
        up(s,90,21,16000,16001);
        s.resetHeldKeys();
        down(s,1,4,20000);
        eq(1,call(s,1,4,1,0,true,20000,22000));eq(1,u.exits);
        press(s,1,4,23000);eq(2,u.exits);
    }
    public static void main(String[]args){
        failedLaunchAndBoundedRetry();
        overlappingAliasCancellation();
        longHeldRepeatAndRelease();
        longHeldAliasesWithoutRepeat();
        staleDirectionalKeyDoesNotCancelFreshOk();
        focusRestoration();
        exitAndBounds();
        System.out.println("FINAL_REMOTE_UI_REGRESSIONS_PASS assertions="+checks);
    }
}
