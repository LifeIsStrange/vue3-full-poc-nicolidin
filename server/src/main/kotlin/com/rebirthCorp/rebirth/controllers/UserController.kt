package com.rebirthCorp.rebirth.controllers

import com.fasterxml.jackson.annotation.JsonView
import com.fasterxml.jackson.databind.ObjectMapper
import com.rebirthCorp.rebirth.exceptions.generic.ResourceNotFoundException
import com.rebirthCorp.rebirth.exceptions.generic.ResourceReservedException
import com.rebirthCorp.rebirth.exceptions.generic.UnauthorizedException
import com.rebirthCorp.rebirth.exceptions.user.UserCreationException
import com.rebirthCorp.rebirth.models.UserModel
import com.rebirthCorp.rebirth.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = ["**"])
class UserController(
        @Autowired val service: UserService
) {
    @PostMapping(consumes = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    @JsonView(UserModel.PrivateView::class)
    @Throws(UserCreationException::class, ResourceReservedException::class)
    fun createUser(
            @Valid @RequestBody user: UserModel): UserModel {
        return this.service.saveNewUser(user)
    }

    @GetMapping("/{id}", produces = ["application/json"])
    @Throws(ResourceNotFoundException::class)
    fun getUser(@PathVariable id: String, @AuthenticationPrincipal currentUser: UserModel): String {
        val search = try {
            this.service.userRepo.findById(id).get()
        } catch (err: NoSuchElementException) {
            throw ResourceNotFoundException(UserModel())
        }
        val om = ObjectMapper()

        return om.writerWithView(
                if (currentUser.id != id) UserModel.DetailedView::class.java
                else UserModel.PrivateView::class.java)
                .writeValueAsString(search)
    }

    @PutMapping("/{id}", consumes = ["application/json"])
    @Throws(UnauthorizedException::class, UserCreationException::class)
    fun putUser(@PathVariable id: String, @RequestBody user: String, @AuthenticationPrincipal currentUser: UserModel): UserModel {
        val old = this.service.userRepo.findById(id)

        if (currentUser.id != id || !old.isPresent) {
            throw UnauthorizedException("You're not authorized to modify this user")
        }
        val patchedUser = this.service.patchUser(old.get(), user)
        this.service.validateUser(patchedUser)
        return this.service.userRepo.save(patchedUser)
    }

    @PatchMapping("/{id}", consumes = ["application/json"])
    @Throws(UnauthorizedException::class, UserCreationException::class)
    fun patchUser(@PathVariable id: String, @RequestBody user: String, @AuthenticationPrincipal currentUser: UserModel): UserModel {
        val old = this.service.userRepo.findById(id)

        if (currentUser.id != id || !old.isPresent) {
            throw UnauthorizedException("You're not authorized to modify this user")
        }
        val patchedUser = this.service.patchUser(old.get(), user)
        this.service.validateUser(patchedUser)
        return this.service.userRepo.save(patchedUser)
    }

    @DeleteMapping("/{id}")
    @Throws(UnauthorizedException::class)
    fun deleteUser(@PathVariable id: String, @AuthenticationPrincipal currentUser: UserModel) {
        if (currentUser.id != id) {
            throw UnauthorizedException("You're not authorized to delete this user")
        }
        this.service.removeUserFromApp(currentUser.id)
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUserCreationException(err: UserCreationException): String {
        return err.message ?: "Provided user is invalid"
    }
}