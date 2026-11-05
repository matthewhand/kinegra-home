package tv.metaxy.dashboard;
/** Dismissal applies only to the current failure; recovery or a changed issue resets it. */
public final class RecoveryNoticePolicy {
 private String current="",dismissed="";
 public boolean observe(int mask,String message){String next=mask>0?"panes:"+mask:"helper:"+(message==null?"":message);if(!next.equals(current)){current=next;dismissed="";}return !current.equals(dismissed);}
 public boolean suppressed(){return !current.isEmpty()&&current.equals(dismissed);}
 public void dismiss(){dismissed=current;}
 public void resolved(){current="";dismissed="";}
}
