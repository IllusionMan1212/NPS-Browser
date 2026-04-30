package com.illusionware.npsbrowser

sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Onboarding : Routes("onboarding")
    data object Settings : Routes("settings")
    data object PackageDetails : Routes("package_details")
    data object Downloads : Routes("downloads")
}
