package com.rebirthCorp.rebirth.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.rebirthCorp.rebirth.exceptions.user.UserCreationException
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.repositories.UserRepository
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FeatureSpec
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.*
import org.springframework.security.core.userdetails.UsernameNotFoundException

@SpringBootTest
@ObsoleteCoroutinesApi
internal class UserServiceTest : FeatureSpec() {

    private val mock: UserRepository
    private val service: UserService
    private val mockJdbcTemplate: JdbcTemplate = mock {}

    init {
        this.mock = mock {
            on { save(any<UserModel>()) } doReturn VALID_USER
            on { findByUsername("InvalidUser") } doThrow UsernameNotFoundException("Requested user not found")
            on { findByUsername("ExistingUser") } doReturn VALID_USER
        }
        this.service = UserService(this.mock, jdbcTemplate = mockJdbcTemplate)

        feature("Create a user") {
            scenario("Save new valid user") {
                service.saveNewUser(VALID_USER) shouldBe VALID_USER
            }

            scenario("Send an invalid mail") {
                for (invalidMail in INVALID_MAILS) {
                    shouldThrow<UserCreationException> {
                        service.validateUser(UserModel("Damn", invalidMail))
                    }
                }
            }
        }

        feature("Search a user") {
            scenario("User exists") {
                service.loadUserByUsername("ExistingUser") shouldBe User(VALID_USER.username, VALID_USER.password, emptyList())
            }

            scenario("User don't exists") {
                shouldThrow<UsernameNotFoundException> {
                    service.loadUserByUsername("InvalidUser")
                }
            }
        }

        feature("Patch a User") {
            scenario("Valid user metadata patch") {
                val old = VALID_USER.clone()
                val patch = """{"password":"wowDaniel","username":"wow"}"""

                service.patchUser(old, patch).let {
                    it.username shouldBe "wow"
                    it.password shouldNotBe VALID_USER.password
                }
            }

            scenario("Try to modify server handled properties") {
                val baseUser = VALID_USER.clone()
                val patch = """{"password":"wowDaniel","username":"wow", "has_verified_mail": true}"""

                service.patchUser(baseUser, patch).let {
                    it.username shouldBe "wow"
                    it.password shouldNotBe VALID_USER.password
                    it.hasVerifiedMail shouldBe false
                }
            }

            scenario("User changes his password") {
                val baseUser = VALID_USER.clone()
                val patch = """{"password":"wowDaniel"}"""
                val patched = service.patchUser(baseUser, patch)
                baseUser.password = service.hashPassword("wowDaniel")
                patched shouldBe baseUser
            }
        }
    }

    companion object {
        val VALID_USER = UserModel("Damn", "damn@daniel.wow", "damnDaniel")
        val INVALID_MAILS = arrayOf("damn@daniel", "damn.wow", "woooooww:./&@daniel.com", "damn@daniel@daniel.wow")
    }
}
