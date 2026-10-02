package com.unal.nachoquest.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Módulo principal de inyección de dependencias.
 * En Parcial 2 se agregarán los proveedores de Firebase Auth y Firestore.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
