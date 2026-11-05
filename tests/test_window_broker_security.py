"""Boundary checks for the explicit ADB window helper; JVM tests cover policy behavior."""
from pathlib import Path
import unittest
import importlib.util
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
class WindowBrokerSecurity(unittest.TestCase):
    def test_app_remains_permission_free(self):
        root = ET.parse(ROOT / 'harness/AndroidManifest.xml').getroot()
        self.assertEqual([n.get('{http://schemas.android.com/apk/res/android}name') for n in root.findall('uses-permission')], ['android.permission.INTERNET'])
        self.assertEqual(len(root.find('application').findall('provider')), 1)

    def test_only_shell_can_attach_helper(self):
        text = (ROOT / 'android/dashboard/WindowBrokerProvider.java').read_text()
        self.assertIn('Binder.getCallingUid()!=2000', text.replace(' ', ''))
        self.assertIn('Process.myUid()', text)
        self.assertIn('linkToDeath', text)

    def test_each_transaction_checks_owner_before_identity_change(self):
        text = (ROOT / 'tools/windowbroker/KinegrarchyWindowBroker.java').read_text()
        self.assertLess(text.index('BrokerPolicy.ownerAllowed'), text.index('Binder.clearCallingIdentity'))
        self.assertIn('Binder.restoreCallingIdentity', text)
        self.assertIn('p.dataAvail()!=0', text)
        self.assertIn('default:throw new IllegalArgumentException', text)
        for forbidden in ('Runtime.getRuntime', 'ProcessBuilder', 'ServerSocket', 'DatagramSocket', 'WebView'):
            self.assertNotIn(forbidden, text)

    def test_starter_has_no_device_address_default(self):
        text = (ROOT / 'tools/start_window_broker.py').read_text()
        self.assertIn("p.add_argument('--serial',required=True)", text)
        self.assertNotIn('10.0.0.', text)
        self.assertIn('Device helper checksum mismatch', text)

    def test_checksum_fails_closed_on_empty_or_invalid_output(self):
        path = ROOT / 'tools/start_window_broker.py'
        spec = importlib.util.spec_from_file_location('offline_window_starter', path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        expected = 'a' * 64
        for output in ('', '  \n', 'sha256sum: unavailable', 'abc  /device/helper.jar'):
            with self.subTest(output=output), self.assertRaisesRegex(SystemExit, 'unavailable'):
                module.verify_checksum_output(output, expected)
        with self.assertRaisesRegex(SystemExit, 'mismatch'):
            module.verify_checksum_output('b'*64 + '  /device/helper.jar', expected)
        module.verify_checksum_output(expected+'  /device/helper.jar', expected)

    def test_back_requires_non_null_focused_stack(self):
        text = (ROOT / 'tools/windowbroker/KinegrarchyWindowBroker.java').read_text()
        method = text.split('private void back(int key)', 1)[1].split('@Override protected', 1)[0]
        self.assertIn('if(info==null)throw new SecurityException("Selected app does not have focus")', method)
        self.assertLess(method.index('if(info==null)'), method.index('info.getClass()'))

    def test_recents_require_same_strict_broker_package_validation(self):
        text = (ROOT / 'android/dashboard/WindowBrokerProvider.java').read_text()
        section = text.split('if("seed_recents".equals(method))', 1)[1].split('if("status".equals(method))',1)[0]
        self.assertIn('BrokerPolicy.packageAllowed(pkg)',section)
        self.assertNotIn('HomeMenuPolicy.valid(pkg)',section)
        policy = (ROOT / 'android/dashboard/BrokerPolicy.java').read_text()
        self.assertIn('value.length()<200',policy)
        self.assertIn('[A-Za-z][A-Za-z0-9_]',policy)

    def test_shortcut_service_does_not_retrieve_screens(self):
        ns = '{http://schemas.android.com/apk/res/android}'
        root = ET.parse(ROOT / 'harness/res/xml/session_shortcuts.xml').getroot()
        self.assertEqual(root.get(ns + 'canRetrieveWindowContent'), 'false')
        self.assertEqual(root.get(ns + 'accessibilityEventTypes'), 'typeWindowStateChanged')
        text = (ROOT / 'android/dashboard/SessionShortcutService.java').read_text()
        self.assertIn('!a.sessionVisible()', text)
        self.assertIn('onAccessibilityEvent(AccessibilityEvent e){}', text)
        self.assertNotIn('getRootInActiveWindow', text)
        self.assertNotIn('Log.', text)

    def test_viewport_ownership_and_back_only_input(self):
        text = (ROOT / 'tools/windowbroker/KinegrarchyWindowBroker.java').read_text()
        self.assertIn('validateHost', text)
        self.assertIn('Host must be the Kinegra canvas', text)
        self.assertIn('Viewports exceed native input space', text)
        self.assertIn('Selected app does not have focus', text)
        self.assertIn('KeyEvent.KEYCODE_BACK', text)
        self.assertNotIn('data.readParcelable', text)

if __name__ == '__main__': unittest.main()
