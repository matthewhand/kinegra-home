package tv.metaxy.proof;
import java.io.*;import java.net.*;import java.nio.*;import java.util.*;
import com.cgutman.adblib.AdbCrypto;
/** Transport boundary and real loopback exchange tests, without any Android device or keys. */
public final class test_local_adb {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void invalid(byte[] data,String why)throws Exception{try{LocalAdbSession.readPacket(new ByteArrayInputStream(data));throw new AssertionError(why);}catch(IOException expected){checks++;}}
 public static void main(String[] args)throws Exception{
  byte[] payload=new byte[]{0,127,(byte)255};byte[] frame=LocalAdbSession.frame(LocalAdbSession.WRTE,2,3,payload);LocalAdbSession.Packet p=LocalAdbSession.readPacket(new ByteArrayInputStream(frame));check(p.a==2&&p.b==3&&Arrays.equals(p.data,payload),"binary packet round trip");
  byte[] corrupt=frame.clone();corrupt[20]^=1;invalid(corrupt,"bad magic");corrupt=frame.clone();corrupt[24]^=1;invalid(corrupt,"bad checksum");invalid(Arrays.copyOf(frame,25),"truncated payload");
  corrupt=frame.clone();ByteBuffer.wrap(corrupt).order(ByteOrder.LITTLE_ENDIAN).putInt(12,-1);invalid(corrupt,"negative size");ByteBuffer.wrap(corrupt).order(ByteOrder.LITTLE_ENDIAN).putInt(12,1000000);invalid(corrupt,"oversized allocation");
  try{LocalAdbSession.frame(1,0,0,new byte[4097]);throw new AssertionError("unbounded transmit");}catch(IllegalArgumentException expected){checks++;}
  try(ServerSocket server=new ServerSocket(0,1,InetAddress.getByAddress(new byte[]{127,0,0,1}))){
   final Throwable[] error={null};Thread peer=new Thread(()->{try(Socket socket=server.accept()){
    InputStream in=socket.getInputStream();OutputStream out=socket.getOutputStream();LocalAdbSession.Packet c=LocalAdbSession.readPacket(in);check(c.code==LocalAdbSession.CNXN&&c.a==0x01000000,"legacy checksum protocol negotiated");
    out.write(LocalAdbSession.frame(LocalAdbSession.AUTH,1,0,new byte[20]));LocalAdbSession.Packet auth=LocalAdbSession.readPacket(in);check(auth.code==LocalAdbSession.AUTH&&auth.a==2&&auth.data.length==256,"RSA challenge signed");out.write(LocalAdbSession.frame(LocalAdbSession.CNXN,0x01000000,4096,new byte[0]));
    LocalAdbSession.Packet open=LocalAdbSession.readPacket(in);check(new String(open.data,"UTF-8").equals("shell:id -u\0"),"only requested shell stream");out.write(LocalAdbSession.frame(LocalAdbSession.OKAY,77,open.a,new byte[0]));out.write(LocalAdbSession.frame(LocalAdbSession.WRTE,77,open.a,"2000\n".getBytes("UTF-8")));check(LocalAdbSession.readPacket(in).code==LocalAdbSession.OKAY,"stream payload acknowledged");out.write(LocalAdbSession.frame(LocalAdbSession.CLSE,77,open.a,new byte[0]));check(LocalAdbSession.readPacket(in).code==LocalAdbSession.CLSE,"stream close acknowledged");
   }catch(Throwable t){error[0]=t;}});peer.start();
   try(LocalAdbSession client=new LocalAdbSession(server.getLocalPort())){client.authenticate(AdbCrypto.generateAdbKeyPair(b->Base64.getEncoder().encodeToString(b)));check(client.shell("id -u").equals("2000"),"complete authenticated shell exchange");}
   peer.join(5000);check(!peer.isAlive()&&error[0]==null,"peer completed cleanly");if(error[0]!=null)throw new AssertionError(error[0]);
  }
  System.out.println("LOCAL_ADB_TRANSPORT_CHECKS="+checks);
 }
}
