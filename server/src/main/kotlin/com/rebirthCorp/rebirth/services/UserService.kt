package com.rebirthCorp.rebirth.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.rebirthCorp.rebirth.exceptions.generic.ResourceReservedException
import com.rebirthCorp.rebirth.exceptions.generic.UnauthorizedException
import com.rebirthCorp.rebirth.exceptions.user.UserCreationException
import com.rebirthCorp.rebirth.exceptions.user.UserDoesNotExistException
import com.rebirthCorp.rebirth.models.AdminLevel
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.repositories.UserRepository
import com.rebirthCorp.rebirth.utils.ID
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*
import java.util.regex.Pattern

@Service
class UserService(@Autowired val userRepo: UserRepository,
                  @Autowired val jdbcTemplate: JdbcTemplate
) : UserDetailsService {
    private val emailRegexp = Pattern.compile(
        "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
    )
    private val passwordEncoder = SCryptPasswordEncoder()
    private val om = ObjectMapper()

    fun hashPassword(clearPassword: String): String {
        return this.passwordEncoder.encode(clearPassword)
    }

    fun throwIfUserDoesNotExists(userId: String) {
        if (!this.userRepo.existsById(userId)) {
            throw UserDoesNotExistException("corresponding user does not exists")
        }
    }

    fun isUserReserved(user: UserModel): Boolean {
        val isReserved = this.userRepo.countUserModelByUsernameEqualsOrEmailEquals(user.username, user.email) > 0
        return if (this.userRepo.existsById(user.id)) true else isReserved
    }

    fun validateUser(user: UserModel) {
        if (!this.emailRegexp.matcher(user.email).matches()) {
            throw UserCreationException("Invalid email address", user)
        }
        if (user.password.length < 5) {
            throw UserCreationException("Password length is too short", user)
        }
        if (user.username.length < 3) {
            throw UserCreationException("Username length is too short", user)
        }
    }

    fun saveNewUser(user: UserModel): UserModel {
        this.validateUser(user)
        if (this.isUserReserved(user)) {
            throw ResourceReservedException(user)
        }

        user.password = this.hashPassword(user.password)
        return this.userRepo.save(user)
    }

    override fun loadUserByUsername(username: String?): UserDetails {
        if (username != null) {
            val localUser = this.userRepo.findByUsername(username)
                ?: throw UsernameNotFoundException("Requested user $username not found.")
            return User(localUser.username, localUser.password, emptyList())
        }
        throw UsernameNotFoundException("No username provided")
    }

    fun removeUserFromApp(userId: String) {
        if (this.userRepo.existsById(userId)) {
            // todo delete subresources
            this.userRepo.deleteById(userId)
        }
    }

    fun patchUser(dest: UserModel, src: String): UserModel {
        val authorizedValues = arrayListOf("username", "email", "password")
        val json: Map<String, Any> = om.readValue(src)

        val patch = json.filter {
            authorizedValues.contains(it.key) && it.value is String
        }.mapValues {
            when (it.key) {
                "password" -> this.hashPassword(it.value as String)
                else -> it.value
            }
        }
        this.om.readerForUpdating(dest).readValue<String>(this.om.writeValueAsString(patch))
        return dest
    }

    fun getUser(id: String): UserModel? {
        return this.userRepo.findUserByID(id);
    }

    fun checkUserAdminLevel(id: String, roles: List<AdminLevel>): Boolean {
        val currentUser = this.getUser(id);
        if (roles.contains(currentUser?.adminLevel?: return false)) {
            return true;
        }
        return false;
    }
}
