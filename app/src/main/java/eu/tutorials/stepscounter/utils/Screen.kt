package eu.tutorials.stepscounter.utils

sealed class Screen(val route:String){
    object StartScreen: Screen("start")
    object LoginScreen: Screen("login")
    object SignupScreen: Screen("signup")
    object MainFlow: Screen("mainflow")
}

sealed class MainScreen(val route:String){
    object HomeScreen : MainScreen("home")
    object ProfileScreen : MainScreen("profile")
    object SettingsScreen : MainScreen("settings")
    object SummaryScreen : MainScreen("summary")
}