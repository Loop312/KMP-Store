package io.github.kmpstore.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
actual fun BindBrowserNavigation(backStack: SnapshotStateList<Route>) {}