#!/usr/bin/env bash
# Isolated next-dashboard build. No signing, secrets, device or network.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
out="$root/build/Kinegrarchy-v0.9.23-code59-UNSIGNED.apk"
mkdir -p "$root/build"
test ! -e "$out" || { echo 'Refusing to overwrite candidate' >&2; exit 1; }
task_dir=$(mktemp -d "$root/build/next-dashboard-XXXXXXXX")
trap 'rm -rf "$task_dir"' EXIT
source_sha=$(git -C "$root" rev-parse HEAD)
test -z "$(git -C "$root" status --porcelain)" || { echo 'Clean committed source required' >&2; exit 1; }
git -C "$root" archive HEAD | tar -xf - -C "$task_dir"
python3 - "$task_dir/tools/build_shieldarchy_dashboard_v6_offline.sh" <<'PY'
from pathlib import Path
import sys
p=Path(sys.argv[1]); s=p.read_text()
a='--version-code 6 --version-name 0.6.0-controller-mvp'
b="versionCode='6' versionName='0.6.0-controller-mvp'"
assert s.count(a)==1 and s.count(b)==1, 'Unknown build template'
s=s.replace(a,'--version-code 59 --version-name 0.9.23-beta-preview').replace(b,"versionCode='59' versionName='0.9.23-beta-preview'")
p.write_text(s)
PY
bash "$task_dir/tools/build_shieldarchy_dashboard_v6_offline.sh"
candidate="$task_dir/build/Shieldarchy-Dashboard-v0.6-rebuilt-UNSIGNED.apk"
test -s "$candidate"
# Exclusive creation; preserve every existing candidate even on a race.
(umask 077; set -o noclobber; cat "$candidate" > "$out")
printf 'SOURCE_REVISION=%s\n' "$source_sha"
sha256sum "$out"
