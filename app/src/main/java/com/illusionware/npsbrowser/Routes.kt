package com.illusionware.npsbrowser

sealed class Routes(val route: String) {
    object Home : Routes("home")
    object Onboarding : Routes("onboarding")
    object Settings : Routes("settings")
    object PackageDetails : Routes("package_details")
}
