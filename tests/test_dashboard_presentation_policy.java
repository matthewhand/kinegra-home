package tv.metaxy.dashboard;

/** JVM-only checks of the same presentation rules used by DashboardActivity. */
public final class test_dashboard_presentation_policy {
    private static int checks;
    private static void eq(int want,int got) {
        checks++;
        if (want!=got) throw new AssertionError("height check "+checks+" "+want+" != "+got);
    }
    private static void yes(boolean pass) {
        checks++;
        if (!pass) throw new AssertionError("check "+checks+" false");
    }
    private static void status(boolean enabled,boolean focused,String expected) {
        yes(expected.equals(DashboardPresentationPolicy.status(enabled,focused)));
    }
    public static void main(String[] args) {
        eq(116,DashboardPresentationPolicy.minTileHeightDp(116,1f));
        eq(85,DashboardPresentationPolicy.minTileHeightDp(85,1f));
        eq(116,DashboardPresentationPolicy.minTileHeightDp(116,0.65f));
        eq(151,DashboardPresentationPolicy.minTileHeightDp(116,1.3f));
        eq(170,DashboardPresentationPolicy.minTileHeightDp(85,2f));
        eq(290,DashboardPresentationPolicy.minTileHeightDp(116,3f));
        eq(116,DashboardPresentationPolicy.minTileHeightDp(116,Float.NaN));
        eq(116,DashboardPresentationPolicy.minTileHeightDp(116,Float.POSITIVE_INFINITY));
        for (int base=48;base<=180;base++) {
            int previous=0;
            for (int tenths=1;tenths<=30;tenths++) {
                int actual=DashboardPresentationPolicy.minTileHeightDp(base,tenths/10f);
                yes(actual>=base && actual>=previous && actual<=base*2.5f+1);
                previous=actual;
            }
        }
        status(true,true,"SELECTED \u2022 Press OK to open");
        status(true,false,"Installed \u2022 Ready to open");
        status(false,true,"Unavailable / not installed or cannot open");
        status(false,false,"Unavailable / not installed or cannot open");
        boolean threw=false;
        try { DashboardPresentationPolicy.minTileHeightDp(0,1f); }
        catch (IllegalArgumentException expected) { threw=true; }
        yes(threw);
        threw=false;
        try { DashboardPresentationPolicy.minTileHeightDp(2048,1f); }
        catch (IllegalArgumentException expected) { threw=true; }
        yes(threw);
        System.out.println("DASHBOARD_PRESENTATION_POLICY_PASS assertions="+checks);
    }
}
