package tv.metaxy.dashboard;
/** App indices remain stable when their visual roles change. */
public final class PaneRecoveryPolicy {
 private PaneRecoveryPolicy(){}
 public static String role(int app,boolean pip,boolean swapped){
  if(app<0||app>1)throw new IllegalArgumentException("Invalid pane");
  boolean first=app==(swapped?1:0);
  return pip?(first?"main":"PiP"):(first?"left":"right");
 }
 public static int focusAfterDismiss(int mask,boolean swapped){int first=swapped?1:0;return !failed(mask,first)?first:!failed(mask,1-first)?1-first:2;}
 public static boolean failed(int mask,int app){return app>=0&&app<2&&(mask&(1<<app))!=0;}
}
