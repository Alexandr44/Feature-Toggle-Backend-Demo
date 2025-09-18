package com.alexandr44.featuretogglebackenddemo.dto

import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import java.time.Instant
import java.util.*

data class UserDto(
    val id: UUID,
    val username: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)