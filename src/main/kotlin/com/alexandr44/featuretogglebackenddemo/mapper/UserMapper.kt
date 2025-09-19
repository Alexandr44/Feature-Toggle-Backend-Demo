package com.alexandr44.featuretogglebackenddemo.mapper

import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.dto.UserEditDto
import com.alexandr44.featuretogglebackenddemo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface UserMapper {

    fun map(user: User): UserDto

    fun map(users: List<User>): List<UserDto>

    @Mapping(target = "password", ignore = true)
    fun update(@MappingTarget user: User, dto: UserEditDto)

}
