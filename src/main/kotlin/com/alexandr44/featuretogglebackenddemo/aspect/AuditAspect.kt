package com.alexandr44.featuretogglebackenddemo.aspect

import com.alexandr44.featuretogglebackenddemo.annotation.Auditable
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.entity.FeatureFlag
import com.alexandr44.featuretogglebackenddemo.enums.AuditAction
import com.alexandr44.featuretogglebackenddemo.repository.FeatureFlagRepository
import com.alexandr44.featuretogglebackenddemo.service.AuditLogService
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

@Aspect
@Component
class AuditAspect(
    private val auditLogService: AuditLogService,
    private val featureFlagRepository: FeatureFlagRepository,
    private val objectMapper: ObjectMapper
) {

    @Around("@annotation(com.alexandr44.featuretogglebackenddemo.annotation.Auditable)")
    fun auditMethod(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as MethodSignature).method
        val annotation = method.getAnnotation(Auditable::class.java)
        val args = joinPoint.args
        val result: Any

        when (annotation.entityType) {
            FeatureFlag.AUDIT_TYPE -> {
                val oldValueJson: String?
                val newValueJson: String
                val entityId: String
                when (annotation.action) {
                    AuditAction.CREATE -> {
                        oldValueJson = null

                        result = joinPoint.proceed() as FeatureFlagDto

                        val newValueKey = result.key
                        val newValue = featureFlagRepository.findByKey(newValueKey).get()
                        newValueJson = newValue.let { objectMapper.writeValueAsString(it) }
                        entityId = newValue.id.toString()
                    }

                    AuditAction.UPDATE, AuditAction.DELETE, AuditAction.TOGGLE -> {
                        val oldValueKey = args.first()
                        val oldValue = featureFlagRepository.findByKey(oldValueKey.toString()).orElse(null)
                        oldValueJson = oldValue.let { objectMapper.writeValueAsString(it) }

                        result = joinPoint.proceed() as FeatureFlagDto

                        val newValueKey = result.key
                        val newValue = featureFlagRepository.findByKey(newValueKey).get()
                        newValueJson = newValue.let { objectMapper.writeValueAsString(it) }
                        entityId = newValue.id.toString()
                    }

                    AuditAction.TOGGLE_BY_TAG -> {
                        val tag = args.first() as String
                        val oldValue = featureFlagRepository.findAllByTagAndActiveIsTrue(tag)
                        oldValueJson = oldValue.let { objectMapper.writeValueAsString(it) }

                        @Suppress("Unchecked_cast")
                        result = joinPoint.proceed() as List<FeatureFlagDto>

                        val newValue = featureFlagRepository.findAllByTagAndActiveIsTrue(tag)
                        newValueJson = newValue.let { objectMapper.writeValueAsString(it) }
                        entityId = newValue.map { it.id }.toString()
                    }
                }

                auditLogService.addAuditLog(
                    entityType = annotation.entityType,
                    entityId = entityId,
                    action = annotation.action,
                    oldValueJson = oldValueJson,
                    newValueJson = newValueJson
                )
            }

            else -> {
                result = joinPoint.proceed()
            }
        }

        return result
    }
}