package com.rebirthCorp.rebirth.models

import com.rebirthCorp.rebirth.utils.generateUUID
import java.sql.Timestamp
import java.time.Instant

//We need this duplicate to get rid of our Jackson rules :/
data class TestUser(var id: String = generateUUID(),
                    var username: String = "",
                    var email: String = "",
                    var password: String = "",
                    var hasVerifiedMail: Boolean = false,
                    var createdAt: Timestamp = Timestamp.from(Instant.now()),
                    var adminLevel: AdminLevel = AdminLevel.NONE,
                    var updatedAt: Timestamp? = null)