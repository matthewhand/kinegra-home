package tv.metaxy.proof;
import android.content.Context;
import android.content.SharedPreferences;
import tv.metaxy.dashboard.PairGeometry;
public final class PairState {
    public String left,right;public int mode,size;public boolean active;
    private final SharedPreferences prefs;
    public PairState(Context c){prefs=c.getSharedPreferences("pair-v1",0);left=prefs.getString("left","com.google.android.youtube.tv");right=prefs.getString("right","org.local.tegramilkdrop");mode=prefs.getInt("mode",0);size=prefs.getInt("size",100);active=prefs.getBoolean("active",false);if(mode<0||mode>=PairGeometry.NAMES.length)mode=0;if(size<60||size>100)size=100;}
    public void save(){prefs.edit().putString("left",left).putString("right",right).putInt("mode",mode).putInt("size",size).putBoolean("active",active).apply();}
}
