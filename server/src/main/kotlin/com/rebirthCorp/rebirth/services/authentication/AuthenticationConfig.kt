package com.rebirthCorp.rebirth.services.authentication

// fixme: secret && expiration_time
object AuthenticationConfig {
    const val SECRET = "damnSecret"
    const val EXPIRATION_TIME = 864_000_000L
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
}