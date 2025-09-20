package com.alexandr44.featuretogglebackenddemo.repository

import com.alexandr44.featuretogglebackenddemo.entity.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, Long> {

}