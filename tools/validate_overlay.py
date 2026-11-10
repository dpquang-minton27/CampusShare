#!/usr/bin/env python3
"""Static checks only: does not replace Android Gradle, Firebase Emulator or Rules tests."""
from pathlib import Path
from xml.etree import ElementTree
import json

root = Path(__file__).resolve().parents[1]
for p in root.rglob("*.json"):
    if p.name == "google-services.json":
        raise SystemExit("Never bundle google-services.json")
    json.loads(p.read_text(encoding="utf-8"))
for p in root.rglob("*.xml"):
    ElementTree.parse(p)
base = root / "app/src/main/java/com/campusshare/app"
for kt in base.rglob("*.kt"):
    relative = kt.relative_to(base)
    expected = "package com.campusshare.app" + ("." + ".".join(relative.parts[:-1]) if len(relative.parts)>1 else "")
    assert expected in kt.read_text(encoding="utf-8"), (kt, expected)
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
assert "CampusShareApplication" in manifest and "android.permission.INTERNET" in manifest
rules = (root / "firebase/firestore.rules").read_text(encoding="utf-8")
for token in ["getAfter(", "contactShares", "requestId ==", "requestAcceptance", "allow delete: if false"]:
    assert token in rules, token
print("PASS: JSON, XML, package names, manifest, important Rules tokens")
print("NOT TESTED: Android build, real Firebase SDK integration, Rules semantics")
