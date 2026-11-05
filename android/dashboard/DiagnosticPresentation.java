package tv.metaxy.dashboard;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/** Compact, unprivileged, read-only About screen formatting. */
public final class DiagnosticPresentation {
    private DiagnosticPresentation() { }
    private static String safe(String value) {
        if (value == null) return "Unavailable";
        String result = value.replaceAll("[\\p{Cntrl}]", " ").trim();
        if (result.isEmpty()) return "Unavailable";
        return result.length() > 80 ? result.substring(0, 80) : result;
    }
    public static String summary(String version, String manufacturer, String model,
                                 String device, String hardware, String android, int sdk) {
        return "App version: " + safe(version) + "\nDevice: " + safe(manufacturer) + " " + safe(model)
            + "\nDevice code: " + safe(device) + "\nHardware platform: " + safe(hardware)
            + "\nHardware revision: Not reported\nAndroid: " + safe(android) + " (API " + sdk + ")";
    }
    private static String flag(Boolean value) {
        return value == null ? "Not reported" : value ? "Enabled" : "Disabled";
    }

    /** OS flags are diagnostic evidence, never proof that this app can tile apps. */
    public static String splitScreenStatus(Boolean multiWindow, Boolean splitScreen) {
        if (Boolean.FALSE.equals(multiWindow) || Boolean.FALSE.equals(splitScreen))
            return "Classic split-screen: disabled by this device's firmware";
        return "Classic split-screen: framework flags alone do not prove support";
    }

    public static String windowCapabilities(Boolean multiWindow, Boolean splitScreen,
                                             boolean pictureInPicture) {
        return splitScreenStatus(multiWindow, splitScreen)
            + "\nA titleless two-app preview is available with owner ADB setup."
            + "\n\nFramework multi-window flag: " + flag(multiWindow)
            + "\nFramework split-screen flag: " + flag(splitScreen)
            + "\nPicture-in-picture feature: " + (pictureInPicture ? "Reported" : "Not reported")
            + "\n\nPicture-in-picture needs app support and does not establish side-by-side apps."
            + "\nEnabled flags are not a successful two-app test."
            + "\nNo system settings are changed by these checks.";
    }

    public static String firmware(String incremental) {
        return "Firmware build: " + safe(incremental);
    }

    private static String inputName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Android sometimes marks these software input services as non-virtual. */
    public static boolean softwareInput(String name, boolean virtual) {
        if (virtual) return true;
        String value = inputName(name);
        return value.equals("virtual-search") || value.equals("shield-ask-remote")
                || value.equals("virtual-remote") || value.equals("ipcontrol_upnp_remoteservice");
    }

    public static String input(String name, int vendorId, int productId, boolean virtual,
                               boolean gamepad, boolean dpad) {
        if (softwareInput(name, virtual)) return "Software/virtual input: " + safe(name);
        if ((vendorId == 0x1189 && productId == 0x8890) || inputName(name).equals("lctech lckey"))
            return "Programmable keypad: " + safe(name);
        if (gamepad) return "Gamepad-capable input: " + safe(name);
        if (dpad) return "D-pad input (remote/keypad): " + safe(name);
        return "";
    }

    /** Show controller/remote-capable inputs before software entries, with bounded output. */
    public static String inputs(List<String> candidates, List<String> software) {
        ArrayList<String> values = new ArrayList<>();
        if (candidates != null) for (String value : candidates)
            if (value != null && !value.isEmpty() && !values.contains(value)) values.add(value);
        if (software != null) for (String value : software)
            if (value != null && !value.isEmpty() && !values.contains(value)) values.add(value);
        if (values.isEmpty()) return "Android reports no gamepad or D-pad inputs."
                + "\nA physical remote may still be connected.";
        boolean more = values.size() > 8;
        return "Android-reported input capabilities:\n"
                + String.join("\n", values.subList(0, Math.min(values.size(), 8)))
                + (more ? "\nAdditional reported inputs not shown." : "")
                + "\nCapabilities do not confirm physical device identity.";
    }
}
