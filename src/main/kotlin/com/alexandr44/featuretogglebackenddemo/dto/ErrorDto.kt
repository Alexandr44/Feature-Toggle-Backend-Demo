package com.alexandr44.featuretogglebackenddemo.dto

data class ErrorDto(
    val message: String,
    val path: String,
    val code: Int,
)