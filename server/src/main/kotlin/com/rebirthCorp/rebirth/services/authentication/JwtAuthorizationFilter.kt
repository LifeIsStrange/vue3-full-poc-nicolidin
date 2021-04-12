package com.rebirthCorp.rebirth.services.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.HEADER_STRING
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.SECRET
import com.rebirthCorp.rebirth.services.authentication.AuthenticationConfig.TOKEN_PREFIX
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthorizationFilter(authManager: AuthenticationManager) : BasicAuthenticationFilter(authManager) {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val header = request.getHeader(HEADER_STRING)

        //if (header?.startsWith(TOKEN_PREFIX) == false) {
        //    chain.doFilter(request, response)
        //}
        val auth = this.getAuthentication(request)
        SecurityContextHolder.getContext().authentication = auth
        chain.doFilter(request, response)
    }

    private fun getAuthentication(req: HttpServletRequest): UsernamePasswordAuthenticationToken? {
        val token = req.getHeader(HEADER_STRING)

        if (token != null) {
            val user = getUserFromJwt(token)
            if (user != null) {
                return UsernamePasswordAuthenticationToken(user, null, emptyList())
            }
        }
        return null
    }

    companion object {
        fun getUserFromJwt(jwt: String): UserModel? {
            val token = JWT.require(Algorithm.HMAC512(SECRET))
                .build()
                .verify(jwt.replace(TOKEN_PREFIX, ""))
            val userId = token.claims["userId"]?.asString()
            val username = token.subject
            if (userId != null && username != null) {
                val user = UserModel(username= username)
                user.id = userId
                return user
            }
            return null
        }
    }
}