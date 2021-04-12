package com.rebirthCorp.rebirth.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.sql.Timestamp
import java.time.Instant

// fixme implement all generics checks e.g isStringBlank
inline fun <reified T : Any> T.patch(src: String, authorizedKeysToPatch: List<String>? = null, forbiddenKeysToPatch: MutableList<String> = mutableListOf(), noinline isFieldValid: ((Any, Any?) -> Boolean)? = null, noinline customMapValues: ((Map<String, Any>) -> Unit)? = null): T {
    val om = ObjectMapper()
    val json: Map<String, Any> = om.readValue(src)
    val universallyForbiddenKeys = listOf("id", "createdAt", "updatedAt")
    forbiddenKeysToPatch.addAll(universallyForbiddenKeys)

    // filter out forbidden keys
    var patch = json.filter {
        !forbiddenKeysToPatch.contains(it.key) //&& it.value is String
    }.toMutableMap()
    // filter out non-authorized keys
    if (authorizedKeysToPatch != null) {
        patch = json.filter {
            authorizedKeysToPatch.contains(it.key) //&& it.value is String
        }.toMutableMap()
    }
    // filter out invalid values
    if (isFieldValid != null) {
        patch.filterValues {
            isFieldValid(it, null) // review signature
        }
    }
    // optionally set values before patching
    if (customMapValues != null) {
        customMapValues(patch)
    }

    patch["updatedAt"] = Timestamp.from(Instant.now())

    om.readerForUpdating(this).readValue<String>(om.writeValueAsString(patch))
    return this
}