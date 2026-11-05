package tv.metaxy.dashboard;

/** Display-relative window shapes, independent of Android and app orientation. */
public final class PairGeometry {
    private PairGeometry() { }
    public static final String[] NAMES={"Equal panes (50:50)","Landscape + portrait","Portrait + landscape","Two-thirds + one-third (67:33)","One-third + two-thirds (33:67)"};
    public static final class Box {
        public final int left,top,right,bottom;
        public Box(int l,int t,int r,int b){left=l;top=t;right=r;bottom=b;}
        public int width(){return right-left;} public int height(){return bottom-top;}
        public boolean overlaps(Box b){return left<b.right&&right>b.left&&top<b.bottom&&bottom>b.top;}
    }
    public static final class Layout {
        public final Box left,right,dock;
        Layout(Box a,Box b,Box c){left=a;right=b;dock=c;}
    }
    public static Layout compute(int w,int h,int mode,int percent,int controlsHeight){
        if(w<320||h<240||mode<0||mode>=NAMES.length||percent<60||percent>100)throw new IllegalArgumentException("Invalid layout");
        int gap=Math.max(8,Math.round(Math.min(w,h)*0.02f));
        int dockH=Math.max(64,Math.min(h/3,controlsHeight));
        int availableW=w-3*gap,availableH=h-3*gap-dockH;
        if(availableW<80||availableH<80)throw new IllegalArgumentException("Display too small");
        int height,leftW,rightW;
        if(mode==0){height=availableH*percent/100;leftW=(availableW/2)*percent/100;rightW=leftW;}
        else if(mode==3||mode==4){
            height=availableH*percent/100;
            int total=availableW*percent/100,narrow=total/3,wide=total-narrow;
            leftW=mode==3?wide:narrow;rightW=mode==3?narrow:wide;
        }
        else{
            height=(int)Math.floor(Math.min(availableH,availableW/(16.0/9+9.0/16))*percent/100.0);
            int wide=(int)Math.round(height*16.0/9),narrow=(int)Math.round(height*9.0/16);
            leftW=mode==1?wide:narrow;rightW=mode==1?narrow:wide;
        }
        int x=(w-leftW-rightW-gap)/2,y=gap+(availableH-height)/2;
        return new Layout(new Box(x,y,x+leftW,y+height),new Box(x+leftW+gap,y,x+leftW+gap+rightW,y+height),new Box(gap,h-gap-dockH,w-gap,h-gap));
    }
}
