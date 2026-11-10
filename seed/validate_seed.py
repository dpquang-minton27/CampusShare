#!/usr/bin/env python3
"""Validate fixture references, uniqueness, and post/request state invariants."""
import json
from pathlib import Path

seed=json.loads((Path(__file__).parent/'seed.v2.json').read_text(encoding='utf-8'))
assert seed['schemaVersion'] == 2
for group in ('users','userContacts','categories','posts','requests','favorites'):
    entries=seed[group]
    ids=[e['id'] for e in entries]
    assert len(ids)==len(set(ids)),(group,'duplicate ids')
users={u['id'] for u in seed['users']}
cats={c['id'] for c in seed['categories']}
posts={p['id']:p for p in seed['posts']}
req={r['id']:r for r in seed['requests']}
assert {u['id'] for u in seed['userContacts']} == users
for p in posts.values():
    assert p['ownerId'] in users and p['categoryId'] in cats
    assert 0 <= p['conditionPercent'] <= 100
    assert len(p['imageUrls']) <= 3
    winner=p['acceptedRequestId']
    if winner:
        assert winner in req and req[winner]['status']=='ACCEPTED'
        assert req[winner]['postId']==p['id']
        assert p['status'] in {'TAKEN','EXCHANGED'}
    else:
        assert p['status'] in {'AVAILABLE','HIDDEN'}
for r in req.values():
    assert r['postId'] in posts
    assert r['requesterId'] in users and r['ownerId']==posts[r['postId']]['ownerId']
    assert r['requesterId'] != r['ownerId']
    assert r['id'] == r['postId']+'__'+r['requesterId']
    assert 1<=len(r['message'])<=200
    if r['status']=='ACCEPTED': assert posts[r['postId']]['acceptedRequestId']==r['id']
for f in seed['favorites']:
    assert f['userId'] in users and f['postId'] in posts
    assert f['id']==f['userId']+'__'+f['postId']
print('PASS: 3 User, 4 Category, 10 Post, 6 Request, 3 Favorite; references/invariants valid')
