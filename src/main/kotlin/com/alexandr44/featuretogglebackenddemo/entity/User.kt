package com.alexandr44.featuretogglebackenddemo.entity

import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
class User(

    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 255)
    var username: String,

    @Column(nullable = false, length = 255)
    var password: String,

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var role: UserRole,

    @Column(nullable = false)
    var isActive: Boolean,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: Instant? = null

) {

    constructor() : this(
        username = "",
        password = "",
        role = UserRole.USER,
        isActive = true
    )
}
