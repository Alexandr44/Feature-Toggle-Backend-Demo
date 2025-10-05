package com.alexandr44.featuretogglebackenddemo.integration

import com.alexandr44.featuretogglebackenddemo.dto.ErrorDto
import com.alexandr44.featuretogglebackenddemo.dto.UserDto
import com.alexandr44.featuretogglebackenddemo.dto.UserEditDto
import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import com.alexandr44.featuretogglebackenddemo.mapper.UserMapper
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest(
    val mockMvc: MockMvc,
    val userRepository: UserRepository,
    val userMapper: UserMapper,
    val objectMapper: ObjectMapper,
    val passwordEncoder: PasswordEncoder
) {

    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll()
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test get all users - success`() {
        val expectedUsers = userRepository.findAll().toList()
        val expectedUsersDto = userMapper.map(expectedUsers)

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/users")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val users: List<UserDto> = objectMapper.readValue(responseStr, object : TypeReference<List<UserDto>>() {})

        assertEquals(3, users.size)
        assertEquals(1, users.filter { it.role == UserRole.ADMIN }.size)
        assertEquals(2, users.filter { it.role == UserRole.USER }.size)
        assertContentEquals(expectedUsersDto, users)
    }

    @Test
    @Sql("/sql/users.sql")
    fun `Test get all users - no auth`() {
        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/users")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test edit user - success`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")
        val userEditDto = UserEditDto(
            username = "user3",
            password = "pass3",
            role = UserRole.ADMIN,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEditDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val user: UserDto = objectMapper.readValue(responseStr, UserDto::class.java)

        assertEquals(userEditDto.username, user.username)
        assertEquals(userEditDto.role, user.role)
        assertEquals(userEditDto.active, user.active)

        val savedUser = userRepository.findById(userId).get()
        assertTrue { passwordEncoder.matches(userEditDto.password, savedUser.password) }
    }

    @Test
    @Sql("/sql/users.sql")
    fun `Test edit user - no auth`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")
        val userEditDto = UserEditDto(
            username = "user3",
            password = "pass3",
            role = UserRole.ADMIN,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEditDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["USER"])
    fun `Test edit user - not admin`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")
        val userEditDto = UserEditDto(
            username = "user3",
            password = "pass3",
            role = UserRole.ADMIN,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEditDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test edit user - no such user`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")
        val userEditDto = UserEditDto(
            username = "user3",
            password = "pass3",
            role = UserRole.ADMIN,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/users/$userId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userEditDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("User with id $userId does not exist", errorDto.message)
        assertEquals("PUT /api/v1/users/$userId", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test delete user - success`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/users/$userId")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val user: UserDto = objectMapper.readValue(responseStr, UserDto::class.java)
        assertFalse { user.active }

        val savedUser = userRepository.findById(userId).get()
        assertFalse { savedUser.active }
    }

    @Test
    @Sql("/sql/users.sql")
    fun `Test delete user - no auth`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/users/$userId")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["USER"])
    fun `Test delete user - not admin`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/users/$userId")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test delete user - no such user`() {
        val userId = UUID.fromString("3945ebed-cf9e-496f-acff-47a90cd20bec")

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/users/$userId")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("User with id $userId does not exist", errorDto.message)
        assertEquals("DELETE /api/v1/users/$userId", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)
    }
}