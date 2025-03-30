package eu.tutorials.stepscounter

sealed class Screen(val route:String){
    object StartScreen: Screen("startscreen")
    object LoginScreen: Screen("loginscreen")
    object SignupScreen: Screen("signupscreen")
    object MainScreen: Screen("mainscreen")
}