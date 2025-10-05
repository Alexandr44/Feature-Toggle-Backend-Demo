package com.alexandr44.featuretogglebackenddemo.dto

import com.alexandr44.featuretogglebackenddemo.enums.UserRole

data class UserEditDto(
    val username: String?,
    val password: String?,
    val role: UserRole?,
    val active: Boolean?
)