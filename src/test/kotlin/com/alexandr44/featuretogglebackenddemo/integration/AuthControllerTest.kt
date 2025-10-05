package com.alexandr44.featuretogglebackenddemo.integration

import com.alexandr44.featuretogglebackenddemo.dto.*
import com.alexandr44.featuretogglebackenddemo.enums.UserRole
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest(
    val mockMvc: MockMvc,
    val userRepository: UserRepository,
    val objectMapper: ObjectMapper,
    val passwordEncoder: PasswordEncoder
) {

    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll(
            userRepository.findAll()
                .filter { it.username != "admin" }
        )
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test register user - success`() {
        val registrationRequest = RegistrationRequest(
            username = "user",
            password = "password",
            role = UserRole.USER
        )
        assertTrue(userRepository.findByUsername(registrationRequest.username).isEmpty)

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val registrationResponse: RegistrationResponse =
            objectMapper.readValue(responseStr, RegistrationResponse::class.java)
        val savedUser = userRepository.findByUsername(registrationResponse.username).get()

        assertEquals(registrationRequest.role, registrationResponse.role)
        assertEquals(registrationRequest.username, registrationResponse.username)
        assertEquals(savedUser.id, registrationResponse.id)

        assertEquals(registrationRequest.role, savedUser.role)
        assertEquals(registrationRequest.username, savedUser.username)
        assertTrue(passwordEncoder.matches(registrationRequest.password, savedUser.password))
    }

    @Test
    fun `Test register user - no auth`() {
        val registrationRequest = RegistrationRequest(
            username = "user",
            password = "password",
            role = UserRole.USER
        )
        assertTrue(userRepository.findByUsername(registrationRequest.username).isEmpty)

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }

        assertTrue(userRepository.findByUsername(registrationRequest.username).isEmpty)
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test register user - not admin`() {
        val registrationRequest = RegistrationRequest(
            username = "user",
            password = "password",
            role = UserRole.USER
        )
        assertTrue(userRepository.findByUsername(registrationRequest.username).isEmpty)

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }

        assertTrue(userRepository.findByUsername(registrationRequest.username).isEmpty)
    }

    @Test
    @Sql("/sql/users.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test register user - user already exists`() {
        val registrationRequest = RegistrationRequest(
            username = "user1",
            password = "password",
            role = UserRole.USER
        )
        assertTrue(userRepository.findByUsername(registrationRequest.username).isPresent)

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Username already taken", errorDto.message)
        assertEquals("POST /api/v1/auth/register", errorDto.path)
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDto.code)
    }

    @Test
    fun `Test login - success`() {
        val authorizationRequest = AuthorizationRequest(
            username = "admin",
            password = "admin"
        )
        assertTrue { userRepository.findByUsername(authorizationRequest.username).isPresent }

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val authorizationResponse: AuthorizationResponse =
            objectMapper.readValue(responseStr, AuthorizationResponse::class.java)

        assertEquals(authorizationRequest.username, authorizationResponse.username)
        assertNotNull(authorizationResponse.token)
    }

    @Test
    @Sql("/sql/users.sql")
    fun `Test login - success by user`() {
        val authorizationRequest = AuthorizationRequest(
            username = "user1",
            password = "pass1"
        )
        assertTrue { userRepository.findByUsername(authorizationRequest.username).isPresent }

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val authorizationResponse: AuthorizationResponse =
            objectMapper.readValue(responseStr, AuthorizationResponse::class.java)

        assertEquals(authorizationRequest.username, authorizationResponse.username)
        assertNotNull(authorizationResponse.token)
    }

    @Test
    fun `Test login - no such user`() {
        val authorizationRequest = AuthorizationRequest(
            username = "admin-123",
            password = "admin"
        )
        assertTrue { userRepository.findByUsername(authorizationRequest.username).isEmpty }

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.UNAUTHORIZED.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Bad credentials", errorDto.message)
        assertEquals("POST /api/v1/auth/login", errorDto.path)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDto.code)
    }

    @Test
    fun `Test login - wrong pass`() {
        val authorizationRequest = AuthorizationRequest(
            username = "admin",
            password = "admin-123"
        )
        assertTrue { userRepository.findByUsername(authorizationRequest.username).isPresent }

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.UNAUTHORIZED.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Bad credentials", errorDto.message)
        assertEquals("POST /api/v1/auth/login", errorDto.path)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDto.code)
    }

    @Test
    @Sql("/sql/users.sql")
    fun `Test login - user not active`() {
        val authorizationRequest = AuthorizationRequest(
            username = "user1",
            password = "pass1"
        )
        assertTrue { userRepository.findByUsername(authorizationRequest.username).isPresent }
        userRepository.save(
            userRepository.findByUsername(authorizationRequest.username).get().apply {
                this.active = false
            }
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizationRequest))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.UNAUTHORIZED.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Bad credentials", errorDto.message)
        assertEquals("POST /api/v1/auth/login", errorDto.path)
        assertEquals(HttpStatus.UNAUTHORIZED.value(), errorDto.code)
    }

}