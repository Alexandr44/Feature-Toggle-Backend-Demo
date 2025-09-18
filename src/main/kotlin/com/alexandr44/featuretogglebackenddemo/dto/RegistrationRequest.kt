package com.alexandr44.featuretogglebackenddemo.dto

import com.alexandr44.featuretogglebackenddemo.enums.UserRole

data class RegistrationRequest(
    val username: String,
    val password: String,
    val role: UserRole? = UserRole.USER
)
