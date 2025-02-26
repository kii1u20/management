
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import org.w1001.schedule.AppViewModel
import org.w1001.schedule.subViews.CollectionView
import org.w1001.schedule.subViews.ConnectionStringView
import org.w1001.schedule.subViews.OpenedCollectionView
import org.w1001.schedule.subViews.PlacesView

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainMenuV2(
    viewModel: AppViewModel,
) {
    var selectedPlace by remember { mutableStateOf<String?>(null) }
    var selectedCollection by remember { mutableStateOf<String?>(null) }

    if (!viewModel.isRepositoryInitialized) {
        ConnectionStringView()
        return
    }


    AnimatedContent(
        targetState = Triple(selectedPlace, selectedCollection, null),
        transitionSpec = {
            when {
                // Going forward (deeper into the navigation)
                targetState.first != null && initialState.first == null -> {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
                targetState.second != null && initialState.second == null -> {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }
                // Going backward
                targetState.first == null && initialState.first != null -> {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
                targetState.second == null && initialState.second != null -> {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
                // Default case
                else -> {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            }
        }
    ) { (place, collection, _) ->
        when {
            place == null -> {
                PlacesView(viewModel.repository) { selectedPlace = it }
            }
            collection == null -> {
                CollectionView(
                    place = place,
                    repository = viewModel.repository,
                    onBack = { selectedPlace = null },
                    onCollectionSelected = { selectedCollection = it }
                )
            }
            else -> {
                OpenedCollectionView(
                    place = place,
                    collection = collection,
                    repository = viewModel.repository,
                    onBack = { selectedCollection = null }
                )
            }
        }
    }
}
