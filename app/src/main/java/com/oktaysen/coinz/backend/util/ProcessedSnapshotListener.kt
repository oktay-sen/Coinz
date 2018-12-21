package com.oktaysen.coinz.backend.util

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import timber.log.Timber

// A thin wrapper around a Firestore SnapshotListener that summarizes the changes.
inline fun <reified T:Any>ProcessedSnapshotListener(
        crossinline callback:(current: List<T>, added: List<T>, modified: List<T>, removed: List<T>) -> Unit
): (QuerySnapshot?, FirebaseFirestoreException?) -> Unit {
    return { snapshot, exception ->
        if (exception != null || snapshot == null) {
            Timber.e(exception)
        } else {
            Timber.v("Received update.")
            val current = snapshot.toObjects(T::class.java)
            val added: MutableList<T> = mutableListOf()
            val modified: MutableList<T> = mutableListOf()
            val removed: MutableList<T> = mutableListOf()
            snapshot.documentChanges.forEach { change ->
                when (change.type) {
                    DocumentChange.Type.ADDED -> added.add(change.document.toObject(T::class.java))
                    DocumentChange.Type.MODIFIED -> modified.add(change.document.toObject(T::class.java))
                    DocumentChange.Type.REMOVED -> removed.add(change.document.toObject(T::class.java))
                }
            }
            callback(current, added, modified, removed)
        }
    }
}