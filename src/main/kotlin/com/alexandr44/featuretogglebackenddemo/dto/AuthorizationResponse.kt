package com.alexandr44.featuretogglebackenddemo.dto

import java.time.Instant

data class AuthorizationResponse(
    val username: String,
    val token: String,
    val expiresAt: Instant
)
