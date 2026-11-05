"""Run the production reload callback with deterministic broker/UI boundaries."""
from pathlib import Path
import unittest, os, subprocess, tempfile
ROOT = Path(os.environ.get("KINEGRA_TEST_SOURCE_ROOT", Path(__file__).resolve().parents[1]))
class ReloadBehavior(unittest.TestCase):
 @classmethod
 def setUpClass(cls):
  cls.temp = tempfile.TemporaryDirectory()
  cls.folder = Path(cls.temp.name)
  source = (ROOT / "android/dashboard/PairCanvasActivity.java").read_text()
  start = source.index("private void reloadPane(")
  end = source.index("private Button add(", start)
  method = source[start:end]
  harness = r'''package tv.metaxy.dashboard;
public class ReloadHarness {
 boolean visible=true,ending=false,started=true;int generation=1,reloadingPanes,failedPanes;
 String lastAction;RecoveryNoticePolicy recoveryNotice=new RecoveryNoticePolicy();
 static class Controls {void dismiss(){}} Controls controls=new Controls();
 static class Handler {void post(Runnable r){r.run();}} Handler main=new Handler();
 static class WindowBrokerClient {static class Result {boolean ok;int failedPanes;String message;Result(boolean o,int f,String m){ok=o;failedPanes=f;message=m;}}}
 static class CanvasClient {static final int RELOAD=1;static WindowBrokerClient.Result next;static WindowBrokerClient.Result call(int c,Object a,Object b,int side,int x,int y){return next;}}
 void enqueue(Runnable r){r.run();}void showWarning(String message,boolean recoverable){}void hideWarning(){}void logEvent(String event){}void updateReloadButtons(){}void focus(int side){}String paneRole(int side){return side==0?"left":"right";}
 __PRODUCTION_METHOD__
 public static void main(String[] args){
  boolean failed=args[0].equals("failed");
  for(int side=0;side<2;side++){
   ReloadHarness h=new ReloadHarness();int remaining=1<<(1-side);
   h.failedPanes=3;h.recoveryNotice.observe(3,"Original failure");
   CanvasClient.next=new WindowBrokerClient.Result(!failed,failed?0:remaining,failed?"Reload failed":"Ready");
   h.reloadPane(side);
   int expected=failed?(1<<side):remaining;
   if(h.failedPanes!=expected||h.reloadingPanes!=0)throw new AssertionError("Reload result not applied");
   h.recoveryNotice.dismiss();
   for(int poll=0;poll<20;poll++)if(h.recoveryNotice.observe(expected,"Health poll"))throw new AssertionError("Dismissed reload warning returned");
   if(!h.recoveryNotice.observe(expected==1?2:1,"Different pane failed"))throw new AssertionError("New failure was suppressed");
  }
 }
}'''.replace("__PRODUCTION_METHOD__", method)
  (cls.folder / "ReloadHarness.java").write_text(harness)
  subprocess.run(["javac", "-d", str(cls.folder), str(ROOT / "android/dashboard/RecoveryNoticePolicy.java"), str(cls.folder / "ReloadHarness.java")], check=True, capture_output=True)
 @classmethod
 def tearDownClass(cls): cls.temp.cleanup()
 def run_case(self, case):
  result = subprocess.run(["java", "-cp", str(self.folder), "tv.metaxy.dashboard.ReloadHarness", case], capture_output=True, text=True)
  self.assertEqual(0, result.returncode, result.stderr)
 def test_partial_success_dismiss_survives_health_polls(self): self.run_case("remaining")
 def test_failed_reload_dismiss_survives_health_polls(self): self.run_case("failed")
if __name__ == "__main__": unittest.main()
