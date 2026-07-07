package io.github.kmpstore.presentation.drawer

import io.github.kmpstore.Category

sealed class DrawerIntent {
    data object OpenDrawer : DrawerIntent()
    data object CloseDrawer : DrawerIntent()
    data class OpenCategory(val category: Category) : DrawerIntent()
    data class CloseCategory(val category: Category) : DrawerIntent()
}