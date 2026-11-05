package tv.metaxy.dream;
public final class test_dream_cadence {
  static int n=0;
  static void assertOk(boolean x){if(!x)throw new AssertionError("case "+n);n++;}
  public static void main(String[] args){
     assertOk(DreamCadence.slide(-1)==0);
     assertOk(DreamCadence.slide(0)==0);
     assertOk(DreamCadence.slide(19999)==0);
     assertOk(DreamCadence.slide(20000)==1);
     assertOk(DreamCadence.slide(40000)==2);
     assertOk(DreamCadence.slide(60000)==3);
     assertOk(DreamCadence.slide(80000)==0);
     assertOk(DreamCadence.slide(Long.MAX_VALUE)>=0);
     assertOk(DreamCadence.frame(-11)==0);
     assertOk(DreamCadence.frame(124)==0);
     assertOk(DreamCadence.frame(125)==1);
     assertOk(DreamCadence.columns(3440)==98);
     assertOk(DreamCadence.rows(1440)==24);
     assertOk(DreamCadence.columns(1)==36);
     assertOk(DreamCadence.columns(20000)==116);
     assertOk(DreamCadence.rows(1)==12);
     assertOk(DreamCadence.rows(20000)==36);
     assertOk(DreamCadence.clamp(300,5,20)==20);
     assertOk(DreamCadence.clamp(-9,5,20)==5);
     assertOk(DreamCadence.glyph(42,1,2)==DreamCadence.glyph(42,1,2));
     assertOk(DreamCadence.glyph(42,1,2)!=DreamCadence.glyph(47,1,2));
     for(int k=0;k<800;k++){
         int dx=DreamCadence.driftX(k*125L),dy=DreamCadence.driftY(k*125L);
         assertOk(dx>=-12&&dx<=12&&dy>=-8&&dy<=8);
     }
     for(int i=0;i<4;i++) {
         assertOk(!DreamCadence.label(i).isEmpty());
         assertOk(!DreamCadence.lowerText(i).isEmpty());
     }
     System.out.println("DREAM_CADENCE_PURE_TESTS_PASS "+n);
  }
}
