package tv.metaxy.dashboard;
class test_pane_recovery {
 public static void main(String[] args){int checks=0;
  for(boolean pip:new boolean[]{false,true})for(boolean swapped:new boolean[]{false,true})for(int side=0;side<2;side++){
   String role=PaneRecoveryPolicy.role(side,pip,swapped);boolean first=side==(swapped?1:0);
   String expected=pip?(first?"main":"PiP"):(first?"left":"right");if(!expected.equals(role))throw new AssertionError(role);checks++;
  }
  for(int mask=0;mask<4;mask++)for(int side=0;side<2;side++){boolean expected=mask==3||(mask==1&&side==0)||(mask==2&&side==1);if(PaneRecoveryPolicy.failed(mask,side)!=expected)throw new AssertionError("mask");checks++;}
  for(boolean swapped:new boolean[]{false,true})for(int mask=0;mask<4;mask++){int target=PaneRecoveryPolicy.focusAfterDismiss(mask,swapped);if(mask==3?target!=2:PaneRecoveryPolicy.failed(mask,target))throw new AssertionError("dismiss focus");checks++;}
  boolean rejected=false;try{PaneRecoveryPolicy.role(2,false,false);}catch(IllegalArgumentException e){rejected=true;}if(!rejected)throw new AssertionError("invalid pane");
  System.out.println("PANE_RECOVERY_CHECKS="+(checks+1));
 }
}