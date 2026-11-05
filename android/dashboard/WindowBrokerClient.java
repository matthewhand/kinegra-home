package tv.metaxy.proof;
import android.os.IBinder;
import android.os.Parcel;
import tv.metaxy.dashboard.PairProtocol;
public final class WindowBrokerClient {
    public static final class Result {
        public final boolean ok;public final String message;public final int failedPanes;
        Result(boolean value,String text){this(value,text,0);}
        Result(boolean value,String text,int mask){ok=value;message=text;failedPanes=mask>=0&&mask<=3?mask:0;}
    }
    public static boolean connected(){return WindowBrokerProvider.current()!=null;}
    public static Result call(int operation,String left,String right,int mode,int size){
        IBinder binder=WindowBrokerProvider.current();
        if(binder==null)return new Result(false,"Window helper disconnected. Open Setup to reconnect.");
        Parcel data=Parcel.obtain(),reply=Parcel.obtain();
        try{
            data.writeInterfaceToken(PairProtocol.DESCRIPTOR);data.writeString(left);data.writeString(right);data.writeInt(mode);data.writeInt(size);
            if(!binder.transact(operation,data,reply,0))return new Result(false,"Helper does not support this action");
            reply.readException();return new Result(reply.readInt()==1,reply.readString());
        }catch(Exception e){return new Result(false,"Window action failed: "+e.getClass().getSimpleName());}
        finally{data.recycle();reply.recycle();}
    }
}
