package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.dto.AuthorizationRequest
import com.alexandr44.featuretogglebackenddemo.dto.AuthorizationResponse
import com.alexandr44.featuretogglebackenddemo.dto.RegistrationRequest
import com.alexandr44.featuretogglebackenddemo.dto.RegistrationResponse
import com.alexandr44.featuretogglebackenddemo.entity.User
import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import com.alexandr44.featuretogglebackenddemo.exception.AuthorizationException
import com.alexandr44.featuretogglebackenddemo.exception.RegistrationException
import com.alexandr44.featuretogglebackenddemo.mapper.AuthMapper
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import com.alexandr44.featuretogglebackenddemo.security.AppUserDetailsService
import com.alexandr44.featuretogglebackenddemo.security.JwtTokenService
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: AuthenticationManager,
    private val appUserDetailsService: AppUserDetailsService,
    private val jwtService: JwtTokenService,
    private val authMapper: AuthMapper
) {

    private val log = KotlinLogging.logger {}

    @Transactional
    fun registerUser(registrationRequest: RegistrationRequest): RegistrationResponse {
        if (userRepository.findByUsername(registrationRequest.username).isPresent) {
            throw RegistrationException("Username already taken")
        }

        val user = User(
            username = registrationRequest.username,
            password = passwordEncoder.encode(registrationRequest.password),
            role = registrationRequest.role ?: UserRole.USER,
            isActive = true
        )
        return authMapper.map(userRepository.save(user))
    }

    fun login(authorizationRequest: AuthorizationRequest): AuthorizationResponse {
        try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authorizationRequest.username, authorizationRequest.password)
            ).apply {
                if (!this.isAuthenticated) {
                    log.error { "Authorization failed for user ${authorizationRequest.username}" }
                    throw AuthorizationException("Authentication failed!")
                }
            }
        } catch (e: AuthenticationException) {
            log.error { "User ${authorizationRequest.username} could not make authentication" }
            throw AuthorizationException(e.message ?: "Couldn't make authentication")
        }
        val userDetails = appUserDetailsService.loadUserByUsername(authorizationRequest.username)
        return authMapper.map(
            username = userDetails.username,
            token = jwtService.generateToken(userDetails)
        )
    }

    fun getCurrentUsername(): String {
        val authentication = SecurityContextHolder.getContext().authentication

        return when {
            authentication == null -> {
                log.warn { "Anonymous user in security context" }
                "anonymous"
            }

            authentication.principal is UserDetails -> (authentication.principal as UserDetails).username
            authentication.principal is String -> authentication.principal as String
            else -> {
                log.warn { "Unknown user in security context $authentication" }
                "unknown"
            }
        }
    }

}
