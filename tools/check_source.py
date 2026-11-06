#!/usr/bin/env python3
"""Verify the complete source inventory; never contacts a device or network."""
from pathlib import Path
import hashlib, json, os

ROOT = Path(__file__).resolve().parents[1]
NAME = "packaging/SOURCE-MANIFEST.json"
record = json.loads((ROOT / NAME).read_text())
ignored = {".git", "build", ".gradle", "__pycache__"}
actual = {p.relative_to(ROOT).as_posix() for p in ROOT.rglob("*")
          if p.is_file() and not any(part in ignored for part in p.relative_to(ROOT).parts)}
assert actual == set(record["files"]) | {NAME}, "Unexpected or missing source file"
for name, item in record["files"].items():
    path = ROOT / name
    assert not path.is_symlink(), name
    assert hashlib.sha256(path.read_bytes()).hexdigest() == item["sha256"], name
    if os.name != "nt":
        assert bool(path.stat().st_mode & 0o111) == (item["mode"] == "100755"), name
    assert not name.lower().endswith((".apk", ".jar", ".class", ".dex", ".jks", ".keystore", ".pem")), name
print("SOURCE_INTEGRITY_PASS files=" + str(len(record["files"])))
