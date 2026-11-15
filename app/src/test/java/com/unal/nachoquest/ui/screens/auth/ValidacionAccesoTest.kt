package com.unal.nachoquest.ui.screens.auth

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionAccesoTest {

    @Test
    fun `acepta correos institucionales y personales`() {
        assertNull(validarCorreo("jmunoz@unal.edu.co"))
        assertNull(validarCorreo("nacho.quest+pruebas@gmail.com"))
    }

    @Test
    fun `rechaza correos incompletos`() {
        listOf("", "jmunoz", "jmunoz@unal", "@unal.edu.co", "j munoz@unal.edu.co").forEach {
            assertNotNull("Debería rechazar '$it'", validarCorreo(it))
        }
    }

    @Test
    fun `la contraseña necesita al menos seis caracteres`() {
        assertNotNull(validarContrasena("12345"))
        assertNull(validarContrasena("123456"))
    }

    @Test
    fun `el nombre de usuario es obligatorio`() {
        assertNotNull(validarNombre("   "))
        assertNull(validarNombre("Nacho"))
    }
}
