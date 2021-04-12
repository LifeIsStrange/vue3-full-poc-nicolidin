package com.rebirthCorp.rebirth.config

import com.rebirthCorp.rebirth.repositories.UserRepository
import com.rebirthCorp.rebirth.services.UserService
import com.rebirthCorp.rebirth.services.authentication.JwtAuthenticationFilter
import com.rebirthCorp.rebirth.services.authentication.JwtAuthorizationFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
        @Autowired val userService: UserService,
        @Autowired val userRepository: UserRepository
) : WebSecurityConfigurerAdapter() {
    val encoder = SCryptPasswordEncoder()

    override fun configure(http: HttpSecurity?) {
        http?.cors()?.and()?.csrf()?.disable()?.authorizeRequests()
                ?.antMatchers(HttpMethod.POST, "/users")?.permitAll()
                ?.antMatchers(HttpMethod.GET, "/articles/**")?.permitAll()
                ?.anyRequest()?.authenticated()
                ?.and()
                ?.addFilter(JwtAuthenticationFilter(this.authenticationManager(), this.userRepository))
                ?.addFilter(JwtAuthorizationFilter(this.authenticationManager()))
                ?.sessionManagement()?.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.userDetailsService(this.userService)?.passwordEncoder(this.encoder)
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()

        source.registerCorsConfiguration("/**", CorsConfiguration().applyPermitDefaultValues())
        return source
    }

}