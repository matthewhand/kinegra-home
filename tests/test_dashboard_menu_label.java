package tv.metaxy.dashboard;
public final class test_dashboard_menu_label {
    private static void expect(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError(expected+" != "+actual);
    }
    public static void main(String[] args) {
        expect("BROWSE • Apps and About",DashboardPresentationPolicy.menuStatus(false));
        expect("SELECTED • Press OK to browse",DashboardPresentationPolicy.menuStatus(true));
        expect("More apps, browse apps and About",DashboardPresentationPolicy.menuAccessibilityLabel("More apps",false));
        expect("More apps, selected, press OK to browse",DashboardPresentationPolicy.menuAccessibilityLabel("More apps",true));
        expect("Installed • Ready to open",DashboardPresentationPolicy.status(true,false));
        System.out.println("MENU_LABEL_POLICY_PASS");
    }
}
