package com.example.smartfit

object Validator {

    // Validate the email format
    fun validateEmail(email: String): Boolean {
        return if (email.isBlank()) {
            false
        } else {
            email.contains("@") && email.contains(".") // Must contain @ and . symbol.
        }
    }

    // Verify whether the password meet at least 8 characters requirements
    fun validatePassword(password: String): Boolean {
        return if (password.isBlank()) {
            false
        } else {
            password.length >= 8
        }
    }

    // verify register input（All text box cannot be null）
    fun validateRegistrationInput(name: String, email: String, pass: String): Boolean {
        return !(name.isBlank() || email.isBlank() || pass.isBlank())
    }
}