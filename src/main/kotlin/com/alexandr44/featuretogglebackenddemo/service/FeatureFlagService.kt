package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagInputDto
import com.alexandr44.featuretogglebackenddemo.exception.FeatureFlagNotFoundException
import com.alexandr44.featuretogglebackenddemo.mapper.FeatureFlagMapper
import com.alexandr44.featuretogglebackenddemo.repository.FeatureFlagRepository
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
                .orElseThrow { FeatureFlagNotFoundException("Feature flag with key $key is not found or is not active") }
        )
    }

    @Transactional
    fun addFeatureFlag(featureFlagInputDto: FeatureFlagInputDto): FeatureFlagDto {
        val featureFlag = featureFlagRepository.save(
            featureFlagMapper.map(featureFlagInputDto)
        )
        return featureFlagMapper.map(
            featureFlag
        )
    }

    @Transactional
    fun editFeatureFlag(key: String, featureFlagInputDto: FeatureFlagInputDto): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow { FeatureFlagNotFoundException("Feature flag with key $key is not found or is not active") }
        featureFlagMapper.update(featureFlag, featureFlagInputDto)
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    fun deleteFeatureFlag(key: String): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow { FeatureFlagNotFoundException("Feature flag with key $key is not found or is not active") }
            .apply {
                active = false
            }
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    fun toggleFeatureFlag(key: String, value: Boolean): FeatureFlagDto {
        val featureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(key)
            .orElseThrow { FeatureFlagNotFoundException("Feature flag with key $key is not found or is not active") }
            .apply {
                this.value = value
            }
        return featureFlagMapper.map(featureFlag)
    }

    @Transactional
    fun toggleFeatureFlagsByTag(tag: String, value: Boolean): List<FeatureFlagDto> {
        val featureFlags = featureFlagRepository.findAllByTagAndActiveIsTrue(tag)
            .onEach {
                it.value = value
            }
        return featureFlagMapper.map(featureFlags)
    }

}