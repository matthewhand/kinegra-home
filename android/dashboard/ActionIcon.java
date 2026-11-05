package tv.metaxy.proof;
import android.graphics.*;import android.graphics.drawable.Drawable;
/** Original line icons for workspace actions; installed apps keep their own icons. */
final class ActionIcon extends Drawable {
 static final int SPLIT=1,PIP=2,APPS=3,ABOUT=4,HOME=5,SETTINGS=6;
 private final int kind;private final Paint p=new Paint(3);private int opacity=255;
 ActionIcon(int kind){this.kind=kind;}
 @Override public void draw(Canvas c){Rect b=getBounds();c.save();c.translate(b.left,b.top);c.scale(b.width()/64f,b.height()/64f);p.setColor(0xff6be3b8);p.setAlpha(opacity);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.7f);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
  switch(kind){
   case SPLIT:c.drawRoundRect(5,10,59,50,5,5,p);c.drawLine(39,10,39,50,p);break;
   case PIP:c.drawRoundRect(5,10,59,50,5,5,p);c.drawRoundRect(37,30,53,44,2,2,p);break;
   case APPS:for(int row=0;row<2;row++)for(int col=0;col<2;col++)c.drawRoundRect(9+col*27,9+row*27,27+col*27,27+row*27,3,3,p);break;
   case ABOUT:c.drawCircle(27,27,21,p);c.drawLine(27,25,27,39,p);c.drawCircle(27,17,1,p);p.setStyle(Paint.Style.FILL);p.setColor(0xff132733);c.drawRect(41,35,63,59,p);p.setColor(0xff6be3b8);p.setStyle(Paint.Style.STROKE);Path heart=new Path();heart.moveTo(51,54);heart.cubicTo(30,40,44,32,51,41);heart.cubicTo(58,32,72,40,51,54);c.drawPath(heart,p);break;
   case HOME:Path house=new Path();house.moveTo(6,29);house.lineTo(32,8);house.lineTo(58,29);house.moveTo(13,24);house.lineTo(13,55);house.lineTo(51,55);house.lineTo(51,24);house.moveTo(25,55);house.lineTo(25,37);house.lineTo(39,37);house.lineTo(39,55);c.drawPath(house,p);break;
   case SETTINGS:Path gear=new Path();for(int i=0;i<32;i++){double a=2*Math.PI*i/32;float r=i%4==1||i%4==2?27:22,x=32+r*(float)Math.cos(a),y=32+r*(float)Math.sin(a);if(i==0)gear.moveTo(x,y);else gear.lineTo(x,y);}gear.close();c.drawPath(gear,p);c.drawCircle(32,32,9,p);break;
  }c.restore();
 }
 @Override public void setAlpha(int a){opacity=a;invalidateSelf();}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}@Override public int getIntrinsicWidth(){return 64;}@Override public int getIntrinsicHeight(){return 64;}
}
