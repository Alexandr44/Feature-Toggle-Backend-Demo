package com.alexandr44.featuretogglebackenddemo.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@Entity
@Table(name = "feature_flags")
@EntityListeners(AuditingEntityListener::class)
class FeatureFlag(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var key: String,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column
    var tag: String? = null,

    @Column(length = 1000)
    var description: String? = null,

    @Column(nullable = false)
    var value: Boolean,

    @Column(name = "is_active", nullable = false)
    var active: Boolean = true,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        key = "",
        name = "",
        value = true
    )
}
