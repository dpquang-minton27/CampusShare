#!/usr/bin/env python3
"""Copy Quang-owned overlay onto a clean GitHub clone checked out on develop.

It never commits, pushes, creates a branch, or modifies a remote repository.
"""
import argparse
from pathlib import Path
import subprocess
import shutil
from datetime import datetime, timezone

parser = argparse.ArgumentParser()
parser.add_argument('--target', required=True, type=Path, help='Existing CampusShare Git clone')
parser.add_argument('--apply', action='store_true', help='Actually copy the overlay')
parser.add_argument('--dry-run', action='store_true', help='Show plan only (default)')
args = parser.parse_args()
source = Path(__file__).resolve().parents[1]
target = args.target.expanduser().resolve()
if not (target / '.git').exists() or not (target / 'settings.gradle.kts').is_file():
    parser.error('Target must be an existing CampusShare Git clone, not an arbitrary directory.')
def git(*cmd):
    return subprocess.check_output(['git', '-C', str(target), *cmd], text=True).strip()
branch = git('branch', '--show-current')
if branch != 'develop':
    parser.error(f'Only the develop branch is allowed (current: {branch})')
if args.apply and git('status', '--porcelain'):
    parser.error('Worktree must be clean before apply. Commit/stash your local changes first.')
files = []
for p in sorted(source.rglob('*')):
    if not p.is_file() or '/.local-check/' in str(p) or '__pycache__' in p.parts:
        continue
    if p.name.endswith(('.jar', '.class', '.pyc', '.zip')):
        continue
    rel = p.relative_to(source)
    if any(part.startswith('.') and part not in ('.gitignore', '.github') for part in rel.parts):
        continue
    files.append((p, rel, target / rel))
for _, rel, dest in files:
    print(('REPLACE ' if dest.exists() else 'CREATE  ') + str(rel))
if not args.apply:
    print(f'DRY-RUN ONLY: {len(files)} files. Add --apply after reviewing the list.')
    raise SystemExit(0)
backup = target.parent / (target.name + '-quang-backup-' + datetime.now(timezone.utc).strftime('%Y%m%d-%H%M%S'))
for _, rel, dest in files:
    if dest.exists():
        old = backup / rel
        old.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(dest, old)
for src, rel, dest in files:
    dest.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dest)
print(f'APPLIED {len(files)} files to local develop; prior versions backed up at {backup}')
print('NEXT: Gradle Sync + tests + inspect git diff; git commit/push manually.')
