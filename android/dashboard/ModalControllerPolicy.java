package tv.metaxy.dashboard;
/** Controller A/B for a modal dialog. No input injection or global key handling. */
public final class ModalControllerPolicy {
    public static final int PASS=0, CONSUMED=1, ACTIVATE=2, CANCEL=3;
    public static final int A=96, B=97, DOWN=0, UP=1;
    private int aDevice=-1, bDevice=-1;
    private Object aFocus=null;
    public int handle(int device,int key,int action,int repeat,boolean cancelled,Object focused) {
        if (key!=A && key!=B) return PASS;
        if (action==DOWN) {
            if (repeat==0 && !cancelled) {
                if (key==A && aDevice==-1) { aDevice=device; aFocus=focused; }
                if (key==B && bDevice==-1) bDevice=device;
            }
            return CONSUMED;
        }
        if (action==UP) {
            if (key==A && device==aDevice) {
                boolean activate=!cancelled && focused!=null && focused==aFocus;
                aDevice=-1; aFocus=null;
                return activate ? ACTIVATE : CONSUMED;
            }
            if (key==B && device==bDevice) {
                bDevice=-1;
                return cancelled ? CONSUMED : CANCEL;
            }
        }
        return CONSUMED;
    }
}
