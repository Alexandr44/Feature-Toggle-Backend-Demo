package com.alexandr44.featuretogglebackenddemo.mapper

import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagInputDto
import com.alexandr44.featuretogglebackenddemo.entity.FeatureFlag
import org.mapstruct.Mapper
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface FeatureFlagMapper {

    fun map(featureFlag: FeatureFlag): FeatureFlagDto

    fun map(featureFlags: List<FeatureFlag>): List<FeatureFlagDto>

    fun map(featureFlagDto: FeatureFlagInputDto): FeatureFlag

    fun update(@MappingTarget featureFlag: FeatureFlag, featureFlagInputDto: FeatureFlagInputDto): FeatureFlag

}
