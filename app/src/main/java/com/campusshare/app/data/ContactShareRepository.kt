package com.campusshare.app.data

import com.campusshare.app.domain.model.UserContact

/** A contact share is a separate opt-in record, readable only after ACCEPTED. */
interface ContactShareRepository {
    suspend fun shareMyContact(requestId: String)
    suspend fun getOtherPartyContact(requestId: String): UserContact?
}
