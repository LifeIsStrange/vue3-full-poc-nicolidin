package com.rebirthCorp.rebirth.utils

fun throwUnless(truthValue: Boolean, exception: Any? = null) {
    if (!truthValue) {
        if (exception == null) {
            throw RuntimeException("called truthFunction resolve to false")
        } else {
            throw exception as Throwable
        }
    }
}