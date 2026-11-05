package tv.metaxy.proof;
import android.app.ActivityManager;import android.content.ComponentName;import android.content.Context;
import tv.metaxy.dashboard.RecentCanvasPolicy;
/** Prunes only this app's old canvas records; never selected apps or the live canvas. */
final class CanvasTaskHistory {
 static void trim(Context context){
  int removed=0;PairCanvasActivity live=PairCanvasActivity.current;int currentId=live==null?-1:live.getTaskId();
  try{
   ActivityManager manager=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);if(manager==null)return;
   for(ActivityManager.AppTask task:manager.getAppTasks()){
    ActivityManager.RecentTaskInfo info=task.getTaskInfo();if(info==null||info.baseIntent==null)continue;
    ComponentName component=info.baseIntent.getComponent();if(component==null)continue;
    int action=RecentCanvasPolicy.action(info.persistentId,currentId,component.getPackageName(),component.getClassName());
    if(action==RecentCanvasPolicy.HIDE_CURRENT)task.setExcludeFromRecents(true);
    else if(action==RecentCanvasPolicy.REMOVE_STALE){task.finishAndRemoveTask();removed++;}
   }
  }catch(RuntimeException unavailable){android.util.Log.i("KinegraHome","CANVAS_HISTORY cleanup unavailable");}
  if(removed>0)android.util.Log.i("KinegraHome","CANVAS_HISTORY pruned="+removed);
 }
 private CanvasTaskHistory(){}
}
