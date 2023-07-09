package com.deepshooter.moviesapp.ui.navigations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.deepshooter.moviesapp.domain.model.Movie
import com.deepshooter.moviesapp.ui.screens.movies.MoviesScreen
import com.deepshooter.moviesapp.ui.screens.movies.MoviesViewModel
import com.deepshooter.moviesapp.ui.screens.moviesDetails.MovieDetailsScreen
import com.deepshooter.moviesapp.ui.screens.moviesDetails.MovieDetailsViewModel
import com.deepshooter.moviesapp.ui.theme.GoogleSans
import com.deepshooter.moviesapp.util.ScreenUiState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(navController: NavHostController) {
    AnimatedNavHost(navController = navController, startDestination = Destinations.Movies.route) {
        composable(Destinations.Movies.route) { MoviesScreenGraph() }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MoviesScreenGraph(navController: NavHostController = rememberAnimatedNavController()) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Destinations.MoviesDiscover.route
    ) {
        //Movies
        composable(
            Destinations.MoviesDiscover.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Destinations.MoviesDetails.route ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Destinations.MoviesDetails.route ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Destinations.MoviesDetails.route ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    Destinations.MoviesDetails.route ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            }
        ) {
            val moviesViewModel = hiltViewModel<MoviesViewModel>()
            val moviesUiStateList =
                moviesViewModel.moviesUiStatesList.map {
                    Pair(
                        it.title,
                        it.list.collectAsStateWithLifecycle(initialValue = ScreenUiState.Loading).value
                    )
                }
            val searchedMoviesUiStateList =
                moviesViewModel
                    .searchedMoviesUiState
                    .collectAsStateWithLifecycle(initialValue = ScreenUiState.Loading).value

            MoviesScreen(
                navigateToDetails = { movieId: Int ->
                    navController.navigate("MovieDetailsScreen/$movieId") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                moviesUiStateList = moviesUiStateList,
                searchedMoviesUiStateList = searchedMoviesUiStateList,
                setSearchedMoviesList = { searchTerm: String ->
                    moviesViewModel.setSearchedMoviesList(
                        searchTerm
                    )
                }
            )
        }

        //Movies Details
        composable(
            Destinations.MoviesDetails.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Destinations.MoviesDiscover.route ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Destinations.MoviesDiscover.route ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Destinations.MoviesDiscover.route ->
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            },
            popExitTransition = {
                when (targetState.destination.route) {
                    Destinations.MoviesDiscover.route ->
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(700)
                        )

                    else -> null
                }
            }
        ) {

            val movieDetailsViewModel = hiltViewModel<MovieDetailsViewModel>()
            val movieIdArg = it.arguments?.getString("movieId")?.toInt()
            val movieDetailsUiState =
                movieDetailsViewModel
                    .movieDetailUiState
                    .collectAsStateWithLifecycle(initialValue = ScreenUiState.Loading).value
            val isMovieFavorite =
                movieDetailsViewModel
                    .isMovieFavorite
                    .collectAsStateWithLifecycle(initialValue = false).value

            movieIdArg?.let {
                LaunchedEffect(movieIdArg) {
                    movieDetailsViewModel.setMovieDetails(movieIdArg)
                    movieDetailsViewModel.refreshIsMovieFavorite(movieIdArg)
                }

                MovieDetailsScreen(
                    popBackStack = {
                        navController.popBackStack()
                    },
                    movieDetailsUiState = movieDetailsUiState,
                    isMovieFavorite = isMovieFavorite,
                    addFavoriteMovie = { movie: Movie ->
                        movieDetailsViewModel.addFavoriteMovie(
                            movie
                        )
                    },
                    removeFavoriteMovie = { movie: Movie ->
                        movieDetailsViewModel.removeFavoriteMovie(
                            movie
                        )
                    }
                )
            }
        }
    }


    @Composable
    fun BottomNavBar(navController: NavHostController, modifier: Modifier = Modifier) {

        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry.value?.destination?.route

        Box(modifier.background(MaterialTheme.colorScheme.primary)) {
            NavigationBar(containerColor = Color.Transparent) {

                Destinations.navScreenList.forEach { screen ->
                    if (screen.title != null && screen.icon != null) {
                        NavigationBarItem(
                            label = { Text(stringResource(screen.title), fontFamily = GoogleSans) },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = stringResource(screen.title)
                                )
                            },
                            selected = currentDestination == screen.route || currentDestination == screen.subRoute,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}