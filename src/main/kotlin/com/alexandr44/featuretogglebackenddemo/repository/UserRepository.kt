package com.alexandr44.featuretogglebackenddemo.repository

import com.alexandr44.featuretogglebackenddemo.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByUsername(username: String): Optional<User>
}
