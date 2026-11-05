package tv.metaxy.proof;
import android.accessibilityservice.*;import android.os.*;import android.view.*;import android.view.accessibility.*;
import tv.metaxy.dashboard.ShortcutPolicy;
/** No screen retrieval or event logging. The filter is dormant outside our session. */
public final class SessionShortcutService extends AccessibilityService {
 private final ShortcutPolicy keys=new ShortcutPolicy();private final Handler h=new Handler();
 private final Runnable hold=()->{PairCanvasActivity a=PairCanvasActivity.current;if(a!=null&&a.sessionVisible())run(a,keys.hold(SystemClock.uptimeMillis()));};
 @Override protected void onServiceConnected(){AccessibilityServiceInfo info=getServiceInfo();info.eventTypes=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;info.flags|=AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;setServiceInfo(info);}
 @Override public void onAccessibilityEvent(AccessibilityEvent e){}
 @Override public void onInterrupt(){keys.reset();h.removeCallbacks(hold);}
 @Override protected boolean onKeyEvent(KeyEvent e){
  PairCanvasActivity a=PairCanvasActivity.current;
  if(a==null||!a.sessionVisible()||(e.getFlags()&KeyEvent.FLAG_FALLBACK)!=0){keys.reset();return false;}
  if(a.controlsVisible()&&!keys.pending())return false;
  int action=keys.key(e.getKeyCode(),e.getAction(),e.getRepeatCount(),SystemClock.uptimeMillis());run(a,action);return (action&ShortcutPolicy.CONSUME)!=0;
 }
 private void run(PairCanvasActivity a,int action){
  if((action&ShortcutPolicy.SCHEDULE)!=0){h.removeCallbacks(hold);h.postDelayed(hold,810);}
  if((action&ShortcutPolicy.SWAP)!=0){h.removeCallbacks(hold);a.swapViews();}
  if((action&ShortcutPolicy.CONTROLS)!=0)a.showControls();
  if((action&ShortcutPolicy.BACK)!=0){h.removeCallbacks(hold);a.forwardBack(keys.forwardedKey());}
 }
}
