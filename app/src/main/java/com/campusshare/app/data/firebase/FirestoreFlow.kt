package com.campusshare.app.data.firebase

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal fun DocumentReference.observe(): Flow<DocumentSnapshot?> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        when {
            error != null -> close(error)
            else -> trySend(snapshot)
        }
    }
    awaitClose { listener.remove() }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
internal fun Query.observe(): Flow<QuerySnapshot> = callbackFlow {
    val listener = addSnapshotListener { snapshot, error ->
        when {
            error != null -> close(error)
            snapshot != null -> trySend(snapshot)
        }
    }
    awaitClose { listener.remove() }
}
