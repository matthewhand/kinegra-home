package tv.metaxy.dashboard;
/** Only Back + OK or Select + Start is reserved, and only during a visible projection session. */
public final class ShortcutPolicy {
 public static final int CONSUME=1,SWAP=2,CONTROLS=4,BACK=8,SCHEDULE=16;
 private boolean down,used,ok;private int base=4;private long started;
 public int key(int code,int action,int repeat,long now){
  boolean back=code==4||code==109,select=base==109?code==108:(code==23||code==66||code==96);
  if(back&&down&&code!=base){if(action==0&&repeat==0)reset();else return 0;}
  if(back){if(action==0){if(repeat==0){down=true;base=code;used=false;ok=false;started=now;return CONSUME|SCHEDULE;}return CONSUME;}
   if(action==1){boolean send=down&&!used;down=false;used=false;return CONSUME|(send?BACK:0);}return CONSUME;}
  if(select&&action==0&&down&&!used&&repeat==0){used=true;ok=true;return CONSUME|SWAP;}
  if(select&&ok){if(action==1)ok=false;return CONSUME;}return 0;
 }
 public int hold(long now){if(down&&!used&&now-started>=800){used=true;return CONTROLS;}return 0;}
 public int forwardedKey(){return base;}
 public boolean pending(){return down||ok;}
 public void reset(){down=used=ok=false;base=4;}
}
