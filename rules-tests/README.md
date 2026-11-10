# Security Rules integration tests

This suite requires a running **local Firestore Emulator**. It does not connect to production.

```sh
firebase emulators:start --only firestore --project demo-campusshare-security-tests
# another terminal
cd rules-tests
npm install
FIRESTORE_EMULATOR_HOST=127.0.0.1:8080 npm test
```

Windows PowerShell:

```powershell
$env:FIRESTORE_EMULATOR_HOST = "127.0.0.1:8080"
npm test
```

Tests cover hidden Post visibility, private contacts, unauthorized writes, verified email, unique requests and `getAfter`-protected two-document acceptance. The suite also checks competing accepts for giveaway and exchange posts, owner-scoped rejection of losing requests, the post create/edit/read/hide flow, and private favorites.

Do not assert that these tests passed merely because the file exists. They must actually be run with a local Firebase Emulator and recorded with the emulator version.
