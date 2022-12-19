package com.sample.topmovies.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.topmovies.model.MovieList
import com.sample.topmovies.repository.MoviesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TMDBViewModel
@Inject
constructor(private val mainRepository: MoviesRepository) : ViewModel() {

    private val _movieStateFlow:MutableStateFlow<ApiStatus> = MutableStateFlow(ApiStatus.Idle)
    val movieStateFlow:StateFlow<ApiStatus> = _movieStateFlow

    private var  movieList : MutableStateFlow<MovieList?> = MutableStateFlow(null)

    fun getPopularMovies(apiKey : String) = viewModelScope.launch {

        movieList.let {
            when (it.value) {
                null -> {
                    _movieStateFlow.value = ApiStatus.Loading
                    mainRepository.getPopularMovies(apiKey)
                        .catch { e->
                            _movieStateFlow.value = ApiStatus.Failure(e)
                        }.collect { data->
                            movieList.value = data
                            _movieStateFlow.value = ApiStatus.MovieListSuccess(data)
                        }
                }else -> {
                    _movieStateFlow.value = ApiStatus.MovieListSuccess(it.value!!)
                }
            }
        }
    }

    fun getMovieDetail(apiKey : String, movieId: String) = viewModelScope.launch {
        _movieStateFlow.value = ApiStatus.Loading
        mainRepository.getMovieDetail(apiKey, movieId)
            .catch { e->
                _movieStateFlow.value = ApiStatus.Failure(e)
            }.collect { data->
                _movieStateFlow.value = ApiStatus.MovieDetailSuccess(data)
            }
    }
}