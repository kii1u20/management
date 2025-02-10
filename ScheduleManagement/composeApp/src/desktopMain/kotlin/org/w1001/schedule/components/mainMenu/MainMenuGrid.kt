package org.w1001.schedule.components.mainMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun MainMenuGrid(objects: List<String>, onObjectSelected: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(16.dp))
            .zIndex(1f),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(100.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 48.dp)
        ) {
            items(objects) { obj ->
                MainMenuCard(
                    text = obj,
                    onClick = { onObjectSelected(obj) },
                    modifier = Modifier.size(width = 200.dp, height = 200.dp)
                )
            }
        }
    }
}