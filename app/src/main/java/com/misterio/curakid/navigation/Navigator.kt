package com.misterio.curakid.navigation

import android.util.Log
import androidx.navigation3.runtime.NavKey

//https://github.com/android/nav3-recipes/blob/main/app/src/main/java/com/example/nav3recipes/multiplestacks/Navigator.kt
/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
//    fun navigate(route: NavKey) {
//        if (route in state.backStacks.keys) {
//            // This is a top level route, just switch to it
//            state.topLevelRoute = route
//        } else {
//            state.backStacks[state.topLevelRoute]?.add(route)
//        }
//    }

    fun navigate(route: NavKey) {
        Log.d("NAV", "navigate($route), currentTopLevel=${state.topLevelRoute}")
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            Log.d("NAV", "Adding to stack: ${state.topLevelRoute}")
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}