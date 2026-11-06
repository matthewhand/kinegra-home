package tv.metaxy.proof;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import tv.metaxy.dashboard.PairProtocol;

/** A shell-only Binder handoff. No network socket, token, query or file access. */
public final class WindowBrokerProvider extends ContentProvider {
    private static volatile IBinder current;
    public static IBinder current(){IBinder b=current;return b!=null&&b.pingBinder()?b:null;}
    @Override public boolean onCreate(){return true;}
    @Override public Bundle call(String method,String argument,Bundle extras){
        if("owner_broker".equals(method)&&Binder.getCallingUid()==Process.myUid()){Bundle b=new Bundle();b.putBinder("broker",current());return b;}
        if(Binder.getCallingUid()!=2000)throw new SecurityException("ADB shell attachment only");
        if("seed_recents".equals(method)){String[] packages=extras==null?null:extras.getStringArray("packages");if(packages==null&&argument!=null)packages=argument.split(",");if(packages==null||packages.length!=4)throw new IllegalArgumentException("Choose four apps");android.content.pm.PackageManager pm=getContext().getPackageManager();for(String pkg:packages){if(!tv.metaxy.dashboard.BrokerPolicy.packageAllowed(pkg)||pkg.equals(getContext().getPackageName()))throw new SecurityException("Invalid recent app");android.content.Intent i=pm.getLeanbackLaunchIntentForPackage(pkg);if(i==null)i=pm.getLaunchIntentForPackage(pkg);try{android.content.pm.ActivityInfo info=i==null||i.getComponent()==null?null:pm.getActivityInfo(i.getComponent(),0);if(info==null||!info.exported||!info.enabled||!info.applicationInfo.enabled)throw new SecurityException("App unavailable");}catch(android.content.pm.PackageManager.NameNotFoundException e){throw new IllegalArgumentException("App unavailable");}}for(int n=packages.length-1;n>=0;n--)RecentApps.record(getContext(),packages[n]);Bundle result=new Bundle();result.putInt("recentCount",4);return result;}
        if("status".equals(method)){Bundle b=PairCanvasActivity.diagnostics();b.putBoolean("brokerConnected",current()!=null);b.putBoolean("localAdbConfigured",LocalAdbBootstrap.enabled(getContext()));b.putString("localAdbPhase",LocalAdbBootstrap.phase());return b;}
        if(!"attach".equals(method)||extras==null||extras.getInt("owner",-1)!=Process.myUid())throw new SecurityException("Invalid attachment");
        final IBinder value=extras.getBinder("broker");
        try{
            if(value==null||!PairProtocol.DESCRIPTOR.equals(value.getInterfaceDescriptor()))throw new SecurityException("Wrong helper protocol");
            value.linkToDeath(()->{if(current==value)current=null;},0);
            current=value;
        }catch(android.os.RemoteException e){throw new IllegalStateException("Helper disconnected",e);}
        Bundle result=new Bundle();result.putBoolean("attached",true);return result;
    }
    private static void deny(){throw new SecurityException("No data API");}
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){deny();return null;}
    @Override public String getType(Uri u){deny();return null;}
    @Override public Uri insert(Uri u,ContentValues v){deny();return null;}
    @Override public int delete(Uri u,String s,String[] a){deny();return 0;}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){deny();return 0;}
}
