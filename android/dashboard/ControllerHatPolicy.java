package tv.metaxy.dashboard;
import java.util.HashMap;
import java.util.Map;
/** Handle hat-only D-pads; coalesce devices that also emit native D-pad keys. */
public final class ControllerHatPolicy {
 private static final int LIMIT=8;
 private static final class State{int hat,key;boolean suppressKey;}
 private final Map<Integer,State> states=new HashMap<>();
 private State state(int device){if(device<0)return null;State s=states.get(device);if(s==null&&states.size()<LIMIT){s=new State();states.put(device,s);}return s;}
 public int motion(int device,float x,float y){if(Float.isNaN(x)||Float.isInfinite(x)||Float.isNaN(y)||Float.isInfinite(y))return 0;State s=state(device);if(s==null)return 0;int direction=0;if(Math.max(Math.abs(x),Math.abs(y))>=.5f)direction=Math.abs(x)>=Math.abs(y)?(x>0?2:1):(y>0?4:3);if(direction==s.hat)return 0;s.hat=direction;if(direction==0){if(s.key==0)states.remove(device);return 0;}return s.key==direction?0:direction;}
 /** Return true only for the key half of an already handled hat+key press. */
 public boolean key(int device,int direction,int action){if(direction<1||direction>4)return false;State s=state(device);if(s==null)return false;if(action==0){s.key=direction;s.suppressKey=s.hat==direction;return s.suppressKey;}if(action==1){boolean consume=s.key==direction&&s.suppressKey;s.key=0;s.suppressKey=false;if(s.hat==0)states.remove(device);return consume;}return false;}
 public void resetDevice(int device){states.remove(device);}
 public void reset(){states.clear();}
}
