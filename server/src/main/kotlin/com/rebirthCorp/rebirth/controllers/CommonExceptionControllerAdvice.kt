package com.rebirthCorp.rebirth.controllers

import com.rebirthCorp.rebirth.exceptions.generic.BadRequest
import com.rebirthCorp.rebirth.exceptions.generic.ResourceNotFoundException
import com.rebirthCorp.rebirth.exceptions.generic.ResourceReservedException
import com.rebirthCorp.rebirth.exceptions.generic.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class CommonExceptionControllerAdvice {

    @ExceptionHandler
    fun handleUnauthorizedException(err: UnauthorizedException): ResponseEntity<UnauthorizedException> {
        return ResponseEntity<UnauthorizedException>(err, err.status)
    }

    @ExceptionHandler
    fun handleResourceReservedException(err: ResourceReservedException): ResponseEntity<ResourceReservedException> {
        return ResponseEntity<ResourceReservedException>(err, err.status)
    }

    @ExceptionHandler
    fun handleResourceNotFoundException(err: ResourceNotFoundException): ResponseEntity<ResourceNotFoundException> {
        return ResponseEntity<ResourceNotFoundException>(err, err.status)
    }

    @ExceptionHandler
    fun handleBadRequestException(err: BadRequest): ResponseEntity<BadRequest> {
        return ResponseEntity(err, HttpStatus.BAD_REQUEST)
    }
}