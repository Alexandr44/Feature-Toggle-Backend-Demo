package com.alexandr44.featuretogglebackenddemo.controller

import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagInputDto
import com.alexandr44.featuretogglebackenddemo.service.FeatureFlagService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/feature-flags")
class FeatureFlagController(
    private val featureFlagService: FeatureFlagService
) {

    @GetMapping
    fun getAll(
        @RequestParam(required = false) tag: String?
    ): ResponseEntity<List<FeatureFlagDto>> {
        return ResponseEntity.ok(featureFlagService.getAll(tag))
    }

    @GetMapping("/{key}")
    fun getFeatureFlag(
        @PathVariable key: String
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(featureFlagService.getByKey(key))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun addFeatureFlag(
        @RequestBody featureFlagInputDto: FeatureFlagInputDto
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(featureFlagService.addFeatureFlag(featureFlagInputDto))
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editFeatureFlag(
        @PathVariable key: String,
        @RequestBody featureFlagInputDto: FeatureFlagInputDto
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(featureFlagService.editFeatureFlag(key, featureFlagInputDto))
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editFeatureFlag(
        @PathVariable key: String
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(featureFlagService.deleteFeatureFlag(key))
    }

    @PutMapping("/toggle/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun toggleFeatureFlag(
        @PathVariable key: String,
        @RequestParam value: Boolean
    ): ResponseEntity<FeatureFlagDto> {
        return ResponseEntity.ok(featureFlagService.toggleFeatureFlag(key, value))
    }

    @PutMapping("/toggle/tag")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    fun toggleFeatureFlagsByTag(
        @RequestParam tag: String,
        @RequestParam value: Boolean
    ): ResponseEntity<List<FeatureFlagDto>> {
        return ResponseEntity.ok(featureFlagService.toggleFeatureFlagsByTag(tag, value))
    }

}
