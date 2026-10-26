"""Small ADB helper for repeatable UI checks, using visible accessibility labels.

Usage: python scripts/android_ui.py --serial emulator-5554 tap 'Map'
       python scripts/android_ui.py --serial emulator-5554 screenshot route-emulator
"""
import argparse
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ADB = Path.home() / 'AppData/Local/Android/Sdk/platform-tools/adb.exe'

def command(serial, *args):
    return subprocess.check_output([str(ADB), '-s', serial, *args])

def dump(serial):
    for attempt in range(4):
        result = command(serial, 'shell', 'uiautomator', 'dump', '/sdcard/virasaya-window.xml')
        if b'dumped to' in result:
            break
        time.sleep(.5)
    else:
        raise RuntimeError('UI hierarchy was unavailable; refusing to use a stale dump')
    xml = command(serial, 'exec-out', 'cat', '/sdcard/virasaya-window.xml')
    (ROOT/'artifacts/window.xml').write_bytes(xml)
    return ET.fromstring(xml)

def tap(serial, label):
    nodes = [n for n in dump(serial).iter('node') if label in (n.get('text'), n.get('content-desc'))]
    if not nodes:
        raise ValueError(f'Visible control not found: {label}')
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', nodes[-1].get('bounds')))
    command(serial, 'shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))
    print('Tapped:', label)

def screenshot(serial, name):
    target = ROOT/'artifacts'/f'{name}.png'
    target.write_bytes(command(serial, 'exec-out', 'screencap', '-p'))
    print(target)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--serial', required=True)
    parser.add_argument('action', choices=['tap','screenshot','dump'])
    parser.add_argument('value', nargs='?')
    args = parser.parse_args()
    if args.action == 'tap': tap(args.serial, args.value)
    elif args.action == 'screenshot': screenshot(args.serial, args.value)
    else:
        for node in dump(args.serial).iter('node'):
            text = node.get('text') or node.get('content-desc')
            if text: print(ascii(text), node.get('bounds'))
