package tv.metaxy.proof;
import android.content.*;import android.content.pm.*;
/** Remember the existing HOME before an optional replacement is ever considered. */
final class StockHome {
 static Intent intent(Context c){SharedPreferences p=c.getSharedPreferences("original-home",0);ComponentName saved=ComponentName.unflattenFromString(p.getString("component",""));if(valid(c,saved))return home(saved);ResolveInfo r=c.getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),PackageManager.MATCH_DEFAULT_ONLY);if(r!=null){ComponentName current=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);if(valid(c,current)){p.edit().putString("component",current.flattenToString()).apply();return home(current);}}return null;}
 private static boolean valid(Context c,ComponentName component){if(component==null||component.getPackageName().equals(c.getPackageName())||component.getPackageName().equals("android"))return false;try{ActivityInfo i=c.getPackageManager().getActivityInfo(component,0);return i.exported&&i.enabled&&i.applicationInfo.enabled;}catch(PackageManager.NameNotFoundException e){return false;}}
 private static Intent home(ComponentName c){return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setComponent(c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);}
 private StockHome(){}
}
