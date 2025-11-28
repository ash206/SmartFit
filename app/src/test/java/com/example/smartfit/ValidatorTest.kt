package com.example.smartfit

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {

    // email testing

    @Test
    fun validateEmail_ValidFormat_ReturnsTrue() {
        val result = Validator.validateEmail("test@example.com")
        assertTrue(result)
    }

    @Test
    fun validateEmail_InvalidFormat_ReturnsFalse() {
        val result = Validator.validateEmail("testexample.com") // Missed @ symbol
        assertFalse(result)
    }

    @Test
    fun validateEmail_Empty_ReturnsFalse() {
        val result = Validator.validateEmail("")
        assertFalse(result)
    }

    // password testing

    @Test
    fun validatePassword_ShortPassword_ReturnsFalse() {
        val result = Validator.validatePassword("1234567") //if 7 digit means didn't meet the requirements
        assertFalse(result)
    }

    @Test
    fun validatePassword_ValidPassword_ReturnsTrue() {
        val result = Validator.validatePassword("12345678") // if 8 digit = true
        assertTrue(result)
    }

    // register input test

    @Test
    fun validateRegistration_EmptyName_ReturnsFalse() {
        val result = Validator.validateRegistrationInput("", "test@test.com", "12345678")
        assertFalse(result)
    }
}