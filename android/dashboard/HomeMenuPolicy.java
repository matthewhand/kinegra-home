package tv.metaxy.dashboard;
import java.util.*;
/** Only successful in-app launches become bounded recent entries; no usage access. */
public final class HomeMenuPolicy {
 public static boolean valid(String pkg){return pkg!=null&&pkg.length()<180&&pkg.matches("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+");}
 public static String[] record(String[] before,String pkg){LinkedHashSet<String> order=new LinkedHashSet<>();if(valid(pkg))order.add(pkg);if(before!=null)for(String p:before)if(valid(p))order.add(p);ArrayList<String> result=new ArrayList<>();for(String p:order){result.add(p);if(result.size()==4)break;}return result.toArray(new String[0]);}
 private HomeMenuPolicy(){}
}
