package com.hashconcepts.buycart.presentation.screens.home

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.hashconcepts.buycart.R
import com.hashconcepts.buycart.presentation.components.Indicators
import com.hashconcepts.buycart.ui.theme.*
import com.hashconcepts.buycart.utils.UIEvents
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.lang.Exception

/**
 * @created 08/07/2022 - 4:49 PM
 * @project BuyCart
 * @author  ifechukwu.udorji
 */

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Destination
@Composable
fun HomeScreen(
    navigator: DestinationsNavigator,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setStatusBarColor(backgroundColor)
        systemUiController.setNavigationBarColor(Color.White)
    }

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(key1 = true) {
        homeViewModel.eventChannelFlow.collectLatest { uiEvent ->
            when (uiEvent) {
                is UIEvents.ErrorEvent -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = uiEvent.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
        ) {
            if (homeViewModel.homeScreenState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(),
                    color = errorColor
                )
            }

            Text(
                text = "Discover",
                style = MaterialTheme.typography.h1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            SearchFilterSection(homeViewModel)

            Spacer(modifier = Modifier.height(20.dp))

            if (!homeViewModel.homeScreenState.filterSelected) {
                OfferSection(homeViewModel.offerImages)
            } else {
                CategorySection(homeViewModel)
            }

            Spacer(modifier = Modifier.height(20.dp))

            ProductSection(homeViewModel)
        }
    }
}

@Composable
fun ProductSection(homeViewModel: HomeViewModel) {
    val products = homeViewModel.homeScreenState.products
    val state = homeViewModel.homeScreenState
    val context = LocalContext.current

    LazyVerticalGrid(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        columns = GridCells.Fixed(2),
        content = {
            items(products) { product ->
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(10.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Toast
                                .makeText(context, product.title, Toast.LENGTH_SHORT)
                                .show()
                        }
                ) {
                    AsyncImage(
                        model = product.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.placeholder_image),
                        modifier = Modifier
                            .size(100.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.h2,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$${product.price}",
                            style = MaterialTheme.typography.h2,
                            fontSize = 13.sp
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(primaryColor)
                                .padding(vertical = 5.dp, horizontal = 7.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    homeViewModel.onEvents(HomeScreenEvents.AddProductToCart(product.id))
                                }
                        ) {
                            if (state.addingToCart && product.id == state.productInCart) {
                                CircularProgressIndicator(
                                    strokeWidth = 1.dp,
                                    modifier = Modifier.size(15.dp),
                                    color = secondaryColor
                                )
                            } else {
                                Text(
                                    text = if (state.addedToCart && product.id == state.productInCart) "Added" else "Add Cart",
                                    style = MaterialTheme.typography.h2,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                }
            }
        })
}

@Composable
fun CategorySection(homeViewModel: HomeViewModel) {
    val categories = homeViewModel.homeScreenState.categories
    val categoryIndex = homeViewModel.homeScreenState.selectedCategoryIndex
    if (categories.isNotEmpty()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), content = {
            items(categories) { category ->
                Text(
                    text = category,
                    style = MaterialTheme.typography.h2,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (categoryIndex == categories.indexOf(category)) primaryColor else Color.White)
                        .padding(horizontal = 15.dp, vertical = 10.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            homeViewModel.onEvents(
                                HomeScreenEvents.CategorySelected(
                                    category,
                                    categories.indexOf(category)
                                )
                            )
                        }
                )
            }
        })
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OfferSection(offers: List<String>) {
    val pagerState = rememberPagerState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            count = offers.size,
            state = pagerState,
            itemSpacing = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentOffer = offers[pagerState.currentPage]
            AsyncImage(
                model = currentOffer,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.placeholder_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .height(170.dp)
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        Indicators(size = offers.size, index = pagerState.currentPage)
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        try {
            delay(3000L)
            val page = if (pagerState.currentPage < pagerState.pageCount - 1) {
                pagerState.currentPage + 1
            } else 0
            pagerState.scrollToPage(page)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun SearchFilterSection(homeViewModel: HomeViewModel) {
    var searchText by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text(
                    text = "Search Product",
                    style = MaterialTheme.typography.body1,
                    color = disableColor
                )
            },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.textFieldColors(
                cursorColor = primaryColor,
                textColor = secondaryColor,
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .weight(1f),
            maxLines = 1,
            singleLine = true,
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            homeViewModel.onEvents(HomeScreenEvents.FilterClicked(!homeViewModel.homeScreenState.filterSelected))
        }) {
            Icon(
                modifier = Modifier
                    .size(50.dp)
                    .clip(shape = RoundedCornerShape(size = 8.dp))
                    .background(if (homeViewModel.homeScreenState.filterSelected) primaryColor else Color.White)
                    .padding(5.dp),
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = null,
                tint = secondaryColor
            )
        }
    }
}
