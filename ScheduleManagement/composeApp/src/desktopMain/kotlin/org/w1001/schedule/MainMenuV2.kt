
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.w1001.schedule.AppViewModel
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.subViews.CollectionView
import org.w1001.schedule.subViews.PlacesView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainMenuV2(
    viewModel: AppViewModel,
) {
    var selectedPlace by remember { mutableStateOf<String?>(null) }
    val repository = remember { SpreadsheetRepository() }

    AnimatedContent(
        targetState = selectedPlace,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { place ->
        if (place == null) {
            PlacesView(repository) { selectedPlace = it }
        } else {
            CollectionView(place, repository) { selectedPlace = null }
        }
    }
}
