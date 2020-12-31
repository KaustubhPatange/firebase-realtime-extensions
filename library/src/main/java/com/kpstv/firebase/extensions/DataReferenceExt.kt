package com.kpstv.firebase.extensions

import com.google.firebase.database.*
import com.google.firebase.database.snapshot.Node
import com.kpstv.firebase.ChildEventResponse
import com.kpstv.firebase.DataResponse
import com.kpstv.firebase.ValueEventResponse
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext

/**
 * Created by Kaustubh Patange at 31st Dec 2020
 */

typealias CancellationCallback = ((cause: Throwable) -> Unit)

/**
 * Performs a [DatabaseReference.setValue] event call on databaseReference as suspending.
 *
 * @param onCancellation action to perform if there is a cancellation
 */
suspend fun DatabaseReference.setValueAsync(value: Any?, priority: Node? = null, onCancellation: CancellationCallback = {}): DataResponse<DatabaseReference> = suspendCancellableCoroutine { continuation ->
    val completeListener = DatabaseReference.CompletionListener { error, ref ->
        if (error == null)
            continuation.resume(DataResponse.complete(ref), onCancellation)
        else
            continuation.resume(DataResponse.error(error.toException()), onCancellation)
    }
    setValue(value, priority, completeListener)
}

/**
 * Performs an [DatabaseReference.updateChildren] event call on databaseReference as suspending.
 *
 * @param onCancellation action to perform if there is a cancellation
 */
suspend fun DatabaseReference.updateChildrenAsync(value: Map<String, Any>, onCancellation: CancellationCallback = {}): DataResponse<DatabaseReference> = suspendCancellableCoroutine { continuation ->
    val completeListener = DatabaseReference.CompletionListener { error, ref ->
        if (error == null)
            continuation.resume(DataResponse.complete(ref), onCancellation)
        else
            continuation.resume(DataResponse.error(error.toException()), onCancellation)
    }
    updateChildren(value, completeListener)
}

/**
 * Performs an [DatabaseReference.setPriority] event call on databaseReference as suspending.
 *
 * @param onCancellation action to perform if there is a cancellation
 */
suspend fun DatabaseReference.setPriorityAsync(value: Any?, onCancellation: CancellationCallback = {}): DataResponse<DatabaseReference> = suspendCancellableCoroutine { continuation ->
    val completeListener = DatabaseReference.CompletionListener { error, ref ->
        if (error == null)
            continuation.resume(DataResponse.complete(ref), onCancellation)
        else
            continuation.resume(DataResponse.error(error.toException()), onCancellation)
    }
    setPriority(value, completeListener)
}

/**
 * Performs a [DatabaseReference.removeValue] event call on databaseReference as suspending.
 *
 * @param onCancellation action to perform if there is a cancellation
 */
suspend fun DatabaseReference.removeValueAsync(onCancellation: CancellationCallback = {}): DataResponse<DatabaseReference> {
    return setValueAsync(null)
}

/**
 * Perform a [Query.addListenerForSingleValueEvent] call on a databaseReference as suspending.
 */
suspend fun DatabaseReference.singleValueEvent(onCancellation: CancellationCallback = {}): DataResponse<DataSnapshot> = suspendCancellableCoroutine { continuation ->
    val valueEventListener = object: ValueEventListener{
        override fun onCancelled(error: DatabaseError) {
            continuation.resume(DataResponse.error(error.toException()), onCancellation)
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            continuation.resume(DataResponse.complete(snapshot), onCancellation)
        }
    }
    addListenerForSingleValueEvent(valueEventListener)
    continuation.invokeOnCancellation { removeEventListener(valueEventListener) }
}

/**
 * Returns a flow for [Query.addChildEventListener].
 *
 * Example code:
 * ```
 * val job = SupervisorJob()
 * CoroutineScope(Dispatchers.Main + job).launch {
 *    dataReference.childEventFlow().collect { result ->
 *       when(result) {
 *          ...
 *       }
 *    }
 * }
 * ```
 *
 * To stop collecting from the flow cancel the [CoroutineContext] `job.cancel()`.
 */
suspend fun DatabaseReference.childEventFlow(): Flow<ChildEventResponse> = callbackFlow {
    val childEventListener = object: ChildEventListener {
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?): Unit = sendBlocking(ChildEventResponse.Added(snapshot, previousChildName))
        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?): Unit = sendBlocking(ChildEventResponse.Changed(snapshot, previousChildName))
        override fun onChildRemoved(snapshot: DataSnapshot): Unit = sendBlocking(ChildEventResponse.Removed(snapshot))
        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?): Unit = sendBlocking(ChildEventResponse.Moved(snapshot, previousChildName))
        override fun onCancelled(error: DatabaseError): Unit = sendBlocking(ChildEventResponse.Cancelled(error))
    }
    addChildEventListener(childEventListener)
    awaitClose {
        removeEventListener(childEventListener)
    }
}

/**
 * Returns a flow for [Query.addValueEventListener].
 *
 * Example code:
 * ```
 * val job = SupervisorJob()
 * CoroutineScope(Dispatchers.Main + job).launch {
 *    dataReference.valueEventFlow().collect { result ->
 *       when(result) {
 *          ...
 *       }
 *    }
 * }
 * ```
 *
 * To stop collecting from the flow cancel the [CoroutineContext] `job.cancel()`.
 */
suspend fun DatabaseReference.valueEventFlow(): Flow<ValueEventResponse> = callbackFlow {
    val valueEventListener = object: ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot): Unit = sendBlocking(ValueEventResponse.Changed(snapshot))
        override fun onCancelled(error: DatabaseError): Unit = sendBlocking(ValueEventResponse.Cancelled(error))
    }
    addValueEventListener(valueEventListener)
    awaitClose {
        removeEventListener(valueEventListener)
    }
}