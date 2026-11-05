package tv.metaxy.dashboard;
public class test_projection {
 static int n;static void check(boolean b,String m){n++;if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  for(int[] s:new int[][]{{3440,1440},{1920,1080},{1440,1080},{1280,720},{800,600},{320,240}})for(int mode=0;mode<ProjectionGeometry.NAMES.length;mode++)for(int size:new int[]{20,30,40})for(boolean swapped:new boolean[]{false,true})for(int percent:new int[]{20,35,70,80}){
   ProjectionGeometry.Box[] bs=ProjectionGeometry.compute(s[0],s[1],mode,size,swapped,percent);
   for(ProjectionGeometry.Box b:bs)check(b.x>=0&&b.y>=0&&b.w>0&&b.h>0&&b.x+b.w<=s[0]&&b.y+b.h<=s[1],"within opaque canvas");
   if(!ProjectionGeometry.isPip(mode)){check(!bs[0].overlaps(bs[1]),"tiles do not overlap");check((long)bs[0].w*bs[0].h+(long)bs[1].w*bs[1].h==(long)s[0]*s[1],"tiles cover every pixel");}
   else{ProjectionGeometry.Box main=swapped?bs[1]:bs[0],pip=swapped?bs[0]:bs[1];check(main.w==s[0]&&main.h==s[1],"main fills canvas");check(main.overlaps(pip),"inset overlays main");int[] view=ProjectionGeometry.viewport(pip,s[0],s[1],true);check(view[0]>=480&&view[1]>=720,"inset retains large render viewport");}
  }
  ProjectionGeometry.Box[] video=ProjectionGeometry.compute(3440,1440,ProjectionGeometry.VIDEO_LEFT,30,false);
  check(video[0].w==2560&&video[1].w==880,"16:9 video fits ultrawide without aspect mismatch");
  int[] nativeSide=ProjectionGeometry.viewport(video[1],3440,1440,false);
  check(nativeSide[0]==880&&nativeSide[1]==1440,"browser keeps native side-pane resolution and usable layout width");
  video=ProjectionGeometry.compute(3440,1440,ProjectionGeometry.VIDEO_RIGHT,30,true);
  check(video[0].w==2560&&video[1].w==880,"right video survives app swap");
  check(ProjectionGeometry.compute(3440,1440,ProjectionGeometry.ADJUSTABLE,30,false,70)[0].w==2408,"70:30 split is exact");
  check(ProjectionGeometry.compute(3440,1440,ProjectionGeometry.ADJUSTABLE,30,false,75)[0].w==2580,"slider changes pane width");
  check(ProjectionGeometry.nextCorner(ProjectionGeometry.VIDEO_LEFT)==3&&ProjectionGeometry.nextCorner(6)==3,"video presets never become invalid PiP corners");
  boolean rejected=false;try{ProjectionGeometry.compute(3440,1440,7,30,false,81);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"split cannot hide the companion pane");
  ShortcutPolicy k=new ShortcutPolicy();check((k.key(4,0,0,0)&ShortcutPolicy.SCHEDULE)!=0,"Back starts chord");check((k.key(23,0,0,100)&ShortcutPolicy.SWAP)!=0,"Back OK swaps");check(k.key(23,0,1,150)==ShortcutPolicy.CONSUME,"hold does not repeat swap");k.key(23,1,0,200);check(k.key(4,1,0,220)==ShortcutPolicy.CONSUME,"chord does not leak Back");
  k.reset();k.key(109,0,0,0);check((k.key(108,0,0,100)&ShortcutPolicy.SWAP)!=0,"Select Start swaps");
  k.reset();check(k.key(96,0,0,0)==0 && k.key(97,0,0,50)==0 && k.key(96,1,0,100)==0 && k.key(97,1,0,120)==0,"normal A/B gameplay is untouched");
  k.reset();k.key(109,0,0,0);check((k.key(109,1,0,100)&ShortcutPolicy.BACK)!=0,"short Select is forwarded");
  k.reset();k.key(4,0,0,0);check(k.hold(799)==0,"no early menu");check(k.hold(800)==ShortcutPolicy.CONTROLS,"hold opens controls");check(k.key(4,1,0,900)==ShortcutPolicy.CONSUME,"release does not close menu");
  k.reset();k.key(4,0,0,0);check((k.key(4,1,0,120)&ShortcutPolicy.BACK)!=0,"plain Back forwarded");check(k.key(20,0,0,200)==0,"ordinary keys untouched");
  k.reset();k.key(109,0,0,0);k.key(108,0,0,100);
  check((k.key(4,0,0,500)&ShortcutPolicy.SCHEDULE)!=0,"fresh remote press recovers a lost controller release");
  check((k.key(23,0,0,600)&ShortcutPolicy.SWAP)!=0,"remote works after missing controller releases");
  check((k.key(4,0,0,1000)&ShortcutPolicy.SCHEDULE)!=0,"fresh same-key press recovers a lost release");
  check((k.key(23,0,0,1100)&ShortcutPolicy.SWAP)!=0,"repeat chord works after focus change drops releases");
  System.out.println("PROJECTION_GEOMETRY_AND_SHORTCUT_CHECKS="+n);
 }
}
