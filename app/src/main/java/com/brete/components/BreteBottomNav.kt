package com.brete.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.brete.ui.theme.*

data class BottomNavItem(val label: String, val iconSelected: ImageVector, val iconDefault: ImageVector)

@Composable
fun BreteBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf(
        BottomNavItem("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
        BottomNavItem("Crear", Icons.Outlined.AddCircle, Icons.Outlined.AddCircle),
        BottomNavItem("Avisos", Icons.Filled.Notifications, Icons.Outlined.Notifications),
        BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar(
        // Utiliza la superficie del tema dinámico para que se adapte al modo oscuro
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        if (selectedTab == index) item.iconSelected else item.iconDefault,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Teal600,
                    selectedTextColor = Teal600,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Teal600.copy(alpha = 0.15f)
                )
            )
        }
    }
}