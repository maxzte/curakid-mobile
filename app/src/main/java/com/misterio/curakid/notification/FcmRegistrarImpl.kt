package com.misterio.curakid.notification

import com.google.firebase.messaging.FirebaseMessaging
import com.misterio.core.domain.util.FcmRegistrar
import com.misterio.core.domain.util.suspendRunCatching
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRegistrarImpl @Inject constructor() : FcmRegistrar {
    override suspend fun register(): Result<Unit> {
        return suspendRunCatching {
            FirebaseMessaging.getInstance().register()
        }.map { Unit }
    }
}
