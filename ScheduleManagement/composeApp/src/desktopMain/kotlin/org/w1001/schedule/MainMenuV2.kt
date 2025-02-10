
import androidx.compose.animation.*
import androidx.compose.runtime.*
import org.w1001.schedule.AppViewModel
import org.w1001.schedule.database.SpreadsheetRepository
import org.w1001.schedule.subViews.CollectionView
import org.w1001.schedule.subViews.OpenedCollectionView
import org.w1001.schedule.subViews.PlacesView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainMenuV2(
    viewModel: AppViewModel,
) {
    var selectedPlace by remember { mutableStateOf<String?>(null) }
    var selectedCollection by remember { mutableStateOf<String?>(null) }
    val repository = remember { SpreadsheetRepository() }

    AnimatedContent(
        targetState = Triple(selectedPlace, selectedCollection, null),
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }
    ) { (place, collection, _) ->
        when {
            place == null -> {
                PlacesView(repository) { selectedPlace = it }
            }
            collection == null -> {
                CollectionView(
                    place = place,
                    repository = repository,
                    onBack = { selectedPlace = null },
                    onCollectionSelected = { selectedCollection = it }
                )
            }
            else -> {
                OpenedCollectionView(
                    place = place,
                    collection = collection,
                    repository = repository,
                    onBack = { selectedCollection = null }
                )
            }
        }
    }
}
