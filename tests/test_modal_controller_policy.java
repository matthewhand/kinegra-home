package tv.metaxy.dashboard;
public final class test_modal_controller_policy {
  static void check(boolean ok) { if(!ok) throw new AssertionError(); }
  public static void main(String[] args) {
    ModalControllerPolicy p=new ModalControllerPolicy();
    Object one=new Object(), two=new Object();
    check(p.handle(1,96,0,0,false,one)==1);
    check(p.handle(1,96,0,1,false,one)==1);
    check(p.handle(1,96,1,0,false,one)==2);
    check(p.handle(1,96,1,0,false,one)==1);
    check(p.handle(1,96,0,0,false,one)==1);
    check(p.handle(1,96,1,0,false,two)==1);
    check(p.handle(1,96,0,0,false,one)==1);
    check(p.handle(2,96,1,0,false,one)==1);
    check(p.handle(1,96,1,0,true,one)==1);
    check(p.handle(1,97,0,0,false,one)==1);
    check(p.handle(1,97,1,0,false,one)==3);
    check(p.handle(1,97,1,0,false,one)==1);
    check(p.handle(1,97,0,0,false,one)==1);
    check(p.handle(1,97,1,0,true,one)==1);
    check(p.handle(1,4,0,0,false,one)==0);
    check(p.handle(1,3,0,0,false,one)==0);
    System.out.println("MODAL_CONTROLLER_PASS 16");
  }
}
