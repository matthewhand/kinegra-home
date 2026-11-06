#!/usr/bin/env bash
# Build an exact clean commit in isolation. No signing, network or device access.
set -euo pipefail
root=$(cd "$(dirname "$0")/.." && pwd)
cd "$root"
test -z "$(git status --porcelain)" || { echo 'Clean committed source required' >&2; exit 1; }
version=$(python3 -c 'import json; print(json.load(open("packaging/COMPONENTS.json"))["version"])')
out="$root/build/KinegraHome-$version-UNSIGNED.apk"
mkdir -p "$root/build"
test ! -e "$out" || { echo 'Refusing to overwrite an existing APK' >&2; exit 1; }
snapshot=$(mktemp -d "$root/build/home-source-XXXXXXXX")
trap 'case "$snapshot" in "$root"/build/home-source-*) rm -rf -- "$snapshot";; esac' EXIT
source_sha=$(git rev-parse HEAD)
git archive HEAD | tar -xf - -C "$snapshot"
python3 "$snapshot/tools/check_source.py"
bash "$snapshot/tools/build_android.sh"
candidate="$snapshot/build/KinegraHome-$version-UNSIGNED.apk"
test -s "$candidate"
(umask 077; set -o noclobber; cat "$candidate" > "$out")
printf 'SOURCE_REVISION=%s\n' "$source_sha"
sha256sum "$out"
