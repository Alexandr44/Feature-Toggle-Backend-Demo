package com.alexandr44.featuretogglebackenddemo.config

import com.alexandr44.featuretogglebackenddemo.security.AppUserDetailsService
import com.alexandr44.featuretogglebackenddemo.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val appUserDetailsService: AppUserDetailsService,
    private val jwtAuthFilter: JwtAuthFilter
) {

    companion object {
        private const val PASS_ENCODER_STRENGTH = 10
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(PASS_ENCODER_STRENGTH)

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager =
        authConfig.authenticationManager

    @Bean
    fun authProvider(): DaoAuthenticationProvider =
        DaoAuthenticationProvider(appUserDetailsService).apply {
            setPasswordEncoder(passwordEncoder())
        }

    @Bean
    fun securityFilterChain(http: HttpSecurity, daoAuthenticationProvider: DaoAuthenticationProvider): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authenticationProvider(daoAuthenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()

}
