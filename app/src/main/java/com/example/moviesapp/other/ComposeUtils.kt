package com.example.moviesapp.other

import androidx.annotation.PluralsRes
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.lazy.LazyGridItemScope
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.placeholder
import com.google.accompanist.placeholder.shimmer

fun Modifier.defaultPlaceholder() = composed {
    placeholder(
        visible = true,
        highlight = PlaceholderHighlight.shimmer(
            highlightColor = MaterialTheme.colors.primary.copy(alpha = 0.5f),
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        ),
        color = MaterialTheme.colors.surface,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun LazyListState.isScrollingTowardsStart(): Boolean {
    var previousIndex by remember(this) {
        mutableStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridState.isScrollingTowardsStart(): Boolean {
    var previousIndex by remember(this) {
        mutableStateOf(firstVisibleItemIndex)
    }
    var previousScrollOffset by remember(this) {
        mutableStateOf(firstVisibleItemScrollOffset)
    }

    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun pluralsResource(
    @PluralsRes resId: Int,
    quantity: Int,
    vararg formatArgs: Any? = emptyArray()
): String {
    return LocalContext.current.resources
        .getQuantityString(resId, quantity, *formatArgs)
}

@Composable
fun BoxWithConstraintsScope.getMaxSizeInt(): Pair<Int, Int> {
    return LocalDensity.current.run { maxWidth.toPx().toInt() to maxHeight.toPx().toInt() }
}

@ExperimentalFoundationApi
fun <T : Any> LazyGridScope.items(
    lazyPagingItems: LazyPagingItems<T>,
    itemContent: @Composable LazyGridItemScope.(value: T?) -> Unit
) {
    items(lazyPagingItems.itemCount) { index ->
        itemContent(lazyPagingItems[index])
    }
}

fun LazyPagingItems<*>.isEmpty(): Boolean {
    return run {
        loadState.source.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached
                && itemCount < 1
    }
}

fun LazyPagingItems<*>.isNotEmpty(): Boolean = !isEmpty()

inline fun <T> List<T>?.ifNotNullAndEmpty(scope: (List<T>) -> Unit) {
    if (!this.isNullOrEmpty()) {
        scope(this)
    }
}


