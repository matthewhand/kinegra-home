import tv.metaxy.dashboard.SessionShortcutPolicy;
public final class test_session_shortcut_policy {
 public static void main(String[] args){
  String own="example.home";int checks=0;
  String[] accepted={own+"/.SessionShortcutService",own+"/"+own+".SessionShortcutService","other/.Service:"+own+"/.SessionShortcutService"};
  for(String value:accepted){if(!SessionShortcutPolicy.enabled(own,value,true))throw new AssertionError(value);checks++;}
  String[] rejected={null,"","other/.SessionShortcutService",own+"/.SessionShortcutServiceExtra",own+".extra/.SessionShortcutService",own+"/.SessionShortcutService/extra"};
  for(String value:rejected){if(SessionShortcutPolicy.enabled(own,value,true))throw new AssertionError(value);checks++;}
  if(SessionShortcutPolicy.enabled(own,accepted[0],false))throw new AssertionError("global accessibility off");checks++;
  if(!SessionShortcutPolicy.caption(false).contains("Back-hold is off")||!SessionShortcutPolicy.caption(true).contains("Hold Back"))throw new AssertionError("honest recovery captions");checks++;
  System.out.println("SESSION_SHORTCUT_POLICY_CHECKS="+checks);
 }
}
