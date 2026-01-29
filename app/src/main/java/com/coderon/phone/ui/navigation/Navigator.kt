package com.coderon.phone.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState) {
    
    fun navigate(route: NavKey) {
        val topLevelBackStacks = state.backStacks
        if (route in topLevelBackStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            // It's a child route, add it to the current stack
            val currentStack = topLevelBackStacks[state.topLevelRoute]
            // Avoid pushing the same route multiple times consecutively
            if (currentStack?.lastOrNull() != route) {
                currentStack?.add(route)
            }
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?:
        error("Stack for ${state.topLevelRoute} not found")
        
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
        } else if (state.topLevelRoute != state.startRoute) {
            // If we're at the base of a non-start stack, go back to the start stack
            state.topLevelRoute = state.startRoute
        }
    }
}
