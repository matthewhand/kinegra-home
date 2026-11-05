package tv.metaxy.dashboard;

/**
 * Simulated TV geometry, font scaling, header stacking and spoken labels.
 * Pure JVM: no claim of real Android View measurement or on-TV acceptance.
 */
public final class test_dashboard_responsive_layout_policy {
    private static int checks;
    private static void yes(boolean valid) {
        checks++;
        if (!valid) throw new AssertionError("responsive rule check " + checks);
    }
    private static void eq(int expected, int actual) {
        checks++;
        if (expected != actual)
            throw new AssertionError("responsive check " + checks + " expected "
                    + expected + " actual " + actual);
    }
    public static void main(String[] unused) {
        int[] widths={-1,0,320,480,600,720,779,780,960,1280,1920,8192,99999};
        float[] scales={0f,0.7f,1f,1.1f,1.3f,1.5f,2f,2.5f,5f,
                Float.NaN,Float.POSITIVE_INFINITY};
        for (int width : widths) {
            for (float scale : scales) {
                int row3=DashboardPresentationPolicy.rowWidthDp(width,30,3,scale);
                int row2=DashboardPresentationPolicy.rowWidthDp(width,30,2,scale);
                yes(row3>=row2 && row2>=0);
                int usable=(width>0?Math.min(width,8192):960)-60;
                yes(row3>=usable && row2>=usable);
                yes(row3<=8192 && row2<=8192);
                if (width>0 && width<780)
                    yes(DashboardPresentationPolicy.stackHeader(width,scale));
            }
        }
        eq(900,DashboardPresentationPolicy.rowWidthDp(960,30,3,1f));
        eq(744,DashboardPresentationPolicy.rowWidthDp(480,30,3,1f));
        eq(1032,DashboardPresentationPolicy.rowWidthDp(480,30,3,2.5f));
        eq(496,DashboardPresentationPolicy.rowWidthDp(480,30,2,1f));
        eq(688,DashboardPresentationPolicy.rowWidthDp(480,30,2,2.5f));
        yes(!DashboardPresentationPolicy.stackHeader(960,1f));
        yes(DashboardPresentationPolicy.stackHeader(960,2f));
        yes(DashboardPresentationPolicy.stackHeader(600,1f));
        yes(!DashboardPresentationPolicy.stackHeader(780,1.3f));
        yes(DashboardPresentationPolicy.stackHeader(780,1.31f));
        yes(!DashboardPresentationPolicy.stackHeader(-1,1f));
        // Wide/normal TV keeps date and time; narrower and 720p-ish
        // landscape widths retain the date until fonts become very large.
        yes(DashboardPresentationPolicy.clockFormat12Hour(960,1f)
                .equals("EEE d MMM  h:mm a"));
        yes(DashboardPresentationPolicy.clockFormat24Hour(960,1f)
                .equals("EEE d MMM  HH:mm"));
        yes(DashboardPresentationPolicy.clockFormat12Hour(720,1f)
                .equals("d MMM h:mm a"));
        yes(DashboardPresentationPolicy.clockFormat24Hour(720,1f)
                .equals("d MMM HH:mm"));
        yes(DashboardPresentationPolicy.clockFormat12Hour(320,1f)
                .equals("h:mm a"));
        yes(DashboardPresentationPolicy.clockFormat24Hour(320,1f)
                .equals("HH:mm"));
        yes(DashboardPresentationPolicy.clockFormat12Hour(960,2.5f)
                .equals("h:mm a"));
        yes(DashboardPresentationPolicy.clockFormat24Hour(1920,2.5f)
                .equals("HH:mm"));
        yes(DashboardPresentationPolicy.clockFormat12Hour(780,1.3f)
                .equals("EEE d MMM  h:mm a"));
        yes(DashboardPresentationPolicy.clockFormat12Hour(780,1.31f)
                .equals("d MMM h:mm a"));
        for (int width : widths) {
            for (float scale : scales) {
                String h12=DashboardPresentationPolicy.clockFormat12Hour(width,scale);
                String h24=DashboardPresentationPolicy.clockFormat24Hour(width,scale);
                yes(h12.length()<=20 && h24.length()<=19);
                yes(h12.contains("mm") && h24.contains("mm"));
                yes(h12.contains("a") && h24.contains("HH"));
            }
        }
        boolean invalid=false;
        try { DashboardPresentationPolicy.rowWidthDp(480,30,4,1f); }
        catch(IllegalArgumentException expected){invalid=true;}
        yes(invalid);
        invalid=false;
        try { DashboardPresentationPolicy.rowWidthDp(480,-1,3,1f); }
        catch(IllegalArgumentException expected){invalid=true;}
        yes(invalid);
        yes(DashboardPresentationPolicy.accessibilityLabel("Plex",true,true)
                .equals("Plex, selected, press OK to open"));
        yes(DashboardPresentationPolicy.accessibilityLabel("Plex",true,false)
                .equals("Plex, installed, press OK to open"));
        yes(DashboardPresentationPolicy.accessibilityLabel("Plex",false,true)
                .equals("Plex, unavailable, not installed or cannot be opened"));
        invalid=false;
        try { DashboardPresentationPolicy.accessibilityLabel(" ",true,false); }
        catch(IllegalArgumentException expected){invalid=true;}
        yes(invalid);
        System.out.println("RESPONSIVE_LAYOUT_POLICY_PASS assertions="+checks);
    }
}
