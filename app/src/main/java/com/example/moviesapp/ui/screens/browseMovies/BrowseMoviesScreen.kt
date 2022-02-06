package com.example.moviesapp.ui.screens.browseMovies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.moviesapp.R
import com.example.moviesapp.model.MovieType
import com.example.moviesapp.ui.components.AppBar
import com.example.moviesapp.ui.components.InfoDialog
import com.example.moviesapp.ui.components.PresentableGridSection
import com.example.moviesapp.ui.screens.destinations.MovieDetailsScreenDestination
import com.example.moviesapp.ui.theme.spacing
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalFoundationApi::class, kotlinx.coroutines.FlowPreview::class)
@Destination
@Composable
fun BrowseMoviesScreen(
    viewModel: BrowseMoviesViewModel = hiltViewModel(),
    movieType: MovieType,
    navigator: DestinationsNavigator
) {
    val movies = viewModel.movies?.collectAsLazyPagingItems()

    val favouriteMoviesCount by viewModel.favouriteMoviesCount.collectAsState()

    val appbarTitle = when (movieType) {
        MovieType.Popular -> stringResource(R.string.all_movies_top_rated_label)
        MovieType.Upcoming -> stringResource(R.string.all_movies_upcoming_label)
        MovieType.TopRated -> stringResource(R.string.all_movies_top_rated_label)
        MovieType.Favourite -> stringResource(
            R.string.all_movies_favourites_label,
            favouriteMoviesCount
        )
        MovieType.RecentlyBrowsed -> stringResource(R.string.all_movies_recently_browsed_label)
        MovieType.Trending -> stringResource(R.string.all_movies_trending_label)
    }

    val showClearButton = movieType == MovieType.RecentlyBrowsed
            && movies?.itemSnapshotList?.isEmpty() != true

    var showClearDialog by remember { mutableStateOf(false) }

    val showDialog = {
        showClearDialog = true
    }

    val dismissDialog = {
        showClearDialog = false
    }

    if (showClearDialog) {
        InfoDialog(
            infoText = stringResource(R.string.clear_recent_movies_dialog_text),
            onDismissRequest = dismissDialog,
            onCancelClick = dismissDialog,
            onConfirmClick = {
                viewModel.onClearClicked()
                dismissDialog()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppBar(title = appbarTitle, action = {
            IconButton(onClick = { navigator.navigateUp() }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "go back",
                    tint = MaterialTheme.colors.primary
                )
            }
        }, trailing = {
            AnimatedVisibility(
                visible = showClearButton,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    modifier = Modifier.padding(end = MaterialTheme.spacing.medium),
                    onClick = showDialog
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "clear recent",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        })
        movies?.let { state ->
            PresentableGridSection(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = MaterialTheme.spacing.small,
                    vertical = MaterialTheme.spacing.medium,
                ),
                state = state
            ) { movieId ->
                navigator.navigate(
                    MovieDetailsScreenDestination(movieId)
                )
            }
        }
    }

}