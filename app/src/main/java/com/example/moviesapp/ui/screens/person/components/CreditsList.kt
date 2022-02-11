package com.example.moviesapp.ui.screens.person.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.moviesapp.model.CreditsPresentable
import com.example.moviesapp.model.MediaType
import com.example.moviesapp.ui.components.SectionLabel
import com.example.moviesapp.ui.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreditsList(
    modifier: Modifier = Modifier,
    title: String,
    list: List<CreditsPresentable>,
    onCreditsClick: (MediaType, Int) -> Unit = { _, _ -> }
) {
    if (list.isNotEmpty()) {
        Column(modifier = modifier) {
            SectionLabel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MaterialTheme.spacing.medium),
                text = title
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.small),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium)
            ) {
                items(list) { creditsPresentable ->
                    CreditsItem(creditsPresentable = creditsPresentable, onClick = {
                        onCreditsClick(
                            creditsPresentable.mediaType, creditsPresentable.id
                        )
                    })
                }
            }
        }
    }

}