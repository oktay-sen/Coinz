package com.oktaysen.coinz.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration

//Storage for Firestore listeners to be addressed by their id and canceled later.
class ListenerRegistryInstance {
    private val registrations: MutableMap<Int, ListenerRegistration> = mutableMapOf()

    fun register(registration: ListenerRegistration): Int {
        var registrationId: Int
        do {
            registrationId = Math.floor(Math.random()*100000).toInt()
        } while (registrations.containsKey(registrationId))
        registrations.put(registrationId, registration)
        return registrationId
    }

    fun unregister(id: Int): Boolean {
        val registration = registrations[id] ?: return false
        registration.remove()
        registrations.remove(id)
        return true
    }

    fun unregisterAll() {
        registrations.values.forEach { it.remove() }
        registrations.clear()
    }
}

private val registryInstance:ListenerRegistryInstance = ListenerRegistryInstance()

fun ListenerRegistry():ListenerRegistryInstance {
    return registryInstance
}