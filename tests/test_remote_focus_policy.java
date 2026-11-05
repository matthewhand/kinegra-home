package tv.metaxy.dashboard;

public final class test_remote_focus_policy {
    private static int checks;
    private static void eq(int expected,int actual) {
        checks++;
        if(expected!=actual) throw new AssertionError("check "+checks+": "+expected+" != "+actual);
    }
    private static boolean[] all() {return new boolean[]{true,true,true,true,true,true,true,true};}
    private static void next(int from, int direction, int wanted, boolean[] enabled) {
        eq(wanted,RemoteFocusPolicy.next(from,direction,enabled));
    }
    public static void main(String[] args) {
        int L=RemoteFocusPolicy.LEFT,R=RemoteFocusPolicy.RIGHT,
            U=RemoteFocusPolicy.UP,D=RemoteFocusPolicy.DOWN;
        boolean[] e=all();
        eq(0,RemoteFocusPolicy.firstEnabled(e));
        for(int i=0;i<8;i++) eq(i,RemoteFocusPolicy.restore(i,e));
        next(0,L,-1,e);next(0,R,1,e);next(1,L,0,e);
        next(1,R,2,e);next(2,R,-1,e);next(3,R,4,e);
        next(4,R,5,e);next(5,R,-1,e);next(6,L,-1,e);
        next(6,R,7,e);next(7,L,6,e);next(7,R,-1,e);
        next(0,U,-1,e);next(0,D,3,e);next(1,D,4,e);
        next(2,D,5,e);next(3,U,0,e);next(4,U,1,e);
        next(5,U,2,e);next(3,D,6,e);next(4,D,7,e);
        next(5,D,7,e);next(6,U,3,e);next(7,U,4,e);
        next(6,D,-1,e);next(7,D,-1,e);
        // Left/right never spill into system tools from the app row.
        next(3,L,-1,e);next(5,R,-1,e);next(6,L,-1,e);
        // Missing middle app skips within same row, not into system settings.
        e[1]=false;
        next(0,R,2,e);next(2,L,0,e);next(4,U,0,e);
        eq(0,RemoteFocusPolicy.restore(1,e));
        // All row-two apps gone: down stays on the current tile.
        e[3]=e[4]=e[5]=false;
        next(0,D,-1,e);next(2,D,-1,e);next(6,U,-1,e);
        // System tools disabled: deliberate DOWN cannot escape app grid.
        e=all();e[6]=e[7]=false;
        next(3,D,-1,e);next(4,D,-1,e);next(5,D,-1,e);
        // Missing one system tool: choose other closest enabled.
        e[7]=true;next(3,D,7,e);next(4,D,7,e);next(5,D,7,e);
        // Missing first app: restore safely to the first available.
        e=all();e[0]=false;
        eq(1,RemoteFocusPolicy.firstEnabled(e));
        eq(1,RemoteFocusPolicy.restore(0,e));
        eq(1,RemoteFocusPolicy.next(0,R,e));
        // Total absence: no focus movement or crash.
        e=new boolean[8];
        eq(-1,RemoteFocusPolicy.firstEnabled(e));
        eq(-1,RemoteFocusPolicy.restore(7,e));
        eq(-1,RemoteFocusPolicy.next(7,D,e));
        // 8 inventory cells strictly enforced.
        boolean threw=false;
        try{RemoteFocusPolicy.firstEnabled(new boolean[6]);}
        catch(IllegalArgumentException ex){threw=true;}
        eq(1,threw?1:0);
        // Fuzz enabled inventories: returned focus always enabled,
        // and never leaves index range; no disabled tile visited.
        for(int mask=0;mask<256;mask++){
            e=new boolean[8];
            for(int k=0;k<8;k++) e[k]=(mask&(1<<k))!=0;
            for(int from=0;from<8;from++){
                for(int d=L;d<=D;d++){
                    int next=RemoteFocusPolicy.next(from,d,e);
                    eq(1, next==-1 || (next>=0 && next<8 && e[next])?1:0);
                }
            }
        }
        System.out.println("REMOTE_FOCUS_POLICY_PASS checks="+checks);
    }
}
