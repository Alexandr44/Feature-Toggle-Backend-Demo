package com.alexandr44.featuretogglebackenddemo.entity

import com.alexandr44.featuretogglebackenddemo.enums.AuditAction
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener::class)
data class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var entityType: String,

    @Column(nullable = false)
    var entityId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var action: AuditAction,

    @Column(columnDefinition = "jsonb")
    var oldValue: String? = null,

    @Column(columnDefinition = "jsonb")
    var newValue: String? = null,

    @Column(nullable = false)
    var changedByName: String,

    @Column(nullable = false)
    var changedById: String,

    @CreatedDate
    @Column(nullable = false)
    var createdAt: Instant? = null

) {
    constructor() : this(
        entityType = "",
        entityId = "",
        action = AuditAction.CREATE,
        changedByName = "",
        changedById = ""
    )
}