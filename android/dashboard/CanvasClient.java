package tv.metaxy.proof;
import android.os.*;import tv.metaxy.dashboard.PairProtocol;
final class CanvasClient {
 static final int START=20,FOCUS=21,BACK=22,SUSPEND=23,UPDATE=24,HEALTH=25,PREPARE=26,RELOAD=27;
 static WindowBrokerClient.Result call(int op,String left,String right,int a,int b,int host){return call(op,left,right,a,null);}
 static WindowBrokerClient.Result call(int op,String left,String right,int a,Bundle spec){
  IBinder broker=WindowBrokerProvider.current();if(broker==null)return new WindowBrokerClient.Result(false,"Window helper disconnected. Reconnect from your trusted ADB computer.");
  Parcel p=Parcel.obtain(),r=Parcel.obtain();try{p.writeInterfaceToken(PairProtocol.DESCRIPTOR);
   if(op==START||op==PREPARE){p.writeString(left);p.writeString(right);p.writeBundle(spec);}
   else if(op==FOCUS||op==BACK||op==RELOAD)p.writeInt(a);else if(op==UPDATE)p.writeBundle(spec);
   if(!broker.transact(op,p,r,0))return new WindowBrokerClient.Result(false,"Unsupported helper action");r.readException();boolean ok=r.readInt()==1;String message=r.readString();return new WindowBrokerClient.Result(ok,message,r.dataAvail()>=4?r.readInt():0);
  }catch(Exception e){return new WindowBrokerClient.Result(false,"Window action failed: "+e.getClass().getSimpleName());}finally{p.recycle();r.recycle();}
 }
}
