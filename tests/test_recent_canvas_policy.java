import tv.metaxy.dashboard.RecentCanvasPolicy;
class test_recent_canvas_policy {
 static void check(boolean ok){if(!ok)throw new AssertionError();}
 public static void main(String[] args){
  String p="tv.metaxy.proof",c="tv.metaxy.proof.PairCanvasActivity";
  check(RecentCanvasPolicy.action(12,12,p,c)==RecentCanvasPolicy.HIDE_CURRENT);
  check(RecentCanvasPolicy.action(11,12,p,c)==RecentCanvasPolicy.REMOVE_STALE);
  check(RecentCanvasPolicy.action(11,-1,p,c)==RecentCanvasPolicy.REMOVE_STALE);
  check(RecentCanvasPolicy.action(0,-1,p,c)==RecentCanvasPolicy.KEEP);
  check(RecentCanvasPolicy.action(11,12,"org.local.tegramilkdrop",c)==RecentCanvasPolicy.KEEP);
  check(RecentCanvasPolicy.action(11,12,p,"tv.metaxy.proof.DashboardActivity")==RecentCanvasPolicy.KEEP);
  check(RecentCanvasPolicy.action(11,12,null,null)==RecentCanvasPolicy.KEEP);
  System.out.println("RECENT_CANVAS_POLICY_PASS=7");
 }
}
