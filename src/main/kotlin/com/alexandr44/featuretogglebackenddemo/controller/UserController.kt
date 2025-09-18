package com.alexandr44.featuretogglebackenddemo.controller

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    fun getAll(): ResponseEntity<List<UserDto>> {
        return ResponseEntity.ok(userService.getAll())
    }

}
