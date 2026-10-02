package com.unal.nachoquest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.unal.nachoquest.ui.screens.auth.AuthViewModel
import com.unal.nachoquest.ui.screens.auth.LoginScreen
import com.unal.nachoquest.ui.screens.auth.RegisterScreen
import com.unal.nachoquest.ui.screens.quiz.QuizScreen
import com.unal.nachoquest.ui.screens.splash.SplashScreen

/**
 * NavHost principal de NachoQuest.
 */
@Composable
fun NachoQuestNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = SplashRoute
    ) {
        composable<SplashRoute> {
            SplashScreen(
                onComenzar = {
                    navController.navigate(RegisterRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                },
                onIniciarSesion = {
                    navController.navigate(LoginRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = {
                    navController.navigate(MainRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate(RegisterRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                viewModel = authViewModel,
                onRegistroExitoso = {
                    navController.navigate(MainRoute) {
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(RegisterRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<MainRoute> {
            MainScreen(
                onIniciarReto = { puntoId -> 
                    navController.navigate(QuizRoute(puntoId)) 
                },
                onCerrarSesion = {
                    authViewModel.cerrarSesion()
                    navController.navigate(SplashRoute) {
                        popUpTo(MainRoute) { inclusive = true }
                    }
                }
            )
        }
        
        composable<QuizRoute> { backStackEntry ->
            val quizRoute = backStackEntry.toRoute<QuizRoute>()
            QuizScreen(
                puntoId = quizRoute.puntoId,
                onTerminar = { navController.popBackStack() }
            )
        }
    }
}
