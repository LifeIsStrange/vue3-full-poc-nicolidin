package com.rebirthCorp.rebirth.exceptions.generic

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.sql.Timestamp
import java.time.Instant

abstract class BaseException(message: String,
                             exceptionName: String,
                             val status: HttpStatus,
                             reason: String = "") : Exception(message) {
    val timestamp: Timestamp = Timestamp.from(Instant.now())
}

class ResourceNotFoundException(resourceOrMessage: Any) : BaseException(
        if (resourceOrMessage is String) {
            resourceOrMessage
        } else { "Resource ${resourceOrMessage.javaClass.kotlin.simpleName} Not Found" },
        ResourceReservedException::toString.toString(),
        HttpStatus.NOT_FOUND
)


class UnauthorizedException(message: String) : BaseException(message,
        UnauthorizedException::toString.toString(),
        HttpStatus.UNAUTHORIZED
)

class PaymentRequiredException(message: String) : BaseException(message,
        PaymentRequiredException::toString.toString(),
        HttpStatus.PAYMENT_REQUIRED
)

class NullRequestParam(message: String) : Exception(message)
class BadRequest(message: String) : Exception(message)
class Forbidden(message: String) : Exception(message)

// https://www.baeldung.com/global-error-handler-in-a-spring-rest-api

class ApiError {
    var status: HttpStatus
    var message: String
    var errors: List<String>

    constructor(status: HttpStatus, message: String, errors: List<String>) : super() {
        this.status = status
        this.message = message
        this.errors = errors
    }

    constructor(status: HttpStatus, message: String, error: String) : super() {
        this.status = status
        this.message = message
        errors = listOf(error)
    }
}

// should make @Valid annotations actually work
@ControllerAdvice
class CustomRestExceptionHandler : ResponseEntityExceptionHandler() {
    @Override
    fun handleMethodArgumentNotValid(
            ex: MethodArgumentNotValidException,
            headers: HttpHeaders,
            status: HttpStatus,
            request: WebRequest): ResponseEntity<Any> {
        val errors: MutableList<String> = mutableListOf<String>();

        for (error: FieldError in ex.bindingResult!!.fieldErrors) {
            errors.add(error.field + ": " + error.defaultMessage);
        }
        for (error: ObjectError in ex.bindingResult!!.globalErrors) {
            errors.add(error.objectName + ": " + error.defaultMessage);
        }

        val apiError = ApiError(HttpStatus.BAD_REQUEST, ex.localizedMessage, errors);
        return handleExceptionInternal(
                ex, apiError, headers, apiError.status, request);
    }
}