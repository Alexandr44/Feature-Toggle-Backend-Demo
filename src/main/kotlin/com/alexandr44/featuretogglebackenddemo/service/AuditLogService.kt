package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.entity.AuditLog
import com.alexandr44.featuretogglebackenddemo.enums.AuditAction
import com.alexandr44.featuretogglebackenddemo.exception.UserNotFoundException
import com.alexandr44.featuretogglebackenddemo.repository.AuditLogRepository
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class AuditLogService(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val auditLogRepository: AuditLogRepository
) {

    fun addAuditLog(
        entityType: String,
        entityId: String,
        action: AuditAction,
        oldValueJson: String?,
        newValueJson: String?
    ) {
        val username = authService.getCurrentUsername()
        val userId = userRepository.findByUsername(username)
            .orElseThrow { UserNotFoundException("Could not find user by username $username") }
            .id
            .toString()

        auditLogRepository.save(
            AuditLog(
                entityType = entityType,
                entityId = entityId,
                action = action,
                oldValue = oldValueJson,
                newValue = newValueJson,
                changedByName = username,
                changedById = userId
            )
        )
    }

}
