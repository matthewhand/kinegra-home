package tv.metaxy.dashboard;
/** Exact own-component check; never enables or disables an Android service. */
public final class SessionShortcutPolicy {
 private SessionShortcutPolicy(){}
 public static boolean enabled(String ownPackage,String services,boolean accessibilityEnabled){
  if(!accessibilityEnabled||ownPackage==null||ownPackage.isEmpty()||services==null)return false;
  for(String entry:services.split(":"))if(entry.equals(ownPackage+"/.SessionShortcutService")||entry.equals(ownPackage+"/"+ownPackage+".SessionShortcutService"))return true;
  return false;
 }
 public static String caption(boolean enabled){return enabled?"Hold Back for app controls. Back + OK swaps views.":"Back-hold is off. Enable session shortcuts in Android Accessibility.";}
}
