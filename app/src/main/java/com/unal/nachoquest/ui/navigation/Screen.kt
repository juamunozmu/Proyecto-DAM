package com.unal.nachoquest.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object MainRoute

@Serializable
object MapRoute

@Serializable
object ChallengesRoute

@Serializable
object InventoryRoute

@Serializable
object ProfileRoute

@Serializable
data class QuizRoute(val puntoId: String)