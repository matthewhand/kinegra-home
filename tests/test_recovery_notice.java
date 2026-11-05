package tv.metaxy.dashboard;
class test_recovery_notice {
 static int checks;static void ck(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
 public static void main(String[] args){RecoveryNoticePolicy p=new RecoveryNoticePolicy();ck(p.observe(1,"Closed"));p.dismiss();for(int n=0;n<100;n++)ck(!p.observe(1,"Closed"));ck(!p.observe(1,"App closed; relaunch pair"));ck(p.observe(3,"Closed"));p.dismiss();ck(!p.observe(3,"Closed"));ck(p.observe(0,"Helper disconnected"));p.dismiss();p.resolved();ck(p.observe(0,"Helper disconnected"));p.dismiss();ck(p.observe(2,null));for(int mask=1;mask<=3;mask++){p.resolved();ck(p.observe(mask,"Reload left another failure"));p.dismiss();for(int i=0;i<20;i++)ck(!p.observe(mask,"Health poll"));p.resolved();ck(p.observe(mask,"Reload failed"));p.dismiss();ck(!p.observe(mask,"Next health poll"));}System.out.println("RECOVERY_NOTICE_CHECKS="+checks);}
}
