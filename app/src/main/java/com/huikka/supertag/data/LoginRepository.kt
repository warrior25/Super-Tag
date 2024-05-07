package com.huikka.supertag.data

import com.google.firebase.auth.FirebaseUser

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

    var user: FirebaseUser? = dataSource.getCurrentUser()

    val isLoggedIn: Boolean = user != null

    suspend fun login(username: String, password: String): Error? {
        return dataSource.login(username, password)
    }

    suspend fun register(username: String, password: String, displayName: String): Error? {
        return dataSource.register(username, password, displayName)
    }

    fun logout(): Error? {
        return dataSource.logout()
    }
}