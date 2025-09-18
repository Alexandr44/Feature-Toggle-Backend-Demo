package com.alexandr44.featuretogglebackenddemo.mapper

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.entity.User
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserMapper {

    fun map(user: User): UserDto

    fun map(users: List<User>): List<UserDto>

}
