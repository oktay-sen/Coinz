package com.oktaysen.coinz.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runners.JUnit4
import org.mockito.Mockito

class AuthTest {
    @Test
    fun `isLoggedIn returns true if logged in`() {
        val fakeAuth = mock<FirebaseAuth>()
        val fakeUser = mock<FirebaseUser>()
        Mockito.`when`(fakeAuth.currentUser).thenReturn(fakeUser)

        val auth = AuthInstance(fakeAuth)
        assertTrue(auth.isLoggedIn())
    }

    @Test
    fun `isLoggedIn returns false if not logged in`() {
        val fakeAuth = mock<FirebaseAuth>()
        Mockito.`when`(fakeAuth.currentUser).thenReturn(null)

        val auth = AuthInstance(fakeAuth)
        assertFalse(auth.isLoggedIn())
    }


    @Test
    fun `logOut calls signOut in FirebaseAuth`() {
        val fakeAuth = mock<FirebaseAuth>()

        val auth = AuthInstance(fakeAuth)
        auth.logOut()
        verify(fakeAuth).signOut()
    }

    @Test
    fun `addAuthStateListener adds a listener to FirebaseAuth`() {
        val fakeAuth = mock<FirebaseAuth>()
        Mockito.`when`(fakeAuth.currentUser).thenReturn(null)

    }
}