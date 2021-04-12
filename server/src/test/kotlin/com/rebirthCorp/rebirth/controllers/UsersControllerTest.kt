package com.rebirthCorp.rebirth.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.rebirthCorp.rebirth.RebirthApplication
import com.rebirthCorp.rebirth.models.TestUser
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.services.UserService
import com.rebirthCorp.rebirth.services.authentication.JwtAuthenticationFilter
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.*

@SpringBootTest
@WebAppConfiguration
@ContextConfiguration(classes = [RebirthApplication::class])
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
internal class UsersControllerTest(@Autowired val service: UserService, @Autowired val mockMvc: MockMvc) {

    var createdUser = TestUser("chicha")
    var secondUser = TestUser("hi")
    //var mockMvc: MockMvc = MockMvcBuilders.webAppContextSetup(wac).apply { springSecurity() }.build()
    val om = ObjectMapper()
    var jwt: String? = null


    @BeforeAll
    internal fun setUp() {
        // fixme::cold remove need of testuser
        val user = TestUser("-1", "TestDamn", "testdamn@daniel.com", "damnDaniel")

        val damn = this.mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(user)
        }.andExpect {
            status { isCreated() }
        }.andReturn()
        this.om.readerForUpdating(secondUser).readValue<String>(damn.response.contentAsString)
    }

    @Test
    @Order(1)
    fun createUser() {
        val user = TestUser("-1", "Damn", "damn@daniel.com", "damnDaniel")

        val damn = this.mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(user)
        }.andExpect {
            status { isCreated() }
        }.andReturn()
        om.readerForUpdating(createdUser)
                .readValue<String>(damn.response.contentAsString)
        assert(!createdUser.hasVerifiedMail)
        // the server is the source of truth for IDs
        assert(createdUser.id != user.id)
        assert(createdUser.updatedAt == null)
        assert(createdUser.username == user.username)
        assert(createdUser.email == user.email)
        this.jwt = "Bearer " + JwtAuthenticationFilter.generateJWT(this.createdUser.username,
                this.createdUser.id)
    }

    @Test
    @Order(2)
    fun getUser() {
        var fetchedUser = TestUser()

        //Get ourself
        var resp = this.mockMvc.get("/users/${this.createdUser.id}")
        {
            header("Authorization", jwt!!)
        }.andExpect { status { isOk() } }.andReturn()

        fetchedUser = this.om.readerForUpdating(fetchedUser)
                .readValue(resp.response.contentAsString)
        assert(fetchedUser.id == this.createdUser.id)

        //Get another user
        resp = this.mockMvc.get("/users/${this.secondUser.id}")
        {
            header("Authorization", jwt!!)
        }.andExpect { status { isOk() } }.andReturn()

        val secondFetched = TestUser()
        this.om.readerForUpdating(secondFetched)
                .readValue<String>(resp.response.contentAsString)
        assert(secondFetched.id == this.secondUser.id)
        assert(secondFetched.email.isEmpty())
        assert(secondFetched.updatedAt == null)
    }

    @Test
    @Order(3)
    fun patchUser() {
        val patchString = """{"username": "damnTest"}"""
        val updated = TestUser()

        //Our user
        val res = this.mockMvc.patch("/users/${this.createdUser.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = patchString
            header("Authorization", jwt!!)
        }.andExpect { status { isOk() } }.andReturn()
        this.om.readerForUpdating(updated)
                .readValue<String>(res.response.contentAsString)
        assert(updated.username == "damnTest")

        //Other user
        this.mockMvc.patch("/users/${this.secondUser.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = patchString
            header("Authorization", jwt!!)
        }.andExpect { status { isUnauthorized() } }
    }

    @Test
    @Order(4)
    fun `create user that already exists`() {
        val user = TestUser("-1", "Damn", "damn@daniel.com", "damnDaniel")

        this.mockMvc.post("/users") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(user)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    @Order(5)
    fun deleteUser() {
        //Our user
        this.mockMvc.delete("/users/${this.createdUser.id}") {
            header("Authorization", jwt!!)
        }.andExpect { status { isOk() } }.andDo {
            assert(!service.isUserReserved(UserModel(createdUser.username, createdUser.email)))
        }

        //Other user
        this.mockMvc.delete("/users/${this.secondUser.id}") {
            header("Authorization", jwt!!)
        }.andExpect { status { isUnauthorized() } }
    }

    @AfterAll
    internal fun tearDown() {
        // you cannot remove a row that has already been deleted
        this.service.removeUserFromApp(this.secondUser.id)
    }
}
