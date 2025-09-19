package com.alexandr44.featuretogglebackenddemo.dto

data class FeatureFlagInputDto(
    val key: String?,
    val name: String?,
    val tag: String?,
    val description: String?,
    val value: Boolean?,
    val active: Boolean?
)