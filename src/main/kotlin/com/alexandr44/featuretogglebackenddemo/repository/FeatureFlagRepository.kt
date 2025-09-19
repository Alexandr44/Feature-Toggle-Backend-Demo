package com.alexandr44.featuretogglebackenddemo.repository

import com.alexandr44.featuretogglebackenddemo.entity.FeatureFlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FeatureFlagRepository : JpaRepository<FeatureFlag, Long> {

    fun findAllByTagAndActiveIsTrue(tag: String): List<FeatureFlag>

    fun findByKeyAndActiveIsTrue(key: String): Optional<FeatureFlag>

}
