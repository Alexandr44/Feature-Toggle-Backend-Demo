package com.alexandr44.featuretogglebackenddemo.controller

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.dto.UserEditDto
import com.alexandr44.featuretogglebackenddemo.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun editUser(
        @PathVariable id: UUID,
        @RequestBody userEditDto: UserEditDto
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.editUser(id, userEditDto))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<UserDto> {
        return ResponseEntity.ok(userService.deleteUser(id))
    }

}
