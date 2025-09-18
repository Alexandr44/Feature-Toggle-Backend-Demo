package com.alexandr44.featuretogglebackenddemo.mapper

import com.alexandr44.featuretogglebackenddemo.dto.AuthorizationResponse
import com.alexandr44.featuretogglebackenddemo.dto.RegistrationResponse
import com.alexandr44.featuretogglebackenddemo.dto.Token
import com.alexandr44.featuretogglebackenddemo.entity.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface AuthMapper {

    fun map(user: User): RegistrationResponse

    @Mapping(target = "token", source = "token.token")
    @Mapping(target = "expiresAt", expression = "java(token.getExpiresAt().toInstant())")
    fun map(username: String, token: Token): AuthorizationResponse

}
