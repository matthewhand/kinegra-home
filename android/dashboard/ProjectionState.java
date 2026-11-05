package tv.metaxy.proof;
import android.content.*;
import tv.metaxy.dashboard.ProjectionGeometry;
public final class ProjectionState {
 public String left,right;public int mode,size,split;public boolean swapped,active;
 private final SharedPreferences p;
 public ProjectionState(Context c){p=c.getSharedPreferences("projection-v1",0);PairState old=new PairState(c);left=p.getString("left",old.left);right=p.getString("right",old.right);android.graphics.Point screen=new android.graphics.Point();((android.view.WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(screen);int first=screen.x/(float)screen.y>=2.23f?ProjectionGeometry.VIDEO_LEFT:ProjectionGeometry.ADJUSTABLE;mode=p.getInt("mode",first);size=p.getInt("size",30);split=p.getInt("split",70);swapped=p.getBoolean("swapped",false);active=p.getBoolean("active",false);if(mode<0||mode>=ProjectionGeometry.NAMES.length)mode=first;if(size<20||size>40)size=30;if(split<20||split>80)split=70;}
 public void selectFamily(String family){if("pip".equals(family)&&!ProjectionGeometry.isPip(mode))mode=p.getInt("last-pip",3);else if("split".equals(family)&&ProjectionGeometry.isPip(mode))mode=p.getInt("last-split",ProjectionGeometry.ADJUSTABLE);if(mode<0||mode>=ProjectionGeometry.NAMES.length)mode="pip".equals(family)?3:ProjectionGeometry.ADJUSTABLE;}
 public void save(){p.edit().putInt(ProjectionGeometry.isPip(mode)?"last-pip":"last-split",mode).putString("left",left).putString("right",right).putInt("mode",mode).putInt("size",size).putInt("split",split).putBoolean("swapped",swapped).putBoolean("active",active).apply();}
}
