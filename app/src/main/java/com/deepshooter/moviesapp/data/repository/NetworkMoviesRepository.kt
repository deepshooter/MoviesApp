package com.deepshooter.moviesapp.data.repository

import android.icu.text.SimpleDateFormat
import com.deepshooter.moviesapp.data.network.MoviesApiService
import com.deepshooter.moviesapp.domain.model.Movie
import com.deepshooter.moviesapp.domain.model.MovieList
import com.deepshooter.moviesapp.domain.repository.MoviesRepository
import com.deepshooter.moviesapp.util.MoviesConstant.DATE_INPUT_FORMAT
import com.deepshooter.moviesapp.util.MoviesConstant.DATE_OUTPUT_FORMAT
import com.deepshooter.moviesapp.util.MoviesConstant.PREFIX_POSTER_URL
import retrofit2.Response
import java.io.IOException
import java.util.Locale

class NetworkMoviesRepository(private val moviesApiService: MoviesApiService) : MoviesRepository {

    override suspend fun getTrendingMovies(): List<Movie> =
        getMoviesListFromApiService(moviesApiService::getTrendingMovies)

    override suspend fun getPopularMovies(): List<Movie> =
        getMoviesListFromApiService(moviesApiService::getPopularMovies)

    override suspend fun getUpcomingMovies(): List<Movie> =
        getMoviesListFromApiService(moviesApiService::getUpcomingMovies)

    private suspend fun getMoviesListFromApiService(
        apiServiceFunction: suspend () -> Response<MovieList>
    ): List<Movie> {
        val response = apiServiceFunction()
        if (response.isSuccessful) {
            val body = response.body()
            val movies = body?.results
            movies?.let { return treatMovies(it).filter { movie -> movie.title != null && movie.posterPath != null } }
        }
        throw IOException()
    }

    override suspend fun getMovieDetails(movieId: Int): Movie {
        val response = moviesApiService.getMovieDetails(movieId = movieId)
        if (response.isSuccessful) {
            val movie = response.body()
            movie?.let {
                return treatMovie(movie)
            }
        }
        throw IOException()
    }

    override suspend fun searchMoviesByTerm(searchTerm: String): List<Movie> {
        val response = moviesApiService.searchMoviesByTerm(searchTerm = searchTerm)
        if (response.isSuccessful) {
            val body = response.body()
            val movies = body?.results
            movies?.let { return treatMovies(it).filter { movie -> movie.title != null && movie.posterPath != null } }
        }
        throw IOException()
    }

    private fun treatMovie(movie: Movie): Movie {

        val inputFormat = SimpleDateFormat(DATE_INPUT_FORMAT, Locale.getDefault())
        val outputFormat = SimpleDateFormat(DATE_OUTPUT_FORMAT, Locale.getDefault())
        val releaseDate = movie.releaseDate

        return Movie(
            movie.id,
            movie.title,
            movie.overview,
            if (!releaseDate.isNullOrEmpty()) outputFormat.format(inputFormat.parse(releaseDate)) else null,
            "${PREFIX_POSTER_URL}${movie.posterPath}",
            movie.voteAverage,
            movie.voteCount,
            movie.genres
        )
    }

    private fun treatMovies(movies: List<Movie>): List<Movie> {
        return movies.map { treatMovie(it) }
    }
}