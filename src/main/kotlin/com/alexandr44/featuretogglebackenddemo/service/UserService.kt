package com.alexandr44.featuretogglebackenddemo.service

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.mapper.UserMapper
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userMapper: UserMapper,
    private val userRepository: UserRepository
) {

    fun getAll(): List<UserDto> = userMapper.map(userRepository.findAll())

}