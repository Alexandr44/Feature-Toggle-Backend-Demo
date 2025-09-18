package com.alexandr44.featuretogglebackenddemo.dto

import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import java.util.*

data class RegistrationResponse(
    val id: UUID,
    val username: String,
    val role: UserRole
)
