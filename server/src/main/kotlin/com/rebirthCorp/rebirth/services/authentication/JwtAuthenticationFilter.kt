package com.rebirthCorp.rebirth.services.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.repositories.UserRepository
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.EXPIRATION_TIME
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.HEADER_STRING
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.TOKEN_PREFIX
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
    val authManager: AuthenticationManager,
    val userRepo: UserRepository
) : UsernamePasswordAuthenticationFilter() {

    override fun attemptAuthentication(request: HttpServletRequest?, response: HttpServletResponse?): Authentication {
        val creds = ObjectMapper().readValue(request?.inputStream, UserModel::class.java)

        return this.authManager.authenticate(UsernamePasswordAuthenticationToken(creds.username, creds.password))
    }

    override fun successfulAuthentication(request: HttpServletRequest?, response: HttpServletResponse?, chain: FilterChain?, authResult: Authentication?) {
        val username = (authResult?.principal as UserDetails).username
        val user = this.userRepo.findByUsername(username)

        response?.addHeader(HEADER_STRING, TOKEN_PREFIX + generateJWT(username,
            user!!.id))
        response?.addHeader("Access-Control-Expose-Headers", HEADER_STRING)
    }

    // fixme why the differences https://github.com/gregwhitaker/springboot-rsocketjwt-example/blob/master/token-generator/src/main/java/example/token/BearerTokenGenerator.java
    companion object {
        fun generateJWT(username: String, id: String): String {
            return JWT.create()
                .withSubject(username)
                .withClaim("userId", id)
                .withExpiresAt(Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(AuthenticationConfig.SECRET)) // should be RSA

            // why public key https://www.codota.com/code/java/methods/com.auth0.jwt.algorithms.Algorithm/RSA512?snippet=5ce6e297e59467000404fe11
        }
    }
}