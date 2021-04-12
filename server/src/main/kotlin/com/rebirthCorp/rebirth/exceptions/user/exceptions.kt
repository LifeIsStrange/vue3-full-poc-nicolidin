package com.rebirthCorp.rebirth.exceptions.user

import com.rebirthCorp.rebirth.models.UserModel

class UserCreationException(message: String, user: UserModel) : Exception(message)
class UserDoesNotExistException(message: String) : Exception(message)
class ArticleDoesNotExistException(message: String) : Exception(message)
class BadCredentials(message: String) : Exception(message)
