#!/usr/bin/env python3
"""Start the owner-authorized, app-UID-restricted native task helper over ADB.

No credentials, private address defaults, settings changes or app data access.
Restart after device reboot, app force-stop or an app update.
"""
import argparse,hashlib,re,shlex,subprocess,time
from pathlib import Path

def verify_checksum_output(output, expected):
    """Reject missing or malformed device checksum output before helper launch."""
    parts=output.split()
    if not parts or not re.fullmatch(r'[0-9a-fA-F]{64}',parts[0]):
        raise SystemExit('Device helper checksum unavailable')
    if parts[0].lower()!=expected:
        raise SystemExit('Device helper checksum mismatch')

def main():
    p=argparse.ArgumentParser();p.add_argument('--serial',required=True);args=p.parse_args()
    root=Path(__file__).resolve().parents[1];jar=root/'build/kinegrarchy-window-broker.jar'
    if not jar.is_file():raise SystemExit('Build the window helper first: bash tools/build_window_broker.sh')
    def adb(*values,check=True):
        r=subprocess.run(['adb','-s',args.serial,*map(str,values)],capture_output=True,text=True,timeout=25)
        if check and r.returncode:raise SystemExit('ADB operation failed; no helper started')
        return r.stdout.strip()
    if 'SHIELD' not in adb('shell','getprop','ro.product.model').upper():raise SystemExit('This starter currently expects a verified NVIDIA Shield')
    for key in ('enable_freeform_support','force_resizable_activities'):
        if adb('shell','settings','get','global',key)!='1':raise SystemExit('Complete developer setup first: '+key+' must read 1')
    package=adb('shell','dumpsys','package','tv.metaxy.proof')
    uid=re.search(r'\buserId=(\d+)\b',package);version=re.search(r'\bversionCode=(\d+)\b',package)
    if not uid or int(uid.group(1))<10000 or not version or int(version.group(1))<34:raise SystemExit('Install Kinegra Home 0.8.10 or newer first')
    # Stop only an earlier instance of this exact, shell-owned helper.
    for pid in adb('shell','pidof','KinegrarchyWindowBroker',check=False).split():
        if not pid.isdigit():raise SystemExit('Unexpected helper PID')
        status=adb('shell','cat','/proc/'+pid+'/status')
        cmd=adb('shell','cat','/proc/'+pid+'/cmdline')
        if not re.search(r'^Uid:\s+2000\b',status,re.M) or 'KinegrarchyWindowBroker' not in cmd:raise SystemExit('Refusing to stop an unrelated process')
        adb('shell','kill',pid)
    remote='/data/local/tmp/kinegrarchy-window-broker.jar'
    adb('push',jar,remote);adb('shell','chmod','600',remote)
    expected=hashlib.sha256(jar.read_bytes()).hexdigest()
    verify_checksum_output(adb('shell','sha256sum',remote),expected)
    command='CLASSPATH='+remote+' nohup app_process /system/bin --nice-name=KinegrarchyWindowBroker KinegrarchyWindowBroker '+uid.group(1)+' >/data/local/tmp/kinegrarchy-window-broker.log 2>&1 </dev/null &'
    adb('shell','sh','-c',shlex.quote(command))
    for _ in range(30):
        output=adb('shell','cat','/data/local/tmp/kinegrarchy-window-broker.log',check=False)
        if 'KINEGRARCHY_WINDOW_HELPER_READY' in output:
            print('WINDOW_HELPER_READY: app-UID restricted; no network listener; restart after reboot or force-stop');return
        time.sleep(.3)
    print(output[-5000:]);raise SystemExit('Window helper did not attach; no success claimed')
if __name__=='__main__':main()
