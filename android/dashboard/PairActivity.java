package tv.metaxy.proof;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import tv.metaxy.dashboard.ChoiceKeyPolicy;
import tv.metaxy.dashboard.PaneRecoveryPolicy;import tv.metaxy.dashboard.ProjectionGeometry;
import tv.metaxy.dashboard.PairProtocol;

/** Full-screen, remote-controlled pair selector. No automatic launch on entry. */
public final class PairActivity extends PairUi {
    private final List<App> apps=new ArrayList<>();
    private Button left,right,layout,launchButton;private Preview preview;private LinearLayout splitControls,pipControls,recoveryActions;private TextView splitCaption,launchStatus,shortcutStatus;private Button shortcutSetup;private SeekBar splitSlider;private int launchGeneration;
    private static final class App {final String pkg,title;App(String p,String t){pkg=p;title=t;}}
    private void loadApps(){
        LinkedHashMap<String,App> found=new LinkedHashMap<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){
            for(ResolveInfo r:getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category),0)){
                String pkg=r.activityInfo.packageName;
                if(!pkg.equals(getPackageName())&&r.activityInfo.exported&&r.activityInfo.enabled&&r.activityInfo.applicationInfo.enabled){
                    String label=r.loadLabel(getPackageManager()).toString().replace('\n',' ').replace('\r',' ').trim();
                    if(label.length()>90)label=label.substring(0,90);
                    found.put(pkg,new App(pkg,label));
                }
            }
        }
        apps.addAll(found.values());Collections.sort(apps,(a,b)->a.title.compareToIgnoreCase(b.title));
        if(!has(state.left)&&!apps.isEmpty())state.left=apps.get(0).pkg;
        if(!has(state.right)||state.right.equals(state.left))for(App a:apps)if(!a.pkg.equals(state.left)){state.right=a.pkg;break;}
        state.save();
    }
    private boolean has(String pkg){for(App a:apps)if(a.pkg.equals(pkg))return true;return false;}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);state.selectFamily(getIntent().getStringExtra("family"));state.save();loadApps();
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(30),dp(20),dp(30),dp(24));scroll.addView(body);
        body.addView(text("KINEGRA HOME  /  TWO APPS",28,ACCENT));
        body.addView(text("Choose your apps, tiled split or PiP corner. Apps render inside an opaque fullscreen canvas.",17,MUTED));
        Button launch=button("Launch two-app layout",this::launchLayout);
        launchButton=launch;
        launch.setTextColor(BG);launch.setTextSize(19);launch.setTypeface(null,Typeface.BOLD);launch.setMinHeight(dp(58));
        launch.setBackground(launchBackground(false));launch.setOnFocusChangeListener((v,focused)->v.setBackground(launchBackground(focused)));
        launch.setContentDescription("Launch the selected two-app layout");add(body,launch);
        connection=text("Checking window helper...",15,ACCENT);body.addView(connection);
        launchStatus=text("",16,ACCENT);body.addView(launchStatus);launchStatus.setVisibility(View.GONE);
        recoveryActions=new LinearLayout(this);recoveryActions.setOrientation(0);body.addView(recoveryActions);
        recoveryActions.addView(button("Retry",this::launchLayout));
        recoveryActions.addView(button("Choose apps",()->{recoveryActions.setVisibility(View.GONE);left.requestFocus();}));
        recoveryActions.addView(button("Setup",this::setup));recoveryActions.setVisibility(View.GONE);
        preview=new Preview();body.addView(preview,new LinearLayout.LayoutParams(-1,dp(180)));
        left=button("",()->chooseApp(selectionIsLeft(true)));right=button("",()->chooseApp(selectionIsLeft(false)));layout=button("",this::chooseLayout);
        add(body,left);add(body,right);add(body,layout);
        splitControls=new LinearLayout(this);splitControls.setOrientation(1);body.addView(splitControls);splitCaption=text("",17,FG);splitControls.addView(splitCaption);splitSlider=new SeekBar(this);splitSlider.setFocusable(true);splitSlider.setFocusableInTouchMode(true);splitSlider.setMax(60);splitSlider.setKeyProgressIncrement(1);splitSlider.setProgress(state.split-20);splitSlider.setContentDescription("Left pane percentage, use left or right to adjust");splitControls.addView(splitSlider,new LinearLayout.LayoutParams(-1,dp(48)));splitSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int value,boolean user){state.split=value+20;state.save();refresh();}});
        pipControls=new LinearLayout(this);pipControls.setOrientation(1);body.addView(pipControls);pipControls.addView(text("PiP size (20% to 40%) - use Left / Right",17,FG));
        SeekBar size=new SeekBar(this);size.setMax(20);size.setKeyProgressIncrement(5);size.setProgress(state.size-20);size.setContentDescription("Window size, left or right changes size");pipControls.addView(size,new LinearLayout.LayoutParams(-1,dp(48)));
        size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}public void onProgressChanged(SeekBar s,int value,boolean user){state.size=value+20;state.save();refresh();}});
        add(body,button("Swap app choices",()->{String a=state.left;state.left=state.right;state.right=a;state.save();refresh();}));
        shortcutStatus=text("",15,MUTED);body.addView(shortcutStatus);
        shortcutSetup=button("Enable session shortcuts",this::openSessionShortcutSettings);add(body,shortcutSetup);
        add(body,button("Setup / controls",this::setup));
        add(body,button("Browser zoom / Music Assistant help",this::browserHelp));
        add(body,button("Back to launcher",this::finish));
        message=text("D-pad selects - OK / A chooses - Back / B returns. Audio follows each app and system settings.",15,MUTED);body.addView(message);
        setContentView(scroll);refresh();launch.requestFocus();if(getIntent().getBooleanExtra("needs_setup",false))scroll.post(this::showSetupRequired);
    }
    @Override public void onResume(){super.onResume();if(shortcutStatus!=null){boolean enabled=sessionShortcutsEnabled();shortcutStatus.setText(tv.metaxy.dashboard.SessionShortcutPolicy.caption(enabled));shortcutSetup.setVisibility(enabled?View.GONE:View.VISIBLE);}}
    private GradientDrawable launchBackground(boolean focused){
        GradientDrawable d=new GradientDrawable();d.setColor(focused?0xffffd99b:0xffffbd5c);d.setCornerRadius(dp(9));
        d.setStroke(dp(focused?4:1),focused?0xfffff4df:0xffc78a39);return d;
    }
    private void launchLayout(){
        if(!has(state.left)||!has(state.right)||state.left.equals(state.right)){
            new AlertDialog.Builder(this).setTitle("Choose two apps").setMessage("Choose two different available apps before launching a layout.").setPositiveButton("OK",null).show();return;
        }
        if(busy)return;busy=true;final int g=++launchGeneration;launchButton.setEnabled(false);showLaunchStatus("Checking window support…",false);
        LocalAdbBootstrap.checkRecovery(new LocalAdbBootstrap.Listener(){public void status(String text){}public void done(boolean ok,String text){if(g!=launchGeneration||isDestroyed()||isFinishing())return;busy=false;launchButton.setEnabled(true);if(!ok){showLaunchStatus(text,true);logLaunch("recovery_unavailable",g,0);return;}launchVerified();}});
    }
    private void launchVerified(){
        if(!WindowBrokerClient.connected()){state.active=false;state.save();if(LocalAdbBootstrap.enabled(this))connectLocally();else showSetupRequired();return;}
        if(busy)return;busy=true;launchButton.setEnabled(false);showLaunchStatus("Starting apps… Your choices stay available if an app needs more time.",false);Bundle spec=new Bundle();spec.putInt("host",getTaskId());final String a=state.left,b=state.right;final int g=launchGeneration;final long began=android.os.SystemClock.elapsedRealtime();logLaunch("prepare_begin",g,0);
        new Thread(()->{WindowBrokerClient.Result result=CanvasClient.call(CanvasClient.PREPARE,a,b,0,spec);runOnUiThread(()->{if(g!=launchGeneration||isDestroyed()||isFinishing())return;busy=false;launchButton.setEnabled(true);logLaunch(result.ok?"prepare_ready":"prepare_warning",g,android.os.SystemClock.elapsedRealtime()-began);if(result.ok){state.active=true;state.save();startActivity(new Intent(this,PairCanvasActivity.class).putExtra("new_layout",true));finish();}else{state.active=false;state.save();showLaunchStatus(result.message,true);}});},"prepare-two-apps").start();
    }
    private void showLaunchStatus(String value,boolean retry){launchStatus.setText(value);launchStatus.setVisibility(View.VISIBLE);recoveryActions.setVisibility(retry?View.VISIBLE:View.GONE);}
    private void logLaunch(String event,int g,long elapsed){android.util.Log.i("KinegraLayout","event="+event+" generation="+g+" host="+getTaskId()+" elapsed_ms="+elapsed);}
    @Override public void onDestroy(){launchGeneration++;super.onDestroy();}
    private void showSetupRequired(){
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Two-app setup required")
            .setMessage("Connect this app to the Shield's own debugging service to start window support. No PC is needed after Android remembers this app's key.\n\nNetwork debugging must be enabled in Developer options. Android may ask you once to Allow debugging: select Always allow for Kinegra Home. The key stays in this app's private storage. This grants the app local ADB access for its window helper.")
            .setPositiveButton("Connect locally",(dialog,which)->connectLocally()).setNegativeButton("Back to choices",null)
            .setNeutralButton("Check connection",(dialog,which)->launchLayout()).create();
        d.setOnKeyListener((dialog,key,e)->{if(key==android.view.KeyEvent.KEYCODE_BUTTON_B){if(e.getAction()==android.view.KeyEvent.ACTION_UP)d.dismiss();return true;}if(key==android.view.KeyEvent.KEYCODE_BUTTON_A){if(e.getAction()==android.view.KeyEvent.ACTION_UP&&d.getCurrentFocus()!=null)d.getCurrentFocus().performClick();return true;}return false;});
        d.show();d.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    }
    private void connectLocally(){
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Connecting local window support").setMessage("Preparing…").setNegativeButton("Cancel",(dialog,which)->LocalAdbBootstrap.cancel()).create();d.setCancelable(false);d.show();
        LocalAdbBootstrap.connect(this,new LocalAdbBootstrap.Listener(){public void status(String text){if(!isDestroyed()&&d.isShowing())d.setMessage(text);}public void done(boolean ok,String text){if(isDestroyed()||isFinishing()||!d.isShowing())return;d.dismiss();if(ok)launchLayout();else new AlertDialog.Builder(PairActivity.this).setTitle("Local setup needs attention").setMessage(text).setPositiveButton("Retry",(dialog,which)->connectLocally()).setNegativeButton("Back to choices",null).show();}});
    }
    private void add(LinearLayout body,View v){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));body.addView(v,p);}
    private void refresh(){if(left==null)return;boolean pip=ProjectionGeometry.isPip(state.mode);left.setText((pip?"Main app: ":"Left app: ")+label(selectionIsLeft(true)?state.left:state.right)+"  ▾");right.setText((pip?"PiP app: ":"Right app: ")+label(selectionIsLeft(false)?state.left:state.right)+"  ▾");layout.setText("Layout: "+ProjectionGeometry.NAMES[state.mode]+(ProjectionGeometry.isPip(state.mode)?"  /  "+state.size+"%":state.mode==ProjectionGeometry.ADJUSTABLE?"  /  "+state.split+":"+(100-state.split):"")+"  ▾");if(splitControls!=null){splitControls.setVisibility(state.mode==ProjectionGeometry.ADJUSTABLE?View.VISIBLE:View.GONE);splitCaption.setText("Split: "+state.split+":"+(100-state.split)+" — Left / Right adjusts 1%");pipControls.setVisibility(ProjectionGeometry.isPip(state.mode)?View.VISIBLE:View.GONE);}preview.invalidate();}
    private boolean selectionIsLeft(boolean first){return state.swapped?!first:first;}
    private void chooseApp(boolean isLeft){
        if(apps.isEmpty()){message.setText("No launchable apps were found");return;}
        String[] names=new String[apps.size()];int selected=0;
        for(int n=0;n<apps.size();n++){names[n]=apps.get(n).title;if(apps.get(n).pkg.equals(isLeft?state.left:state.right))selected=n;}
        choose("Choose "+PaneRecoveryPolicy.role(isLeft?0:1,ProjectionGeometry.isPip(state.mode),state.swapped)+" app",names,selected,index->{if(isLeft)state.left=apps.get(index).pkg;else state.right=apps.get(index).pkg;state.save();refresh();});
    }
    private interface Selected{void choose(int index);}
    private void chooseLayout(){choose("Window shapes",ProjectionGeometry.NAMES,state.mode,index->{state.mode=index;state.save();refresh();});}
    private void choose(String title,String[] names,int selected,Selected action){
        AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(names,selected,(dialog,index)->{action.choose(index);dialog.dismiss();}).setNegativeButton("Cancel",null).create();
        ChoiceKeyPolicy keys=new ChoiceKeyPolicy();
        d.setOnKeyListener((dialog,key,event)->{
            ListView list=d.getListView();int position=list.getSelectedItemPosition();
            int result=keys.handle(event.getDeviceId(),key,event.getAction(),event.getRepeatCount(),event.isCanceled(),position);
            if(result==2&&position>=0){action.choose(position);d.dismiss();}if(result==3)d.cancel();return result!=0;
        });
        d.setOnShowListener(dialog->{d.getListView().setSelection(selected);d.getListView().requestFocus();});d.show();
    }
    private void setup(){new AlertDialog.Builder(this).setTitle("Kinegra Home viewports").setMessage("Window support starts using this app's private local debugging key. Enable Network debugging in Developer options and approve Android's one-time Always allow prompt if shown. Your trusted computer remains a recovery route.\n\nBack + OK (Select + Start) swaps views. Hold Back / Select for controls. Enable the optional Kinegra session shortcuts service in Accessibility for shortcuts while another app has focus. It reads no screen contents.\n\nAdjustable split: select its slider and use Left / Right. Fit 16:9 video gives a full-height 16:9 pane when the screen is wide enough, with a minimum 20% companion pane. Video content itself may contain bars.\n\nTested on the configured Android 11 Shield. Other firmware, apps and DRM can reject embedded layouts. This is still a normal app: the stock Home launcher remains enabled.").setPositiveButton("Close",null).show();}
    private void browserHelp(){new AlertDialog.Builder(this).setTitle("TV Bro and Music Assistant").setMessage("Focus the TV Bro pane. On the page, hold centre OK to open its cursor menu. Left zooms out (−); Right zooms in (+). Repeat to adjust.\n\nUse Music Assistant's web page for library browsing and controls inside this pane. Selecting the Shield's Cast receiver as the player may launch a separate full-screen app. A web player, where available, or ShieldAmp can keep playback in the selected app.").setPositiveButton("Close",null).show();}
    @Override void afterAction(int operation,boolean ok){if(ok&&operation==PairProtocol.APPLY)finish();}
    private final class Preview extends View {
        private final Paint paint=new Paint(3);
        Preview(){super(PairActivity.this);setContentDescription("Preview of the tiled or picture in picture layout");}
        @Override protected void onDraw(Canvas canvas){
            Point real=new Point();getWindowManager().getDefaultDisplay().getRealSize(real);
            ProjectionGeometry.Box[] boxes=ProjectionGeometry.compute(real.x,real.y,state.mode,state.size,state.swapped,state.split);
            float scale=Math.min(getWidth()/(float)real.x,getHeight()/(float)real.y),x=(getWidth()-real.x*scale)/2,y=(getHeight()-real.y*scale)/2;
            canvas.save();canvas.translate(x,y);canvas.scale(scale,scale);
            int[] order=ProjectionGeometry.isPip(state.mode)&&state.swapped?new int[]{1,0}:new int[]{0,1};
            for(int n:order){ProjectionGeometry.Box box=boxes[n];paint.setColor(n==0?0xff397a88:0xff8c6b32);canvas.drawRect(box.x,box.y,box.x+box.w,box.y+box.h,paint);paint.setColor(FG);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(16/scale);boolean first=n==(state.swapped?1:0);canvas.drawText(ProjectionGeometry.isPip(state.mode)?(first?"MAIN APP":"PiP APP"):(first?"LEFT APP":"RIGHT APP"),box.x+box.w/2f,box.y+box.h/2f,paint);}
            canvas.restore();
        }
    }
}
