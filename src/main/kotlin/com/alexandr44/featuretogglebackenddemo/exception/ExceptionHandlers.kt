package com.alexandr44.featuretogglebackenddemo.exception

import com.alexandr44.featuretogglebackenddemo.dto.ErrorDto
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionHandlers {

    private val log = KotlinLogging.logger {}

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(
        exception: AuthorizationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDto> {
        return logAndBuildResponse(
            exception,
            exception.message ?: "Authorization failed",
            HttpStatus.UNAUTHORIZED.value(),
            request
        )
    }

    @ExceptionHandler(RegistrationException::class)
    fun handleRegistrationException(
        exception: RegistrationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDto> {
        return logAndBuildResponse(
            exception,
            exception.message ?: "Registration failed",
            HttpStatus.BAD_REQUEST.value(),
            request
        )
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        exception: UserNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDto> {
        return logAndBuildResponse(
            exception,
            exception.message ?: "User not found",
            HttpStatus.NOT_FOUND.value(),
            request
        )
    }

    @ExceptionHandler(FeatureFlagOperationException::class)
    fun handleFeatureFlagNotFoundException(
        exception: FeatureFlagOperationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDto> {
        return logAndBuildResponse(
            exception,
            exception.message ?: "Feature flag operation error",
            exception.errorCode,
            request
        )
    }

    private fun logAndBuildResponse(
        exception: Exception,
        message: String,
        status: Int,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDto> {
        val path = "${request.method} ${request.requestURI} ${(request.queryString ?: "")}"
        log.error("Got error while requesting $path, got message $message and status $status", exception)
        val errorDto = ErrorDto(
            message = message,
            path = path,
            code = status
        )
        return ResponseEntity.status(status).body(errorDto)
    }

}