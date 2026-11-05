package tv.metaxy.dream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.service.dreams.DreamService;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Shieldarchy Dream, bundled inside the original Metaxy TV Android package.
 *
 * It is intentionally non-interactive so Android itself dismisses the dream
 * on the first physical remote/gamepad input. No Accessibility, networking,
 * screen-reading, usage-observation, or background service is needed.
 */
public final class MetaxyDreamService extends DreamService {
    private DreamCanvas surface;

    @Override public void onAttachedToWindow(){
        super.onAttachedToWindow();
        setInteractive(false);
        setFullscreen(true);
        setScreenBright(false);
        surface=new DreamCanvas(this);
        setContentView(surface);
    }
    @Override public void onDreamingStarted(){
        super.onDreamingStarted();
        if(surface!=null)surface.begin();
    }
    @Override public void onDreamingStopped(){
        if(surface!=null)surface.end();
        super.onDreamingStopped();
    }
    @Override public void onDetachedFromWindow(){
        if(surface!=null){surface.end();surface=null;}
        super.onDetachedFromWindow();
    }
    private static final class DreamCanvas extends View {
        private static final int BACKGROUND=Color.rgb(3,11,17);
        private static final int NEON=Color.rgb(91,224,167);
        private static final int DIM=Color.rgb(40,110,93);
        private static final int WHITE=Color.rgb(185,232,214);
        private static final int BLUE=Color.rgb(52,137,157);
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final SimpleDateFormat timeFormat=
            new SimpleDateFormat("HH:mm:ss",Locale.getDefault());
        private final Date nowDate=new Date();
        private long startTime=0;
        private long lastClockSecond=-1;
        private String clockText="--:--:--";
        private boolean active=false;
        private int lastSlide=-1;
        private String topLine="",lowerLine="";
        private final char[] oneChar=new char[1];
        DreamCanvas(Context context){
            super(context);
            paint.setTypeface(Typeface.MONOSPACE);
        }
        void begin(){active=true;startTime=SystemClock.uptimeMillis();invalidate();}
        void end(){active=false;invalidate();}
        private void print(Canvas canvas,String text,float x,float y,float size,int color,boolean center){
            paint.setColor(color);
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.MONOSPACE);
            paint.setTextSize(size);
            paint.setTextAlign(center?Paint.Align.CENTER:Paint.Align.LEFT);
            canvas.drawText(text,x,y,paint);
        }
        private void stroke(Canvas canvas,float x0,float y0,float x1,float y1,int color){
            paint.setColor(color);
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(x0,y0,x1,y1,paint);
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            int width=getWidth(),height=getHeight();
            canvas.drawColor(BACKGROUND);
            if(width<1 || height<1 || !active)return;
            long elapsed=Math.max(0,SystemClock.uptimeMillis()-startTime);
            long frame=DreamCadence.frame(elapsed);
            int slide=DreamCadence.slide(elapsed);
            if(slide!=lastSlide){
                lastSlide=slide;
                topLine=DreamCadence.label(slide);
                lowerLine=DreamCadence.lowerText(slide);
            }
            long currentSec=System.currentTimeMillis()/1000L;
            if(currentSec!=lastClockSecond){
                lastClockSecond=currentSec;
                nowDate.setTime(currentSec*1000L);
                clockText=timeFormat.format(nowDate);
            }
            // Match the real 3440x1440 ultrawide proportions while retaining
            // correct readable text at 16:9 and all supported TV resolutions.
            float s=Math.min(width/1720f,height/720f);
            float cx=width/2f,cy=height/2f;
            float dx=DreamCadence.driftX(elapsed)*s;
            float dy=DreamCadence.driftY(elapsed)*s;
            // Subtle cadence scan rails, not a full-frame shader.
            for(int i=1;i<=6;i++){
                float fy=height*i/7f;
                stroke(canvas,width*.04f,fy,width*.96f,fy,DIM);
            }
            stroke(canvas,width*.04f,height*.15f,width*.96f,height*.15f,NEON);
            stroke(canvas,width*.04f,height*.84f,width*.96f,height*.84f,NEON);
            // Bounded procedural glyphs (max 36 per frame) ensure no allocation
            // or activity data capture in the animation hot path.
            int count=DreamCadence.clamp(DreamCadence.columns(width)/3,12,36);
            for(int i=0;i<count;i++){
                float x=width*(i+1f)/(count+1f);
                float progress=((frame/2L+i*7L)%44L)/44f;
                float y=height*(.18f+.63f*progress);
                oneChar[0]=DreamCadence.glyph(frame,i,(int)(progress*32));
                paint.setTypeface(Typeface.MONOSPACE);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTextSize(18*s);
                paint.setColor((i%3==0)?BLUE:DIM);
                canvas.drawText(oneChar,0,1,x,y,paint);
            }
            float y0=cy-128*s+dy;
            print(canvas,"SHIELDARCHY",cx+dx,y0,91*s,NEON,true);
            print(canvas,"[  METAXY  //  DREAM  v0.1  ]",cx+dx,y0+49*s,23*s,WHITE,true);
            print(canvas,topLine,cx+dx,y0+122*s,32*s,WHITE,true);
            print(canvas,lowerLine,cx+dx,y0+176*s,21*s,BLUE,true);
            if(slide==2){
                print(canvas,clockText,cx+dx,y0+244*s,48*s,NEON,true);
            }else if(slide==3){
                print(canvas,"# _  01 / 10 / 11 / 00",cx+dx,y0+244*s,25*s,NEON,true);
            }else{
                print(canvas,"< >  . : * + / |  0 1",cx+dx,y0+244*s,24*s,DIM,true);
            }
            print(canvas,"OFFLINE  /  NO MEDIA ACCESS  /  PRESS ANY KEY TO WAKE",
                cx,height*.905f,17*s,WHITE,true);
            print(canvas,"SHIELD  ::  SYNTHETIC CADENCE",
                width*.055f,height*.11f,15*s,DIM,false);
            if(active)postInvalidateDelayed(DreamCadence.FRAME_MS);
        }
    }
}
