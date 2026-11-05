package tv.metaxy.dashboard;
/** An A press may only select the same chooser row on its matching release. */
public final class ChoiceKeyPolicy {
    private int device=-1,position=-1;
    public int handle(int id,int key,int action,int repeat,boolean cancelled,int selected){
        if(key!=96&&key!=97)return 0;
        if(key==97)return action==1&&!cancelled?3:1;
        if(action==0){if(repeat==0&&!cancelled&&device<0){device=id;position=selected;}return 1;}
        if(action==1&&device==id){boolean ok=!cancelled&&position>=0&&selected==position;device=-1;position=-1;return ok?2:1;}
        return 1;
    }
}
