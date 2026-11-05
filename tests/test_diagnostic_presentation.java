package tv.metaxy.dashboard;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Locale;
public final class test_diagnostic_presentation {
    private static void check(boolean v, String s){if(!v)throw new AssertionError(s);System.out.println("PASS "+s);}
    public static void main(String[] args) {
        String info=DiagnosticPresentation.summary("0.6.4", "NVIDIA", "SHIELD Android TV", "darcy", "darcy", "11", 30);
        check(info.contains("0.6.4") && info.contains("darcy") && info.contains("revision: Not reported"),"version and honest hardware description");
        check(DiagnosticPresentation.summary(null,null,null,null,null,null,0).contains("Unavailable"),"missing metadata");
        check(DiagnosticPresentation.input("Pad\nname",0,0,false,true,true).equals("Gamepad-capable input: Pad name"),"sanitize controller name");
        check(DiagnosticPresentation.input("Remote",0,0,false,false,true).startsWith("D-pad input (remote/keypad)"),"remote without unsupported claims");
        check(DiagnosticPresentation.input("Keyboard",0,0,false,false,false).isEmpty(),"unrelated device omitted");
        check(DiagnosticPresentation.inputs(Collections.emptyList(),Collections.emptyList()).contains("A physical remote may still be connected"),"empty device state");
        check(DiagnosticPresentation.inputs(Arrays.asList("Gamepad-capable input: Example"),Collections.emptyList()).contains("Example"),"connected device state");
        String[] softwareNames = {"virtual-search", "shield-ask-remote", "virtual-remote", "IPControl_UPnP_RemoteService"};
        for (String name : softwareNames) {
            check(DiagnosticPresentation.input(name,0,0,false,true,true).startsWith("Software/virtual input:"),
                    "software gamepad flags do not imply a physical controller: " + name);
            check(DiagnosticPresentation.softwareInput("  " + name.toUpperCase(Locale.ROOT) + "  ",false),
                    "case and outer whitespace: " + name);
        }
        check(DiagnosticPresentation.input("Unfamiliar service",0,0,true,true,true).startsWith("Software/virtual input:"),
                "Android virtual flag overrides generic gamepad capability");
        check(DiagnosticPresentation.input("LCTECH LCKEY",0,0,false,true,true).startsWith("Programmable keypad:"),
                "known macropad name overrides controller source flags");
        check(DiagnosticPresentation.input("Renamed keypad",0x1189,0x8890,false,true,true).startsWith("Programmable keypad:"),
                "known macropad hardware IDs survive a name change");
        check(DiagnosticPresentation.input(" lctech   lckey ",0,0,false,true,false).startsWith("Programmable keypad:"),
                "macropad name whitespace and case");
        check(DiagnosticPresentation.input("ASUS Gamepad",0x0b05,0x4500,false,true,true).startsWith("Gamepad-capable input:"),
                "ASUS gamepad capability retained without claiming physical acceptance");
        check(DiagnosticPresentation.input("NVIDIA Controller",0x0955,0x7214,false,true,true).startsWith("Gamepad-capable input:"),
                "NVIDIA gamepad capability retained");
        check(DiagnosticPresentation.input("NVIDIA Remote",0x0955,0x7218,false,false,true).startsWith("D-pad input (remote/keypad):"),
                "remote is distinguished from gamepad");
        check(!DiagnosticPresentation.softwareInput("Virtual Pro Controller",false),
                "unfamiliar names are not rejected by a broad substring heuristic");
        check(DiagnosticPresentation.input(null,0,0,false,true,false).endsWith("Unavailable"),
                "missing input name is safe");
        ArrayList<String> logical = new ArrayList<>();
        for (int i=0;i<12;i++) logical.add("Software/virtual input: service " + i);
        String mixed = DiagnosticPresentation.inputs(Arrays.asList("Gamepad-capable input: physical candidate"),logical);
        check(mixed.indexOf("physical candidate") < mixed.indexOf("service 0"),
                "software entries do not displace controller candidates");
        check(mixed.contains("Additional reported inputs not shown") && !mixed.contains("service 7"),
                "large device list is bounded with an honest truncation notice");
        String onlySoftware = DiagnosticPresentation.inputs(Collections.emptyList(),Arrays.asList("Software/virtual input: virtual-remote"));
        check(!onlySoftware.contains("none detected") && onlySoftware.contains("Capabilities do not confirm physical device identity"),
                "software-only observation does not assert physical absence");
        check(DiagnosticPresentation.inputs(null,null).contains("may still be connected"),
                "unavailable enumeration does not assert physical absence");
        Boolean[] flags = {null, Boolean.FALSE, Boolean.TRUE};
        for (Boolean multi : flags) for (Boolean split : flags) {
            String status = DiagnosticPresentation.splitScreenStatus(multi, split);
            boolean disabled = Boolean.FALSE.equals(multi) || Boolean.FALSE.equals(split);
            check(status.equals(disabled
                    ? "Classic split-screen: disabled by this device's firmware"
                    : "Classic split-screen: framework flags alone do not prove support"),
                    "conservative flags " + multi + "/" + split);
        }
        String unavailable = DiagnosticPresentation.windowCapabilities(true, false, true);
        check(!unavailable.contains("opens one app at a time") && unavailable.contains("titleless two-app preview"), "diagnostics distinguish classic firmware support from the new task compositor");
        check(unavailable.contains("disabled by this device's firmware")
                && unavailable.contains("Picture-in-picture feature: Reported")
                && unavailable.contains("does not establish side-by-side apps"),
                "PiP never establishes split-screen");
        String unknown = DiagnosticPresentation.windowCapabilities(null, null, false);
        check(unknown.contains("Framework split-screen flag: Not reported")
                && unknown.contains("framework flags alone do not prove support"), "missing OS resources stay unknown");
        String enabled = DiagnosticPresentation.windowCapabilities(true, true, true);
        check(enabled.contains("framework flags alone do not prove support")
                && enabled.contains("not a successful two-app test"),
                "enabled flags never promise working app pairs");
        check(DiagnosticPresentation.firmware("build\n12").equals("Firmware build: build 12"),
                "firmware formatting is bounded and sanitized");

    }
}
