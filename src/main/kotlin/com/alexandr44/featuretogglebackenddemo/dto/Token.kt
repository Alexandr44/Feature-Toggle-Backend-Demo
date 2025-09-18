package com.alexandr44.featuretogglebackenddemo.dto

import java.util.*

data class Token(
    val token: String,
    val expiresAt: Date
)
