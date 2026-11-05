package tv.metaxy.dashboard;
public final class test_controller_hat {
 static int n;static void check(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args){ControllerHatPolicy p=new ControllerHatPolicy();
  check(p.motion(1,1,0)==2,"hat-only right navigates");check(p.motion(1,1,0)==0,"held hat does not double navigate");check(p.motion(1,0,0)==0,"release does not navigate");check(p.motion(1,1,0)==2,"next press rearms");
  p.reset();check(p.motion(1,-1,0)==1,"left");p.motion(1,0,0);check(p.motion(1,0,-1)==3,"up");p.motion(1,0,0);check(p.motion(1,0,1)==4,"down");
  p.reset();check(p.motion(1,1,0)==2,"hat arrives first");check(p.key(1,2,0),"duplicate key down consumed");check(p.key(1,2,1),"matching key up consumed");p.motion(1,0,0);
  p.reset();check(!p.key(1,2,0),"native key arrives first and is forwarded");check(p.motion(1,1,0)==0,"following hat does not repeat native key");check(!p.key(1,2,1),"native release reaches key coordinator");p.motion(1,0,0);
  check(p.motion(1,Float.NaN,0)==0,"bad sample ignored");check(p.motion(2,0,-1)==3,"devices independent");check(p.motion(1,0,1)==4,"second device cannot latch first");
  p.resetDevice(1);check(p.motion(1,0,1)==4,"hotplug reset rearms");check(p.motion(-1,0,1)==0,"invalid device ignored");
  System.out.println("CONTROLLER_HAT_CHECKS="+n);
 }
}
