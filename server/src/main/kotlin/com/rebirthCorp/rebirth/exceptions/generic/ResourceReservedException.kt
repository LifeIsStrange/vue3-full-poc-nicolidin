package com.rebirthCorp.rebirth.exceptions.generic

import org.springframework.http.HttpStatus

data class ResourceReservedException(private val resource: Any, private val reason: String = "")
    : BaseException("The resource ${resource.javaClass.kotlin.simpleName}, already exists",
        ResourceReservedException::toString.toString(),
        HttpStatus.CONFLICT,
        reason
)