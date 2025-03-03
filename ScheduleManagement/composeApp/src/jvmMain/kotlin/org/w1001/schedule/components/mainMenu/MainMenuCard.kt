package org.w1001.schedule.components.mainMenu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainMenuCard(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    showDeleteButton: Boolean
) {
    val scale = remember { Animatable(0f) }
    val isHovered = remember { mutableStateOf(false) }
    val deleteButtonScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
        )
    }

    LaunchedEffect(isHovered.value) {
        deleteButtonScale.animateTo(
            targetValue = if (isHovered.value) 1f else 0f,
            animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)
        )
    }

    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .clip(RoundedCornerShape(8.dp))
            .padding(20.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 10.dp,
            hoveredElevation = 14.dp
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onPointerEvent(PointerEventType.Enter) {
                    isHovered.value = true
                }
                .onPointerEvent(PointerEventType.Exit) {
                    isHovered.value = false
                }
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val fontSize = with(LocalDensity.current) { (maxWidth / 8).toSp() }

            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = fontSize, lineHeight = fontSize * 1.1f),
                textAlign = TextAlign.Center,
                maxLines = 3
            )

            if (showDeleteButton) {
                ElevatedButton(
                    modifier = Modifier.align(Alignment.TopEnd).graphicsLayer {
                        scaleX = deleteButtonScale.value
                        scaleY = deleteButtonScale.value
                        alpha = deleteButtonScale.value
                        transformOrigin = TransformOrigin(1f, 0f)
                    },
                    onClick = onDelete,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}