package tv.metaxy.proof;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tv.metaxy.dashboard.PairProtocol;

abstract class PairUi extends Activity {
    static final int BG=0xff071019,FG=0xffedf7fa,ACCENT=0xff6be3b8,MUTED=0xffb0c5d0;
    ProjectionState state;TextView connection,message;boolean busy;
    private final Handler handler=new Handler();
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Runnable poll=new Runnable(){public void run(){if(connection!=null)connection.setText(WindowBrokerClient.connected()?"Window helper connected":"Window helper disconnected - open Setup");handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);state=new ProjectionState(this);}
    @Override public void onResume(){super.onResume();handler.post(poll);}
    @Override public void onPause(){handler.removeCallbacks(poll);super.onPause();}
    @Override public void onDestroy(){worker.shutdown();super.onDestroy();}
    boolean sessionShortcutsEnabled(){
        try{return tv.metaxy.dashboard.SessionShortcutPolicy.enabled(getPackageName(),android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),android.provider.Settings.Secure.getInt(getContentResolver(),android.provider.Settings.Secure.ACCESSIBILITY_ENABLED,0)==1);}
        catch(RuntimeException unavailable){return false;}
    }
    void openSessionShortcutSettings(){
        try{startActivity(new android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));}
        catch(RuntimeException unavailable){Toast.makeText(this,"Open Android Settings > Accessibility > Kinegra Home session shortcuts",Toast.LENGTH_LONG).show();}
    }
    int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
    TextView text(String value,int sp,int color){TextView v=new TextView(this);v.setText(value);v.setTextSize(sp);v.setTextColor(color);v.setPadding(0,dp(3),0,dp(3));return v;}
    Button button(String label,Runnable click){
        Button b=new Button(this);b.setText(label);b.setFocusable(true);b.setFocusableInTouchMode(true);b.setTextColor(FG);b.setTextSize(17);b.setAllCaps(false);b.setMinHeight(dp(50));b.setPadding(dp(14),dp(6),dp(14),dp(6));
        b.setBackground(background(false));b.setOnFocusChangeListener((v,f)->v.setBackground(background(f)));b.setOnTouchListener((v,e)->{if(e.getActionMasked()==android.view.MotionEvent.ACTION_DOWN)v.requestFocusFromTouch();return false;});b.setOnClickListener(v->{if(!busy)click.run();});return b;
    }
    GradientDrawable background(boolean focused){GradientDrawable d=new GradientDrawable();d.setColor(focused?0xff235557:0xff132733);d.setCornerRadius(dp(9));d.setStroke(dp(focused?3:1),focused?ACCENT:0xff42616e);return d;}
    String label(String pkg){try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}catch(Exception e){return "Unavailable app";}}
    void action(int operation){
        if(busy)return;busy=true;
        boolean ending=operation==PairProtocol.END||operation==PairProtocol.EDIT||operation==PairProtocol.FULL_LEFT||operation==PairProtocol.FULL_RIGHT||operation==PairProtocol.STOP;
        final boolean wasActive=state.active;
        if(ending)state.active=false;
        // The helper may create the dock before replying; it must read the
        // active pair from preferences rather than overwrite a stale flag.
        if(operation==PairProtocol.APPLY)state.active=true;
        state.save();
        if(message!=null)message.setText("Working...");
        final String left=state.left,right=state.right;final int mode=state.mode,size=state.size;
        worker.execute(()->{
            WindowBrokerClient.Result result=WindowBrokerClient.call(operation,left,right,mode,size);
            handler.post(()->{
                busy=false;
                if(result.ok&&operation==PairProtocol.APPLY)state.active=true;
                if(!result.ok&&operation==PairProtocol.APPLY)state.active=false;
                if(!result.ok&&ending)state.active=wasActive;
                state.save();
                if(isFinishing()||isDestroyed())return;
                if(message!=null)message.setText(result.message);
                if(!result.ok)Toast.makeText(this,result.message,Toast.LENGTH_LONG).show();
                afterAction(operation,result.ok);
            });
        });
    }
    void afterAction(int operation,boolean ok){ }
    @Override public boolean dispatchKeyEvent(KeyEvent e){
        int key=e.getKeyCode();
        if(key==KeyEvent.KEYCODE_BUTTON_A||key==KeyEvent.KEYCODE_BUTTON_B){
            KeyEvent local=new KeyEvent(e.getDownTime(),e.getEventTime(),e.getAction(),key==KeyEvent.KEYCODE_BUTTON_A?KeyEvent.KEYCODE_DPAD_CENTER:KeyEvent.KEYCODE_BACK,e.getRepeatCount(),e.getMetaState(),e.getDeviceId(),e.getScanCode(),e.getFlags(),e.getSource());
            return super.dispatchKeyEvent(local);
        }
        return super.dispatchKeyEvent(e);
    }
}
