package eu.tutorials.stepscounter

sealed class Screen(val route:String){
    object StartScreen: Screen("startscreen")
    object LoginScreen: Screen("loginscreen")
    object SignupScreen: Screen("signupscreen")
    object MainFlow: Screen("mainflow")
}

sealed class MainScreen(val route:String){
    object HomeScreen : MainScreen("home")
    object DetailScreen : MainScreen("detail")
    object SettingsScreen : MainScreen("settings")
}