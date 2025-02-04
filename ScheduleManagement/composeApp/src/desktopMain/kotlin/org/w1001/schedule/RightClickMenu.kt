package org.w1001.schedule

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RightClickMenu(
    cellData: CellData,
    onAbsence: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTextContextMenu provides object : TextContextMenu {
            @Composable
            override fun Area(
                textManager: TextContextMenu.TextManager, state: ContextMenuState, content: @Composable () -> Unit
            ) {
                ContextMenuArea(
                    items = {
                        listOf(
                            ContextMenuItem("Absence") {
                                // If an onAbsence callback is provided, call it.
                                onAbsence?.invoke() ?: run {
                                    // Default action if not provided.
                                    println("Absence clicked for text: ${textManager.selectedText}")
                                    cellData.content.value = "A"
                                }
                            }
                        )
                    },
                    state = state
                ) { content() }
            }
        },
        content = content
    )
}