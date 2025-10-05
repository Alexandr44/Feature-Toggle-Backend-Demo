package com.alexandr44.featuretogglebackenddemo.integration

import com.alexandr44.featuretogglebackenddemo.dto.ErrorDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagDto
import com.alexandr44.featuretogglebackenddemo.dto.FeatureFlagInputDto
import com.alexandr44.featuretogglebackenddemo.entity.FeatureFlag
import com.alexandr44.featuretogglebackenddemo.enums.AuditAction
import com.alexandr44.featuretogglebackenddemo.mapper.FeatureFlagMapper
import com.alexandr44.featuretogglebackenddemo.repository.AuditLogRepository
import com.alexandr44.featuretogglebackenddemo.repository.FeatureFlagRepository
import com.alexandr44.featuretogglebackenddemo.repository.UserRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FeatureFlagControllerTest(
    val mockMvc: MockMvc,
    val featureFlagRepository: FeatureFlagRepository,
    val featureFlagMapper: FeatureFlagMapper,
    val auditLogRepository: AuditLogRepository,
    val userRepository: UserRepository,
    val objectMapper: ObjectMapper
) {

    @AfterEach
    fun cleanUp() {
        featureFlagRepository.deleteAll()
        auditLogRepository.deleteAll()
        userRepository.deleteAll(
            userRepository.findAll()
                .filter { it.username != "admin" }
        )
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test get all feature flags - success`() {
        val expectedFeatureFlags = featureFlagRepository.findAll().toList()
        val expectedFeatureFlagsDto = featureFlagMapper.map(expectedFeatureFlags)

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlags: List<FeatureFlagDto> =
            objectMapper.readValue(responseStr, object : TypeReference<List<FeatureFlagDto>>() {})

        assertEquals(5, featureFlags.size)
        assertEquals(3, featureFlags.filter { it.tag == "Sport" }.size)
        assertEquals(2, featureFlags.filter { it.tag == "Toys" }.size)
        assertContentEquals(expectedFeatureFlagsDto, featureFlags)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test get all feature flags - no auth`() {
        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test get all feature flags - success by tag`() {
        val expectedFeatureFlags = featureFlagRepository.findAll().toList()
        val expectedFeatureFlagsDto = featureFlagMapper.map(expectedFeatureFlags)
            .filter { it.tag == "Sport" }

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags")
                .param("tag", "Sport")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlags: List<FeatureFlagDto> =
            objectMapper.readValue(responseStr, object : TypeReference<List<FeatureFlagDto>>() {})

        assertEquals(3, featureFlags.size)
        assertEquals(3, featureFlags.filter { it.tag == "Sport" }.size)
        assertContentEquals(expectedFeatureFlagsDto, featureFlags)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test get feature flag by key - success`() {
        val featureFlagKey = "feature-test"
        val expectedFeatureFlag = featureFlagRepository.findByKeyAndActiveIsTrue(featureFlagKey).get()
        val expectedFeatureFlagDto = featureFlagMapper.map(expectedFeatureFlag)

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags/$featureFlagKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)

        assertEquals(expectedFeatureFlagDto, featureFlag)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test get feature flag by key - no auth`() {
        val featureFlagKey = "feature-test"

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags/$featureFlagKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test get feature flag by key - not found`() {
        val featureFlagKey = "feature-test"

        val mvcResult: MvcResult = mockMvc.perform(
            get("/api/v1/feature-flags/$featureFlagKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureFlagKey is not found or is not active", errorDto.message)
        assertEquals("GET /api/v1/feature-flags/$featureFlagKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test add feature flag - success`() {
        assertTrue { featureFlagRepository.findAll().isEmpty() }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = "feature-test",
            name = "Sport boots",
            tag = "Sport",
            description = "A sport feature flag",
            value = true,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)
        val savedFeatureFlag = featureFlagRepository.findByKey(featureFlagInputDto.key!!).get()

        assertEquals(featureFlagInputDto.key, featureFlag.key)
        assertEquals(featureFlagInputDto.name, featureFlag.name)
        assertEquals(featureFlagInputDto.tag, featureFlag.tag)
        assertEquals(featureFlagInputDto.description, featureFlag.description)
        assertEquals(featureFlagInputDto.value, featureFlag.value)
        assertEquals(featureFlagInputDto.active, featureFlag.active)

        assertEquals(featureFlagInputDto.key, savedFeatureFlag.key)
        assertEquals(featureFlagInputDto.name, savedFeatureFlag.name)
        assertEquals(featureFlagInputDto.tag, savedFeatureFlag.tag)
        assertEquals(featureFlagInputDto.description, savedFeatureFlag.description)
        assertEquals(featureFlagInputDto.value, savedFeatureFlag.value)
        assertEquals(featureFlagInputDto.active, savedFeatureFlag.active)

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.CREATE, auditLog.action)
        assertEquals(null, auditLog.oldValue)
        assertEquals("admin", auditLog.changedByName)
        assertEquals(savedFeatureFlag.id.toString(), auditLog.entityId)

        val auditFeatureFlag = objectMapper.readValue(auditLog.newValue, FeatureFlag::class.java)
        assertEquals(savedFeatureFlag.id, auditFeatureFlag.id)
        assertEquals(savedFeatureFlag.key, auditFeatureFlag.key)
        assertEquals(savedFeatureFlag.name, auditFeatureFlag.name)
        assertEquals(savedFeatureFlag.tag, auditFeatureFlag.tag)
        assertEquals(savedFeatureFlag.description, auditFeatureFlag.description)
        assertEquals(savedFeatureFlag.value, auditFeatureFlag.value)
        assertEquals(savedFeatureFlag.active, auditFeatureFlag.active)
    }

    @Test
    fun `Test add feature flag - no auth`() {
        assertTrue { featureFlagRepository.findAll().isEmpty() }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = "feature-test",
            name = "Sport boots",
            tag = "Sport",
            description = "A sport feature flag",
            value = true,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { featureFlagRepository.findAll().isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test add feature flag - not admin`() {
        assertTrue { featureFlagRepository.findAll().isEmpty() }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = "feature-test",
            name = "Sport boots",
            tag = "Sport",
            description = "A sport feature flag",
            value = true,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { featureFlagRepository.findAll().isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test add feature flag - no feature key`() {
        assertTrue { featureFlagRepository.findAll().isEmpty() }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = null,
            name = "Sport boots",
            tag = "Sport",
            description = "A sport feature flag",
            value = true,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag key is missing", errorDto.message)
        assertEquals("POST /api/v1/feature-flags", errorDto.path)
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDto.code)

        assertTrue { featureFlagRepository.findAll().isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test add feature flag - feature key already exists`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport boots",
            tag = "Sport",
            description = "A sport feature flag",
            value = true,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            post("/api/v1/feature-flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.BAD_REQUEST.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature key $featureKey already taken", errorDto.message)
        assertEquals("POST /api/v1/feature-flags", errorDto.path)
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test edit feature flag - success`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport equipment",
            tag = "Sport",
            description = "A new sport feature flag",
            value = false,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/$featureKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)
        val savedFeatureFlag = featureFlagRepository.findByKey(featureFlagInputDto.key!!).get()

        assertEquals(featureFlagInputDto.key, featureFlag.key)
        assertEquals(featureFlagInputDto.name, featureFlag.name)
        assertEquals(featureFlagInputDto.tag, featureFlag.tag)
        assertEquals(featureFlagInputDto.description, featureFlag.description)
        assertEquals(featureFlagInputDto.value, featureFlag.value)
        assertEquals(featureFlagInputDto.active, featureFlag.active)

        assertEquals(featureFlagInputDto.key, savedFeatureFlag.key)
        assertEquals(featureFlagInputDto.name, savedFeatureFlag.name)
        assertEquals(featureFlagInputDto.tag, savedFeatureFlag.tag)
        assertEquals(featureFlagInputDto.description, savedFeatureFlag.description)
        assertEquals(featureFlagInputDto.value, savedFeatureFlag.value)
        assertEquals(featureFlagInputDto.active, savedFeatureFlag.active)

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.UPDATE, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("admin", auditLog.changedByName)
        assertEquals(savedFeatureFlag.id.toString(), auditLog.entityId)

        val auditFeatureFlag = objectMapper.readValue(auditLog.newValue, FeatureFlag::class.java)
        assertEquals(savedFeatureFlag.id, auditFeatureFlag.id)
        assertEquals(savedFeatureFlag.key, auditFeatureFlag.key)
        assertEquals(savedFeatureFlag.name, auditFeatureFlag.name)
        assertEquals(savedFeatureFlag.tag, auditFeatureFlag.tag)
        assertEquals(savedFeatureFlag.description, auditFeatureFlag.description)
        assertEquals(savedFeatureFlag.value, auditFeatureFlag.value)
        assertEquals(savedFeatureFlag.active, auditFeatureFlag.active)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test edit feature flag - no auth`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport equipment",
            tag = "Sport",
            description = "A new sport feature flag",
            value = false,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/$featureKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test edit feature flag - not admin`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport equipment",
            tag = "Sport",
            description = "A new sport feature flag",
            value = false,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/$featureKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test edit feature flag - no feature flag key`() {
        val featureKey = "feature-test-not-exist"
        assertFalse { featureFlagRepository.findByKey(featureKey).isPresent }

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport equipment",
            tag = "Sport",
            description = "A new sport feature flag",
            value = false,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/$featureKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("PUT /api/v1/feature-flags/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test edit feature flag - feature flag not active`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        featureFlagRepository.save(
            featureFlagRepository.findByKey(featureKey).get().apply {
                active = false
            }
        )

        val featureFlagInputDto = FeatureFlagInputDto(
            key = featureKey,
            name = "Sport equipment",
            tag = "Sport",
            description = "A new sport feature flag",
            value = false,
            active = true
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/$featureKey")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(featureFlagInputDto))
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("PUT /api/v1/feature-flags/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test delete feature flag - success`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().active }

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/feature-flags/$featureKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)
        val savedFeatureFlag = featureFlagRepository.findByKey(featureKey).get()

        assertFalse(savedFeatureFlag.active)

        assertEquals(featureFlag.key, featureFlag.key)
        assertEquals(featureFlag.name, featureFlag.name)
        assertEquals(featureFlag.tag, featureFlag.tag)
        assertEquals(featureFlag.description, featureFlag.description)
        assertEquals(featureFlag.value, featureFlag.value)
        assertEquals(featureFlag.active, featureFlag.active)

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.DELETE, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("admin", auditLog.changedByName)
        assertEquals(savedFeatureFlag.id.toString(), auditLog.entityId)

        val auditFeatureFlag = objectMapper.readValue(auditLog.newValue, FeatureFlag::class.java)
        assertEquals(savedFeatureFlag.id, auditFeatureFlag.id)
        assertEquals(savedFeatureFlag.key, auditFeatureFlag.key)
        assertEquals(savedFeatureFlag.name, auditFeatureFlag.name)
        assertEquals(savedFeatureFlag.tag, auditFeatureFlag.tag)
        assertEquals(savedFeatureFlag.description, auditFeatureFlag.description)
        assertEquals(savedFeatureFlag.value, auditFeatureFlag.value)
        assertEquals(savedFeatureFlag.active, auditFeatureFlag.active)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test delete feature flag - no auth`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().active }

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/feature-flags/$featureKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "user", roles = ["USER"])
    fun `Test delete feature flag - not admin`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().active }

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/feature-flags/$featureKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test delete feature flag - no feature flag key`() {
        val featureKey = "feature-test-not-exist"
        assertFalse { featureFlagRepository.findByKey(featureKey).isPresent }

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/feature-flags/$featureKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("DELETE /api/v1/feature-flags/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test delete feature flag - feature flag not active`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        featureFlagRepository.save(
            featureFlagRepository.findByKey(featureKey).get().apply {
                active = false
            }
        )

        val mvcResult: MvcResult = mockMvc.perform(
            delete("/api/v1/feature-flags/$featureKey")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("DELETE /api/v1/feature-flags/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test toggle feature flag - success`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().value }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/$featureKey")
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)
        val savedFeatureFlag = featureFlagRepository.findByKey(featureKey).get()

        assertFalse(savedFeatureFlag.value)
        assertFalse(featureFlag.value)

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.TOGGLE, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("admin", auditLog.changedByName)
        assertEquals(savedFeatureFlag.id.toString(), auditLog.entityId)
    }

    @Test
    @Sql(
        "/sql/feature_flags.sql",
        "/sql/users.sql"
    )
    @WithMockUser(username = "user1", roles = ["USER"])
    fun `Test toggle feature flag - success by user`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().value }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/$featureKey")
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlag: FeatureFlagDto = objectMapper.readValue(responseStr, FeatureFlagDto::class.java)
        val savedFeatureFlag = featureFlagRepository.findByKey(featureKey).get()

        assertFalse(savedFeatureFlag.value)
        assertFalse(featureFlag.value)

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.TOGGLE, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("user1", auditLog.changedByName)
        assertEquals(savedFeatureFlag.id.toString(), auditLog.entityId)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test toggle feature flag - no auth`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }
        assertTrue { featureFlagRepository.findByKey(featureKey).get().value }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/$featureKey")
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test toggle feature flag - no feature flag key`() {
        val featureKey = "feature-test"
        assertFalse { featureFlagRepository.findByKey(featureKey).isPresent }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/$featureKey")
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("PUT /api/v1/feature-flags/toggle/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test toggle feature flag - feature flag not active`() {
        val featureKey = "feature-test"
        assertTrue { featureFlagRepository.findByKey(featureKey).isPresent }

        featureFlagRepository.save(
            featureFlagRepository.findByKey(featureKey).get().apply {
                active = false
            }
        )

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/$featureKey")
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.NOT_FOUND.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val errorDto: ErrorDto = objectMapper.readValue(responseStr, ErrorDto::class.java)

        assertEquals("Feature flag with key $featureKey is not found or is not active", errorDto.message)
        assertEquals("PUT /api/v1/feature-flags/toggle/$featureKey", errorDto.path)
        assertEquals(HttpStatus.NOT_FOUND.value(), errorDto.code)

        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `Test toggle tag feature flag - success`() {
        val featureTag = "Sport"
        featureFlagRepository.findAllByTagAndActiveIsTrue(featureTag)
            .apply {
                assertEquals(3, this.size)
                this.forEach {
                    assertTrue { it.value }
                }
            }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/tag")
                .param("tag", featureTag)
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlags: List<FeatureFlagDto> =
            objectMapper.readValue(responseStr, object : TypeReference<List<FeatureFlagDto>>() {})
        val savedFeatureFlags = featureFlagRepository.findAllByTagAndActiveIsTrue(featureTag)

        assertEquals(3, featureFlags.size)
        assertEquals(3, savedFeatureFlags.size)

        featureFlags.forEach {
            assertFalse { it.value }
        }
        savedFeatureFlags.forEach {
            assertFalse { it.value }
        }

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.TOGGLE_BY_TAG, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("admin", auditLog.changedByName)
    }

    @Test
    @Sql(
        "/sql/feature_flags.sql",
        "/sql/users.sql"
    )
    @WithMockUser(username = "user1", roles = ["USER"])
    fun `Test toggle tag feature flag - success by user`() {
        val featureTag = "Sport"
        featureFlagRepository.findAllByTagAndActiveIsTrue(featureTag)
            .apply {
                assertEquals(3, this.size)
                this.forEach {
                    assertTrue { it.value }
                }
            }

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/tag")
                .param("tag", featureTag)
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        val featureFlags: List<FeatureFlagDto> =
            objectMapper.readValue(responseStr, object : TypeReference<List<FeatureFlagDto>>() {})
        val savedFeatureFlags = featureFlagRepository.findAllByTagAndActiveIsTrue(featureTag)

        assertEquals(3, featureFlags.size)
        assertEquals(3, savedFeatureFlags.size)

        featureFlags.forEach {
            assertFalse { it.value }
        }
        savedFeatureFlags.forEach {
            assertFalse { it.value }
        }

        val auditLog = auditLogRepository.findAll().first()
        assertEquals(FeatureFlag.AUDIT_TYPE, auditLog.entityType)
        assertEquals(AuditAction.TOGGLE_BY_TAG, auditLog.action)
        assertNotNull(auditLog.oldValue)
        assertEquals("user1", auditLog.changedByName)
    }

    @Test
    @Sql("/sql/feature_flags.sql")
    fun `Test toggle tag feature flag - no auth`() {
        val featureTag = "Sport"

        val mvcResult: MvcResult = mockMvc.perform(
            put("/api/v1/feature-flags/toggle/tag")
                .param("tag", featureTag)
                .param("value", "false")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().`is`(HttpStatus.FORBIDDEN.value()))
            .andReturn()

        val responseStr = mvcResult.response.contentAsString
        assertTrue { responseStr.isEmpty() }
        assertTrue { auditLogRepository.findAll().isEmpty() }
    }

}