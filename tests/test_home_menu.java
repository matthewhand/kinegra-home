package tv.metaxy.dashboard;
import java.util.*;
public final class test_home_menu {
 static int n;static void check(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  boolean[] all={true,true,true,true,true,true,true,true};
  check(RemoteFocusPolicy.next(0,RemoteFocusPolicy.RIGHT,all,2)==1,"Split then PiP");
  check(RemoteFocusPolicy.next(1,RemoteFocusPolicy.RIGHT,all,2)==-1,"right does not wrap to recents");
  check(RemoteFocusPolicy.next(0,RemoteFocusPolicy.DOWN,all,2)==2,"Split down reaches first recent");
  check(RemoteFocusPolicy.next(1,RemoteFocusPolicy.DOWN,all,2)==3,"PiP down keeps column");
  boolean[] empty={true,true,false,false,false,false,true,true};
  check(RemoteFocusPolicy.next(0,RemoteFocusPolicy.DOWN,empty,2)==6,"All apps remains reachable with no recents");
  check(RemoteFocusPolicy.next(7,RemoteFocusPolicy.UP,empty,2)==1,"About returns to PiP across empty recents");
  boolean[] partial={true,true,true,false,false,false,true,true};
  check(RemoteFocusPolicy.next(1,RemoteFocusPolicy.DOWN,partial,2)==2,"single recent is reachable from either column");
  for(int from=0;from<8;from++)for(int d=1;d<=4;d++){int next=RemoteFocusPolicy.next(from,d,all,2);check(next>=-1&&next<8,"bounded focus");}
  String[] r=HomeMenuPolicy.record(new String[]{"app.a","app.b","app.c","app.d"},"app.c");check(Arrays.equals(r,new String[]{"app.c","app.a","app.b","app.d"}),"existing recent moves to front without duplicates");
  r=HomeMenuPolicy.record(r,"app.e");check(Arrays.equals(r,new String[]{"app.e","app.c","app.a","app.b"}),"only four recent apps retained");
  r=HomeMenuPolicy.record(new String[]{"bad,package","app.a","app.a","bad package"},null);check(Arrays.equals(r,new String[]{"app.a"}),"invalid preference entries removed");
  boolean[] ten={true,true,true,true,true,true,true,true,true,true};
  check(RemoteFocusPolicy.next(0,RemoteFocusPolicy.UP,ten,2)==8,"Split up reaches Original Home");
  check(RemoteFocusPolicy.next(1,RemoteFocusPolicy.UP,ten,2)==9,"PiP up reaches Settings");
  check(RemoteFocusPolicy.next(8,RemoteFocusPolicy.DOWN,ten,2)==0,"Original Home down reaches Split");
  check(RemoteFocusPolicy.next(9,RemoteFocusPolicy.DOWN,ten,2)==1,"Settings down reaches PiP");
  check(RemoteFocusPolicy.next(8,RemoteFocusPolicy.LEFT,ten,2)==-1&&RemoteFocusPolicy.next(9,RemoteFocusPolicy.RIGHT,ten,2)==-1,"header does not wrap");
  check(RemoteFocusPolicy.restore(9,ten)==9,"header focus is restored");
  class Port implements RemoteUiSession.ViewPort {int focus=0,activated=-1;public int focusedIndex(){return focus;}public boolean isEnabled(int i){return i>=0&&i<10;}public boolean requestFocus(int i){focus=i;return true;}public boolean activate(int i){activated=i;return true;}public void exitDashboard(){}}
  Port port=new Port();RemoteUiSession session=new RemoteUiSession(port,2,10);session.dispatch(1,19,0,0,false,0,0);session.dispatch(1,19,1,0,false,0,1);check(port.focus==8,"real input coordinator reaches header");session.dispatch(1,23,0,0,false,20,20);session.dispatch(1,23,1,0,false,20,40);check(port.activated==8,"OK activates header through real input coordinator");
  System.out.println("HOME_MENU_POLICY_CHECKS="+n);
 }
}
