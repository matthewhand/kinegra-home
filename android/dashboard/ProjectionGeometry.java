package tv.metaxy.dashboard;
/** Complete, opaque tiles or an inset with the same perspective as the screen. */
public final class ProjectionGeometry {
 public static final int ADJUSTABLE=7,VIDEO_LEFT=8,VIDEO_RIGHT=9;
 public static final String[] NAMES={"50:50 split","67:33 split","33:67 split","PiP - top right","PiP - top left","PiP - bottom right","PiP - bottom left","Adjustable split","Fit 16:9 video - left","Fit 16:9 video - right"};
 public static boolean isPip(int mode){return mode>=3&&mode<=6;}
 public static int nextCorner(int mode){return isPip(mode)?3+(mode-2)%4:3;}
 public static final class Box {public final int x,y,w,h;public Box(int a,int b,int c,int d){x=a;y=b;w=c;h=d;}public boolean overlaps(Box b){return x<b.x+b.w&&x+w>b.x&&y<b.y+b.h&&y+h>b.y;}}
 public static Box[] compute(int w,int h,int mode,int size,boolean swapped){
  return compute(w,h,mode,size,swapped,70);
 }
 public static Box[] compute(int w,int h,int mode,int size,boolean swapped,int percent){
  if(w<320||h<240||mode<0||mode>=NAMES.length||size<20||size>40||percent<20||percent>80)throw new IllegalArgumentException("Invalid layout");
  Box a,b;if(!isPip(mode)){int left=mode==0?w/2:mode==1?w*2/3:mode==2?w/3:w*percent/100;
   if(mode==VIDEO_LEFT||mode==VIDEO_RIGHT){int video=Math.min(w*80/100,Math.max(w*20/100,Math.round(h*16f/9f)));left=mode==VIDEO_LEFT?video:w-video;}
   a=new Box(0,0,left,h);b=new Box(left,0,w-left,h);}
  else{int gap=Math.max(8,Math.min(w,h)/80);int pw=Math.max(96,w*size/100),ph=Math.max(64,Math.round(pw*h/(float)w));a=new Box(0,0,w,h);boolean right=mode==3||mode==5,bottom=mode>=5;b=new Box(right?w-gap-pw:gap,bottom?h-gap-ph:gap,pw,ph);}
  return swapped?new Box[]{b,a}:new Box[]{a,b};
 }
 public static int[] viewport(Box box, int screenW,int screenH,boolean pip){
  // Tiled apps use real pane pixels: downsampling while retaining Android's
  // device density doubles control sizes and forces cramped mobile layouts.
  if(!pip)return new int[]{box.w,box.h};
  int height=720,width=Math.round(height*(pip?screenW/(float)screenH:box.w/(float)box.h));
  return new int[]{Math.max(480,Math.min(1920,width)),height};
 }
 private ProjectionGeometry(){}
}
