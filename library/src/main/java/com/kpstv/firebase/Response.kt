package com.kpstv.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

/**
 * A response state for firebase events.
 */
sealed class DataResponse<T> {
    data class Complete<T>(val data: T): DataResponse<T>()
    data class Error<T>(val error: Exception): DataResponse<T>()

    companion object {
        fun<T> complete(data: T): DataResponse<T> = Complete(data)
        fun<T> error(e: Exception): DataResponse<T> = Error(e)
    }
}

/**
 * A response state for ChildEvent listener.
 */
sealed class ChildEventResponse {
    data class Added(val snapshot: DataSnapshot, val previousChildName: String?): ChildEventResponse()
    data class Changed(val snapshot: DataSnapshot, val previousChildName: String?): ChildEventResponse()
    data class Moved(val snapshot: DataSnapshot, val previousChildName: String?): ChildEventResponse()

    data class Removed(val snapshot: DataSnapshot): ChildEventResponse()
    data class Cancelled(val error: DatabaseError): ChildEventResponse()
}

/**
 * A response state for ValueEvent listener.
 */
sealed class ValueEventResponse {
    data class Changed(val snapshot: DataSnapshot): ValueEventResponse()
    data class Cancelled(val error: DatabaseError): ValueEventResponse()
}