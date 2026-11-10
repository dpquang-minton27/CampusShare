import fs from 'node:fs';
import { initializeApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore, Timestamp } from 'firebase-admin/firestore';

const projectId = process.env.GCLOUD_PROJECT || 'demo-campusshare';
const authHost = process.env.FIREBASE_AUTH_EMULATOR_HOST;
const dbHost = process.env.FIRESTORE_EMULATOR_HOST;
function loopback(host) { return /^(localhost|127\.0\.0\.1):\d+$/.test(host ?? ''); }
if (!projectId.startsWith('demo-') || !loopback(authHost) || !loopback(dbHost)) {
  throw new Error('REFUSED: Require demo-* project and BOTH local Firebase Emulator hosts.');
}
const seed = JSON.parse(fs.readFileSync(new URL('./seed.v2.json', import.meta.url), 'utf8'));
const app = initializeApp({ projectId });
const auth = getAuth(app);
const db = getFirestore(app);
for (const [i,user] of seed.users.entries()) {
  const email = seed.userContacts.find(c=>c.id===user.id)?.email;
  if (!email) throw new Error('Missing test email for ' + user.id);
  try { await auth.getUser(user.id); }
  catch(e) {
    if (e?.code !== 'auth/user-not-found') throw e;
    await auth.createUser({ uid: user.id, email, password: 'Campus123', displayName: user.fullName,
      emailVerified: true });
  }
}
const timestamps = ['createdAt', 'updatedAt'];
let written=0;
for (const group of ['users','userContacts','categories','posts','requests','favorites']) {
  for (const object of seed[group]) {
    const { id, ...payload } = object;
    for (const key of timestamps) {
      if (typeof payload[key] === 'string') payload[key]=Timestamp.fromDate(new Date(payload[key]));
    }
    await db.collection(group).doc(id).set(payload);
    written++;
  }
}
console.log(`SEED OK: ${written} synthetic documents, emulator project ${projectId}`);
