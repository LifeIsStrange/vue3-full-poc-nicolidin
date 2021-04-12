package com.rebirthCorp.rebirth.repositories

import com.rebirthCorp.rebirth.models.UserModel
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import javax.transaction.Transactional

interface UserRepository : CrudRepository<UserModel, String> {

    @Query("""SELECT * FROM "user" WHERE "user".email = ?""", nativeQuery = true)
    fun findUserByEmail(@Param("email") email: String): UserModel?

    @Query("""SELECT * FROM "user" WHERE id=?""", nativeQuery = true)
    fun findUserByID(id: String): UserModel?

    @Query("""SELECT * FROM "user" u WHERE u.username = ? LIMIT 1""", nativeQuery = true)
    fun findByUsername(@Param("username") username: String): UserModel?

    @Query("""SELECT COUNT(*) FROM "user" u WHERE u.username = ? OR u.email = ?""", nativeQuery = true)
    fun countUserModelByUsernameEqualsOrEmailEquals(@Param("username") username: String,
                                                    @Param("email") email: String): Int
}
