package org.w1001.schedule.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.w1001.schedule.CellData

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RightClickMenu(
    cellDataGroup: List<CellData>,
    onAbsence: (() -> Unit)? = null,
    onBreak: (() -> Unit)? = null,
    onRightClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val menuItems = listOf(
        ContextMenuItem("Absence") {
            onRightClick()
            onAbsence?.invoke() ?: run {
                cellDataGroup.last().content.value = "A"
            }
        },
        ContextMenuItem("Break") {
            onRightClick()
            onBreak?.invoke() ?: run {
                cellDataGroup.last().content.value = "B"
            }
        }
    )

    // Handle right-click on static content
    ContextMenuArea(
        items = { menuItems }
    ) {
        // Override TextContextMenu for TextFields to show custom items
        CompositionLocalProvider(
            LocalTextContextMenu provides object : TextContextMenu {
                @Composable
                override fun Area(
                    textManager: TextContextMenu.TextManager,
                    state: ContextMenuState,
                    content: @Composable () -> Unit
                ) {
                    ContextMenuArea(
                        items = { menuItems },
                        state = state
                    ) {
                        content()
                    }
                }
            }
        ) {
            content()
        }
    }
}