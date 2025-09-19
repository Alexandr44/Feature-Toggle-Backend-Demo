package com.alexandr44.featuretogglebackenddemo.security

import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AppUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByisActiveIsTrueAndUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found or nor active: $username") }
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            user.isActive,
            true,
            true,
            true,
            authorities
        )
    }

}
