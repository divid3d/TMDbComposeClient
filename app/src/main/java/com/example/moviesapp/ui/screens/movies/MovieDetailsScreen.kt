package com.example.moviesapp.ui.screens.movies

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.moviesapp.R
import com.example.moviesapp.other.formattedMoney
import com.example.moviesapp.ui.components.*
import com.example.moviesapp.ui.screens.destinations.MovieDetailsScreenDestination
import com.example.moviesapp.ui.screens.destinations.MoviesScreenDestination
import com.example.moviesapp.ui.screens.movies.components.GenresSection
import com.example.moviesapp.ui.screens.movies.components.OverviewSection
import com.example.moviesapp.ui.theme.Black500
import com.example.moviesapp.ui.theme.spacing
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun MovieDetailsScreen(
    navigator: DestinationsNavigator,
    movieId: Int,
    startRoute: String = MoviesScreenDestination.route
) {
    val viewModel: MoviesDetailsViewModel = hiltViewModel()
    val density = LocalDensity.current
    val insets = LocalWindowInsets.current

    val movieDetails by viewModel.movieDetails.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()
    val credits by viewModel.credits.collectAsState()

    val similarMoviesState = viewModel.similarMoviesPagingDataFlow?.collectAsLazyPagingItems()
    val moviesRecommendationState =
        viewModel.moviesRecommendationPagingDataFlow?.collectAsLazyPagingItems()

    val otherOriginalTitle: Boolean by derivedStateOf {
        movieDetails?.run { originalTitle.isNotEmpty() && title != originalTitle } ?: false
    }

    val scrollState = rememberScrollState()

    var topSectionHeight: Int? by remember { mutableStateOf(null) }
    val appBarHeight by remember { mutableStateOf(density.run { 56.dp.toPx() }) }
    val statusBarHeight: Int by remember {
        mutableStateOf(insets.statusBars.top)
    }

    val appbarColor: Color by derivedStateOf {
        topSectionHeight?.let { height ->
            val alpha =
                (scrollState.value.toFloat() / (height - appBarHeight - statusBarHeight)).coerceIn(
                    0f,
                    1f
                ) * 0.5f + 0.5f

            Color.Black.copy(alpha)
        } ?: Black500
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            PresentableDetailsTopSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        topSectionHeight = coordinates.size.height
                    },
                presentable = movieDetails,
            ) {
                movieDetails?.let { details ->
                    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                        LabeledText(
                            label = stringResource(R.string.movie_details_status),
                            text = stringResource(details.status.getLabel())
                        )
                        if (details.budget > 0) {
                            LabeledText(
                                label = stringResource(R.string.movie_details_budget),
                                text = details.budget.formattedMoney()
                            )
                        }
                        if (details.revenue > 0) {
                            LabeledText(
                                label = stringResource(R.string.movie_details_boxoffice),
                                text = details.revenue.formattedMoney()
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
                            Label(label = stringResource(R.string.movie_details_genres))
                            GenresSection(genres = details.genres)
                        }
                    }
                }
            }

            movieDetails?.let { details ->
                Column(
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Text(
                        text = details.title,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (otherOriginalTitle) {
                        Text(text = details.originalTitle)
                    }

                    details.tagline?.let { tagline ->
                        if (tagline.isNotEmpty()) {
                            Text(
                                text = "\"$tagline\"",
                                style = TextStyle(fontStyle = FontStyle.Italic)
                            )
                        }
                    }
                    OverviewSection(
                        overview = details.overview
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            credits?.cast?.let { castMembers ->
                MemberSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    title = "Obsada",
                    members = castMembers,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            credits?.crew?.let { crewMembers ->
                MemberSection(
                    modifier = Modifier.animateContentSize(),
                    title = "Ekipa filmowa",
                    members = crewMembers,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
                )
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            similarMoviesState?.let { lazyPagingItems ->
                PresentableSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    title = stringResource(R.string.movie_details_similar),
                    showMoreButton = false,
                    state = lazyPagingItems
                ) { movieId ->
                    navigator.navigate(
                        MovieDetailsScreenDestination(
                            movieId = movieId,
                            startRoute = startRoute
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
            moviesRecommendationState?.let { lazyPagingItems ->
                PresentableSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    title = stringResource(R.string.movie_details_recommendations),
                    showMoreButton = false,
                    state = lazyPagingItems
                ) { movieId ->
                    navigator.navigate(
                        MovieDetailsScreenDestination(
                            movieId = movieId,
                            startRoute = startRoute
                        )
                    )
                }
            }
            Spacer(
                modifier = Modifier.navigationBarsHeight(additional = MaterialTheme.spacing.large)
            )
        }
        AppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = stringResource(R.string.movie_details_label),
            backgroundColor = appbarColor,
            action = {
                IconButton(onClick = { navigator.navigateUp() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            trailing = {
                Row(modifier = Modifier.padding(end = MaterialTheme.spacing.small)) {
                    LikeButton(
                        isFavourite = isFavourite,
                        onClick = {
                            movieDetails?.let { details ->
                                if (isFavourite) {
                                    viewModel.onUnlikeClick(details)
                                } else {
                                    viewModel.onLikeClick(details)
                                }
                            }
                        }
                    )
                    IconButton(
                        onClick = {
                            navigator.popBackStack(startRoute, inclusive = false)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        )
    }

}