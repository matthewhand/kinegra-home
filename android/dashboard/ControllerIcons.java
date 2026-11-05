package tv.metaxy.proof;
import android.content.Context;import android.graphics.*;import android.view.View;
/** Original vector-like line art: flat remote and generic gamepad, no vendor logo. */
final class ControllerIcons extends View {
 private final Paint p=new Paint(3);private long remoteUntil,padUntil;
 private final Runnable remoteFade=()->invalidate(),padFade=()->invalidate();
 void activity(boolean gamepad){long until=android.os.SystemClock.uptimeMillis()+650;if(gamepad){padUntil=until;removeCallbacks(padFade);postDelayed(padFade,660);}else{remoteUntil=until;removeCallbacks(remoteFade);postDelayed(remoteFade,660);}invalidate();}
 void resetActivity(){remoteUntil=padUntil=0;removeCallbacks(remoteFade);removeCallbacks(padFade);invalidate();}
 @Override protected void onDetachedFromWindow(){resetActivity();super.onDetachedFromWindow();}
 ControllerIcons(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
 private void style(boolean active){p.setColor(active?0xff6be3b8:0xffc5d6df);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);}
 static final class Gear extends android.graphics.drawable.Drawable {
  private final Paint paint=new Paint(3);
  @Override public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left,b.top);c.scale(b.width()/64f,b.height()/64f);paint.setColor(0xffc5d6df);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(3);paint.setStrokeJoin(Paint.Join.ROUND);Path path=new Path();for(int i=0;i<32;i++){double a=2*Math.PI*i/32;float r=i%4==1||i%4==2?27:22,x=32+r*(float)Math.cos(a),y=32+r*(float)Math.sin(a);if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}path.close();c.drawPath(path,paint);c.drawCircle(32,32,9,paint);c.restore();}
  @Override public void setAlpha(int alpha){paint.setAlpha(alpha);}@Override public void setColorFilter(ColorFilter f){paint.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}@Override public int getIntrinsicWidth(){return 64;}@Override public int getIntrinsicHeight(){return 64;}
 }
 @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();float s=Math.min(getWidth()/92f,getHeight()/48f);c.translate((getWidth()-92*s)/2,(getHeight()-48*s)/2);c.scale(s,s);
  style(android.os.SystemClock.uptimeMillis()<remoteUntil);c.drawRoundRect(6,3,25,45,4,4,p);c.drawCircle(15.5f,15,6,p);c.drawCircle(15.5f,15,2,p);c.drawCircle(11,27,1.5f,p);c.drawCircle(20,27,1.5f,p);c.drawCircle(15.5f,35,2.2f,p);
  style(android.os.SystemClock.uptimeMillis()<padUntil);Path body=new Path();body.moveTo(49,13);body.cubicTo(43,12,41,17,39,25);body.lineTo(36,36);body.cubicTo(35,42,40,44,44,39);body.lineTo(50,33);body.lineTo(72,33);body.lineTo(78,39);body.cubicTo(82,44,87,42,86,36);body.lineTo(83,25);body.cubicTo(81,17,79,12,73,13);body.close();c.drawPath(body,p);c.drawLine(47,23,53,23,p);c.drawLine(50,20,50,26,p);c.drawCircle(72,18,1.4f,p);c.drawCircle(77,23,1.4f,p);c.drawCircle(67,23,1.4f,p);c.drawCircle(72,28,1.4f,p);c.drawCircle(57,27,2.5f,p);c.restore();
 }
}
