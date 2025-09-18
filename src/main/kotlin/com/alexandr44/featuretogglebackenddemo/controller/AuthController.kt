package com.alexandr44.featuretogglebackenddemo.controller

import com.alexandr44.featuretogglebackenddemo.dto.AuthorizationRequest
import com.alexandr44.featuretogglebackenddemo.dto.AuthorizationResponse
import com.alexandr44.featuretogglebackenddemo.dto.RegistrationRequest
import com.alexandr44.featuretogglebackenddemo.dto.RegistrationResponse
import com.alexandr44.featuretogglebackenddemo.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    fun register(@RequestBody registrationRequest: RegistrationRequest): ResponseEntity<RegistrationResponse> {
        return ResponseEntity.ok(authService.registerUser(registrationRequest))
    }

    @PostMapping("/login")
    fun login(@RequestBody authorizationRequest: AuthorizationRequest): ResponseEntity<AuthorizationResponse> {
        return ResponseEntity.ok(authService.login(authorizationRequest))
    }
}
