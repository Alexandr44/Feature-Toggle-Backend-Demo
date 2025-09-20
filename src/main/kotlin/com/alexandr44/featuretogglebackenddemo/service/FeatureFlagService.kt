package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.annotation.Auditable
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagInputDto
import com.alexandr44.featuretogglebackenddemo.entity.FeatureFlag
import com.alexandr44.featuretogglebackenddemo.enums.AuditAction
import com.alexandr44.featuretogglebackenddemo.exception.FeatureFlagOperationException
import com.alexandr44.featuretogglebackenddemo.mapper.FeatureFlagMapper
import com.alexandr44.featuretogglebackenddemo.repository.FeatureFlagRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeatureFlagService(
    val featureFlagMapper: FeatureFlagMapper,
    val featureFlagRepository: FeatureFlagRepository,
) {

    fun getAll(tag: String?): List<FeatureFlagDto> {
        return featureFlagMapper.map(
            if (tag == null)
                featureFlagRepository.findAll()
            else
                featureFlagRepository.findAllByTagAndActiveIsTrue(tag)
        )
    }

    fun getByKey(key: String): FeatureFlagDto {
        return featureFlagMapper.map(
            featureFlagRepository.findByKeyAndActiveIsTrue(key)
                .orElseThrow {
                    FeatureFlagOperationException(
                        "Feature flag with key $key is not found or is not active",
                        HttpStatus.NOT_FOUND.value()
                    )
                }
        )
    }

    @Transactional
    @Auditable(entityType = FeatureFlag.AUDIT_TYPE, action = AuditAction.CREATE)
    fun addFeatureFlag(featureFlagInputDto: FeatureFlagInputDto): FeatureFlagDto {
        if (featureFlagInputDto.key == null) {
            throw FeatureFlagOperationException("Feature flag key is missing", HttpStatus.BAD_REQUEST.value())
        }
        if (featureFlagRepository.findByKey(featureFlagInputDto.key).isPresent) {
            throw FeatureFlagOperationException(
                "Feature key ${featureFlagInputDto.key} already taken",
                HttpStatus.BAD_REQUEST.value()
            )
        }
        val featureFlag = featureFlagRepository.save(
            featureFlagMapper.map(featureFlagInputDto)
        )
        return featureFlagMapper.map(
            featureFlag
        )
    }

    @Transactional
    @Auditable(entityType = FeatureFlag.AUDIT_TYPE, action = AuditAction.UPDATE)
    fun editFeatureFlag(key: String, featureFlagInputDto: FeatureFlagInputDto): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow {
                FeatureFlagOperationException(
                    "Feature flag with key $key is not found or is not active",
                    HttpStatus.NOT_FOUND.value()
                )
            }
        featureFlagMapper.update(featureFlag, featureFlagInputDto)
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    @Auditable(entityType = FeatureFlag.AUDIT_TYPE, action = AuditAction.DELETE)
    fun deleteFeatureFlag(key: String): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow {
                FeatureFlagOperationException(
                    "Feature flag with key $key is not found or is not active",
                    HttpStatus.NOT_FOUND.value()
                )
            }
            .apply {
                active = false
            }
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    @Auditable(entityType = FeatureFlag.AUDIT_TYPE, action = AuditAction.TOGGLE)
    fun toggleFeatureFlag(key: String, value: Boolean): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow {
                FeatureFlagOperationException(
                    "Feature flag with key $key is not found or is not active",
                    HttpStatus.NOT_FOUND.value()
                )
            }
            .apply {
                this.value = value
            }
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    @Auditable(entityType = FeatureFlag.AUDIT_TYPE, action = AuditAction.TOGGLE_BY_TAG)
    fun toggleFeatureFlagsByTag(tag: String, value: Boolean): List<FeatureFlagDto> {
        val featureFlags = featureFlagRepository.findAllByTagAndActiveIsTrue(tag)
            .onEach {
                it.value = value
            }
        return featureFlagMapper.map(featureFlags)
    }

}