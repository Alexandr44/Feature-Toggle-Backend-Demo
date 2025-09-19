package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.dto.UserEditDto
import com.alexandr44.featuretogglebackenddemo.mapper.UserMapper
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserService(
    private val userMapper: UserMapper,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    fun getAll(): List<UserDto> = userMapper.map(userRepository.findAll())

    @Transactional
    fun editUser(userId: UUID, userEditDto: UserEditDto): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User with id $userId does not exist") }
        userMapper.update(user, userEditDto)
        userEditDto.password?.let {
            user.password = passwordEncoder.encode(it)
        }
        return userMapper.map(user)
    }

    @Transactional
    fun deleteUser(userId: UUID): UserDto {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User with id $userId does not exist") }
            .apply {
                isActive = false
            }
        return userMapper.map(user)
    }

}