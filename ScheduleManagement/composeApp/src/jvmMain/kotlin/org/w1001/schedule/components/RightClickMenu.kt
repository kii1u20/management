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
                                onRightClick()
                                // If an onAbsence callback is provided, call it.
                                onAbsence?.invoke() ?: run {
                                    // Default action if not provided.
                                    println("Absence clicked for text: ${textManager.selectedText}")
                                    cellDataGroup.last().content.value = "A"
                                }
                            },
                            ContextMenuItem("Break") {
                                onRightClick()
                                // If an onAbsence callback is provided, call it.
                                onBreak?.invoke() ?: run {
                                    // Default action if not provided.
                                    println("Break clicked for text: ${textManager.selectedText}")
                                    cellDataGroup.last().content.value = "B"
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