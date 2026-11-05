package tv.metaxy.dashboard;
/** Only our obsolete canvas task records are eligible for cleanup. */
public final class RecentCanvasPolicy {
 public static final int KEEP=0,HIDE_CURRENT=1,REMOVE_STALE=2;
 public static int action(int taskId,int currentId,String pkg,String activity){
  if(taskId<=0||!"tv.metaxy.proof".equals(pkg)||!"tv.metaxy.proof.PairCanvasActivity".equals(activity))return KEEP;
  return taskId==currentId?HIDE_CURRENT:REMOVE_STALE;
 }
 private RecentCanvasPolicy(){}
}
