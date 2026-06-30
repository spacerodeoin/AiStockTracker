package com.aitracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aitracker.app.ui.detail.DetailScreen
import com.aitracker.app.ui.news.NewsScreen
import com.aitracker.app.ui.stocks.StocksScreen
import com.aitracker.app.ui.watchlist.WatchlistScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }
    val currentTab = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (isTopLevel) {
                TopAppBar(title = { Text(currentTab?.label ?: "AI Tracker") })
            }
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    val destination = backStackEntry?.destination
                    TopLevelDestination.entries.forEach { tab ->
                        val selected = destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.STOCKS.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(TopLevelDestination.STOCKS.route) {
                StocksScreen(onCompanyClick = { navController.navigate(Routes.detail(it)) })
            }
            composable(TopLevelDestination.NEWS.route) {
                NewsScreen()
            }
            composable(TopLevelDestination.WATCHLIST.route) {
                WatchlistScreen(onCompanyClick = { navController.navigate(Routes.detail(it)) })
            }
            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument(Routes.ARG_SYMBOL) { type = NavType.StringType }),
            ) {
                DetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
