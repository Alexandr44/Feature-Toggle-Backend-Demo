package com.alexandr44.featuretogglebackenddemo.annotation

import com.alexandr44.featuretogglebackenddemo.enums.AuditAction

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Auditable(
    val entityType: String,
    val action: AuditAction
)
