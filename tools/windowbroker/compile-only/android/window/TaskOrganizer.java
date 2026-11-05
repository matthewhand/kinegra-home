package android.window;
import android.app.ActivityManager;import android.view.SurfaceControl;
/** Android 11 platform signatures, compile-time only. */
public class TaskOrganizer {
 public TaskOrganizer(){} public final void registerOrganizer(int mode){} public final void unregisterOrganizer(){}
 public void onTaskAppeared(ActivityManager.RunningTaskInfo t,SurfaceControl s){} public void onTaskInfoChanged(ActivityManager.RunningTaskInfo t){} public void onTaskVanished(ActivityManager.RunningTaskInfo t){}
}
