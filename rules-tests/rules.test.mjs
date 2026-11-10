import fs from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, getDoc, getDocs, setDoc, updateDoc, serverTimestamp, query,
  collection, where, writeBatch, runTransaction, deleteDoc, Timestamp } from 'firebase/firestore';

if (!process.env.FIRESTORE_EMULATOR_HOST) {
  throw new Error('Start the Firestore Emulator before testing rules.');
}
const rules = fs.readFileSync(new URL('../firebase/firestore.rules', import.meta.url), 'utf8');
const env = await initializeTestEnvironment({
  projectId: 'demo-campusshare-security-tests', firestore: { rules }
});
const owner = env.authenticatedContext('owner', { email:'owner@example.edu', email_verified:true }).firestore();
const requester = env.authenticatedContext('requester', { email:'requester@example.edu', email_verified:true }).firestore();
const stranger = env.authenticatedContext('stranger', { email:'stranger@example.edu', email_verified:true }).firestore();
const unverified = env.authenticatedContext('unverified', { email:'u@example.edu', email_verified:false }).firestore();
const basePost = {
  ownerId:'owner', categoryId:'books', title:'Sách kỹ thuật', description:'Sách còn rất mới',
  type:'GIVE_AWAY', status:'AVAILABLE', imageUrls:[], conditionPercent:90,
  meetingLocation:'Thư viện', exchangeNote:null, acceptedRequestId:null,
  createdAt:Timestamp.now(), updatedAt:Timestamp.now()
};
try {
  await env.withSecurityRulesDisabled(async (ctx) => {
    const admin = ctx.firestore();
    await setDoc(doc(admin,'categories/books'), { name:'Sách',iconKey:'books',sortOrder:1,active:true });
    await setDoc(doc(admin,'posts/postA'), basePost);
    await setDoc(doc(admin,'posts/hidden'), {...basePost, status:'HIDDEN'});
    await setDoc(doc(admin,'userContacts/owner'), {email:'owner@example.edu',phone:'000000000',zaloUrl:null,updatedAt:Timestamp.now()});
  });
  // Read access: public post vs private contact/hidden post.
  await assertSucceeds(getDoc(doc(requester,'posts/postA')));
  await assertFails(getDoc(doc(requester,'posts/hidden')));
  await assertSucceeds(getDoc(doc(owner,'posts/hidden')));
  await assertFails(getDoc(doc(requester,'userContacts/owner')));
  await assertFails(getDocs(query(collection(stranger,'posts'),where('status','==','HIDDEN'))));

  // Owner-only writes, verified-email write restriction.
  await assertFails(updateDoc(doc(requester,'posts/postA'),{title:'Đổi tên trái phép'}));
  const newPost = { ...basePost, createdAt:serverTimestamp(), updatedAt:serverTimestamp() };
  await assertFails(setDoc(doc(unverified,'posts/unverifiedPost'), {...newPost,ownerId:'unverified'}));
  await assertSucceeds(setDoc(doc(owner,'posts/verifiedPost'), newPost));

  const requestId='postA__requester';
  await assertSucceeds(setDoc(doc(requester,`requests/${requestId}`),{
    postId:'postA',ownerId:'owner',requesterId:'requester',message:'Em muốn nhận tài liệu',
    status:'PENDING',createdAt:serverTimestamp(),updatedAt:serverTimestamp()
  }));
  await assertFails(setDoc(doc(requester,'requests/postA__requesterAgain'),{
    postId:'postA',ownerId:'owner',requesterId:'requester',message:'Spam',
    status:'PENDING',createdAt:serverTimestamp(),updatedAt:serverTimestamp()
  }));
  await assertFails(updateDoc(doc(requester,`requests/${requestId}`),{status:'ACCEPTED'}));

  // Two-document ACCEPT must be atomic, verified by getAfter in rules.
  const batch=writeBatch(owner);
  batch.update(doc(owner,'posts/postA'), {status:'TAKEN',acceptedRequestId:requestId,updatedAt:serverTimestamp()});
  batch.update(doc(owner,`requests/${requestId}`), {status:'ACCEPTED',updatedAt:serverTimestamp()});
  await assertSucceeds(batch.commit());
  assert.equal((await getDoc(doc(owner,'posts/postA'))).data().acceptedRequestId,requestId);
  await assertFails(setDoc(doc(stranger,'requests/postA__stranger'),{
    postId:'postA',ownerId:'owner',requesterId:'stranger',message:'Em xin',
    status:'PENDING',createdAt:serverTimestamp(),updatedAt:serverTimestamp()
  }));
  console.log('PASS: owner access, hidden/contact privacy, verified writes, atomic acceptance');

  // Two requesters compete for one available post. The owner must get exactly one winner.
  for (const [type, expectedStatus] of [['GIVE_AWAY', 'TAKEN'], ['EXCHANGE', 'EXCHANGED']]) {
    const racePostId = `race-${type}`;
    const postRef = doc(owner, 'posts', racePostId);
    await assertSucceeds(setDoc(postRef, {
      ...newPost, type, exchangeNote: type === 'EXCHANGE' ? 'Đổi giáo trình khác' : null
    }));
    const send = (db, uid) => setDoc(doc(db, 'requests', `${racePostId}__${uid}`), {
      postId: racePostId, ownerId: 'owner', requesterId: uid, message: 'Em muốn nhận tài liệu',
      status: 'PENDING', createdAt: serverTimestamp(), updatedAt: serverTimestamp()
    });
    await assertFails(send(owner, 'owner'));
    await assertSucceeds(send(requester, 'requester'));
    await assertFails(send(requester, 'requester'));
    await assertSucceeds(send(stranger, 'stranger'));

    const accept = (db, uid) => runTransaction(db, async (tx) => {
      const txPostRef = doc(db, 'posts', racePostId);
      const id = `${racePostId}__${uid}`;
      const requestRef = doc(db, 'requests', id);
      const post = (await tx.get(txPostRef)).data();
      const pending = (await tx.get(requestRef)).data();
      assert.equal(post.status, 'AVAILABLE');
      assert.equal(post.acceptedRequestId, null);
      assert.equal(pending.status, 'PENDING');
      tx.update(txPostRef, {
        status: expectedStatus, acceptedRequestId: id, updatedAt: serverTimestamp()
      });
      tx.update(requestRef, { status: 'ACCEPTED', updatedAt: serverTimestamp() });
      return id;
    });
    await assertFails(accept(requester, 'requester'));
    const contenders = await Promise.allSettled([accept(owner, 'requester'), accept(owner, 'stranger')]);
    const winners = contenders.filter(result => result.status === 'fulfilled');
    assert.equal(winners.length, 1);
    const winnerId = winners[0].value;
    const post = (await getDoc(postRef)).data();
    assert.equal(post.acceptedRequestId, winnerId);
    assert.equal(post.status, expectedStatus);
    assert.equal((await getDoc(doc(owner, 'requests', winnerId))).data().status, 'ACCEPTED');

    const byPost = where('postId', '==', racePostId);
    const byPending = where('status', '==', 'PENDING');
    await assertFails(getDocs(query(collection(owner, 'requests'), byPost, byPending)));
    const pending = await assertSucceeds(getDocs(query(
      collection(owner, 'requests'), where('ownerId', '==', 'owner'), byPost, byPending
    )));
    assert.equal(pending.size, 1);
    const loser = pending.docs[0];
    assert.notEqual(loser.id, winnerId);
    const rejectBatch = writeBatch(owner);
    rejectBatch.update(loser.ref, { status: 'REJECTED', updatedAt: serverTimestamp() });
    await assertSucceeds(rejectBatch.commit());
    assert.equal((await getDoc(loser.ref)).data().status, 'REJECTED');
    console.log(`PASS: two competing ${type} accepts leave one ${expectedStatus} post and reject the other request`);
  }

  // Verify the shared list/detail contract, hiding, and personal favorite permissions.
  const i2Id = 'post-i2';
  const i2Owner = doc(owner, 'posts', i2Id);
  const i2Reader = doc(requester, 'posts', i2Id);
  await assertSucceeds(setDoc(i2Owner, newPost));
  await assertSucceeds(updateDoc(i2Owner, { title: 'Sách BC3 đã sửa', updatedAt: serverTimestamp() }));
  const readHome = () => getDocs(query(collection(requester, 'posts'), where('status', '==', 'AVAILABLE')));
  const mine = await assertSucceeds(getDocs(query(collection(owner, 'posts'), where('ownerId', '==', 'owner'))));
  assert.ok(mine.docs.some(post => post.id === i2Id));
  assert.ok((await assertSucceeds(readHome())).docs.some(post => post.id === i2Id));
  assert.equal((await assertSucceeds(getDoc(i2Reader))).data().title, 'Sách BC3 đã sửa');
  const favoriteId = `requester__${i2Id}`;
  const myFavorite = doc(requester, 'favorites', favoriteId);
  await assertSucceeds(setDoc(myFavorite, { userId: 'requester', postId: i2Id, createdAt: serverTimestamp() }));
  await assertFails(getDoc(doc(owner, 'favorites', favoriteId)));
  const saved = await assertSucceeds(getDocs(query(
    collection(requester, 'favorites'), where('userId', '==', 'requester')
  )));
  assert.ok(saved.docs.some(favorite => favorite.id === favoriteId));
  await assertSucceeds(deleteDoc(myFavorite));
  await assertSucceeds(updateDoc(i2Owner, { status: 'HIDDEN', updatedAt: serverTimestamp() }));
  assert.ok(!(await assertSucceeds(readHome())).docs.some(post => post.id === i2Id));
  await assertFails(getDoc(i2Reader));
  console.log('PASS: created and edited posts reach owner list, home and detail, favorites stay private, hidden posts disappear');
} finally {
  await env.cleanup();
}
