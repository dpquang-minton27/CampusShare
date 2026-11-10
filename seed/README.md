# Fake fixtures & Firebase Emulator seed

- `seed.v2.json`: 3 synthetic users, 4 categories, 10 posts, 6 requests, 3 favorites.
- `validate_seed.py`: reference checks and winner-state invariants.
- `import_to_emulators.mjs`: creates corresponding Firebase Authentication users (UID `u1`, `u2`, `u3`) and Firestore documents.
- Email `an@sv.example.edu`, `binh@sv.example.edu`, `chi@sv.example.edu`; password `Campus123` is for LOCAL EMULATOR ONLY.
- **NEVER** run this seeder against any real Firebase project. Script explicitly requires a `demo-*` project ID and emulator loopback hosts.

Start emulators at the project root:

```sh
firebase emulators:start --only auth,firestore --project demo-campusshare
```

In another terminal:

```sh
cd seed
npm install
export GCLOUD_PROJECT=demo-campusshare
export FIRESTORE_EMULATOR_HOST=127.0.0.1:8080
export FIREBASE_AUTH_EMULATOR_HOST=127.0.0.1:9099
npm run seed
```

On Windows PowerShell use `$env:GCLOUD_PROJECT = 'demo-campusshare'` and analogously set both hosts. The emulator clears data when stopped unless exported with its own persistence options.

Android emulator connects using `10.0.2.2`, not `localhost`. For two real devices and the online demo, use a real Firebase project and create REAL test accounts manually; never reuse hardcoded fake UIDs or demo passwords in production.
