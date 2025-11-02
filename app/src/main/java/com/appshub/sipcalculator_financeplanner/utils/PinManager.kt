package com.appshub.sipcalculator_financeplanner.utils

import java.security.MessageDigest
import java.nio.charset.StandardCharsets

object PinManager {
    
    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray(StandardCharsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    fun validatePin(inputPin: String, storedHash: String): Boolean {
        return hashPin(inputPin) == storedHash
    }
    
    fun isValidPin(pin: String): Boolean {
        return pin.length == 4 && pin.all { it.isDigit() }
    }
}