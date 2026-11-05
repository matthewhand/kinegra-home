package tv.metaxy.proof;
import android.content.*;import tv.metaxy.dashboard.HomeMenuPolicy;
final class RecentApps {
 static String[] read(Context c){String value=c.getSharedPreferences("home-recents",0).getString("packages","");return HomeMenuPolicy.record(value.split(","),null);}
 static String at(Context c,int index){String[] values=read(c);return index<values.length?values[index]:null;}
 static synchronized void record(Context c,String pkg){if(c.getPackageName().equals(pkg))return;String[] values=HomeMenuPolicy.record(read(c),pkg);StringBuilder b=new StringBuilder();for(String p:values){if(b.length()>0)b.append(',');b.append(p);}c.getSharedPreferences("home-recents",0).edit().putString("packages",b.toString()).apply();}
 private RecentApps(){}
}
