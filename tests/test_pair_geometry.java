package tv.metaxy.dashboard;
public final class test_pair_geometry {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        int[][] screens={{1440,1080},{1920,1080},{3440,1440},{1280,720},{800,600}};
        for(int[] screen:screens) for(int mode=0;mode<5;mode++) for(int scale:new int[]{60,80,100}){
            PairGeometry.Layout p=PairGeometry.compute(screen[0],screen[1],mode,scale,200);
            for(PairGeometry.Box b:new PairGeometry.Box[]{p.left,p.right,p.dock}){
                check(b.left>=0&&b.top>=0&&b.right<=screen[0]&&b.bottom<=screen[1],"inside display");
                check(b.width()>0&&b.height()>0,"positive dimensions");
            }
            check(!p.left.overlaps(p.right),"apps never overlap");
            check(!p.left.overlaps(p.dock)&&!p.right.overlaps(p.dock),"controls remain separate");
            if(mode==1||mode==2){
                PairGeometry.Box wide=mode==1?p.left:p.right;
                PairGeometry.Box portrait=mode==1?p.right:p.left;
                check(wide.width()>wide.height(),"wide pane landscape");
                check(portrait.width()<portrait.height(),"narrow pane portrait");
                check(Math.abs(wide.width()-wide.height()*16.0/9)<2,"16:9 fit");
                check(Math.abs(portrait.width()-portrait.height()*9.0/16)<2,"9:16 fit");
            }
            if(mode==3||mode==4){
                int wide=mode==3?p.left.width():p.right.width();
                int narrow=mode==3?p.right.width():p.left.width();
                check(Math.abs(wide-2*narrow)<=2,"two-thirds / one-third width ratio");
                check(p.left.height()==p.right.height(),"ratio presets use equal heights");
            }
        }
        PairGeometry.Layout large=PairGeometry.compute(3440,1440,1,100,200);
        PairGeometry.Layout small=PairGeometry.compute(3440,1440,1,60,200);
        check(small.left.width()<large.left.width()&&small.right.height()<large.right.height(),"size control scales both panes");
        boolean rejected=false;try{PairGeometry.compute(0,1440,1,100,200);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid display rejected");
        check(BrokerPolicy.ownerAllowed(10173,10173),"owner accepted");
        check(!BrokerPolicy.ownerAllowed(10174,10173)&&!BrokerPolicy.ownerAllowed(2000,10173),"other callers rejected");
        check(!BrokerPolicy.ownerAllowed(2000,2000),"shell cannot be configured as app owner");
        check(BrokerPolicy.packageAllowed("com.google.android.youtube.tv"),"package accepted");
        check(!BrokerPolicy.packageAllowed("app;reboot")&&!BrokerPolicy.packageAllowed("../other")&&!BrokerPolicy.packageAllowed(null),"package injection rejected");
        ChoiceKeyPolicy keys=new ChoiceKeyPolicy();
        check(keys.handle(1,96,0,0,false,3)==1,"A waits for release");
        check(keys.handle(1,96,0,1,false,3)==1,"held A does not select repeatedly");
        check(keys.handle(1,96,1,0,false,3)==2,"matching release selects row");
        keys.handle(1,96,0,0,false,3);
        check(keys.handle(1,96,1,0,false,4)==1,"row change cancels selection");
        keys.handle(1,96,0,0,false,3);
        check(keys.handle(1,96,1,0,true,3)==1,"cancelled press does not select");
        check(keys.handle(1,97,1,0,false,3)==3,"B dismisses chooser");
        check(keys.handle(1,20,0,0,false,3)==0,"D-pad keeps native navigation");
        System.out.println("PAIR_GEOMETRY_AND_ACCESS_CHECKS="+checks);
    }
}
