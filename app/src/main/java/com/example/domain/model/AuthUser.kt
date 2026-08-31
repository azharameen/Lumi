package com.example.domain.model

/**
 * Domain model representing an authenticated user from Firebase Auth & Google Identity.
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isNewUser: Boolean = false,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)
