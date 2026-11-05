package tv.metaxy.proof;
import android.os.*;import android.view.*;
/** App-owned window + app-owned display, using transaction IDs read from this device. */
final class WindowSessionLink {
 static void attach(SurfaceView view,int display) throws Exception {
  int reparent=WindowBrokerProvider.reparentCode,location=WindowBrokerProvider.locationCode;
  if(reparent<=0||location<=0)throw new IllegalStateException("Reconnect the matching viewport helper first");
  Object session=Class.forName("android.view.WindowManagerGlobal").getMethod("getWindowSession").invoke(null);
  IBinder binder=((IInterface)session).asBinder();Parcel p=Parcel.obtain(),r=Parcel.obtain();
  try{p.writeInterfaceToken("android.view.IWindowSession");p.writeStrongBinder(view.getWindowToken());p.writeInt(1);view.getSurfaceControl().writeToParcel(p,0);p.writeInt(display);if(!binder.transact(reparent,p,r,0))throw new IllegalStateException("Embedding unavailable on this firmware");r.readException();}finally{p.recycle();r.recycle();}
  int[] xy=new int[2];view.getLocationInWindow(xy);p=Parcel.obtain();r=Parcel.obtain();
  try{p.writeInterfaceToken("android.view.IWindowSession");p.writeStrongBinder(view.getWindowToken());p.writeInt(xy[0]);p.writeInt(xy[1]);p.writeInt(display);if(!binder.transact(location,p,r,0))throw new IllegalStateException("Input geometry unavailable");r.readException();}finally{p.recycle();r.recycle();}
 }
 private WindowSessionLink(){}
}
