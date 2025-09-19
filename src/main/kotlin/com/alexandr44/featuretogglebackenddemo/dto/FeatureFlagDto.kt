package com.alexandr44.featuretogglebackenddemo.dto

import java.time.Instant

data class FeatureFlagDto(
    val id: Long,
    val key: String,
    val name: String,
    val tag: String?,
    val description: String?,
    val value: Boolean,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)