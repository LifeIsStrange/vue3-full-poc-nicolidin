package com.rebirthCorp.rebirth.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonView
import com.rebirthCorp.rebirth.utils.generateUUID
import java.sql.Timestamp
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.Size

enum class AdminLevel {
    NONE, MODERATOR, ADMINISTRATOR
}

@Entity(name = "User")
@Table(name = """"user"""")
//The user is escaped as it's a reserved SQL keyword
class UserModel(
        username: String = "",
        email: String = "",
        password: String = ""
) : Cloneable {
    @Id
    @JsonView(SummaryView::class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var id: String = generateUUID()

    @Size(min = 3, max = 20)
    @JsonView(SummaryView::class)
    var username: String = ""

    @Size(max = 30)
    @JsonProperty
    @JsonView(PrivateView::class)
    var email: String = ""

    @JsonProperty
    @JsonView(PrivateView::class)
    @Enumerated(EnumType.STRING)
    var adminLevel: AdminLevel = AdminLevel.NONE

    @Size(min = 6)
    var password: String = ""
        @JsonIgnore get() = field
        @JsonProperty set(newPassword) {
            field = newPassword
        }

    init
    {
        this.username = username
        this.email = email
        this.password = password
    }

    @Column(name = "has_verified_mail")
    @JsonView(PrivateView::class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var hasVerifiedMail: Boolean = false

    @Column(name = "created_at")
    @JsonView(DetailedView::class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var createdAt = Timestamp.from(Instant.now())

    @Column(name = "updated_at")
    @JsonView(PrivateView::class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    var updatedAt: Timestamp? = null

    public override fun clone(): UserModel {
        return super.clone() as UserModel
    }

    interface SummaryView
    interface DetailedView : SummaryView
    interface PrivateView : DetailedView
}
