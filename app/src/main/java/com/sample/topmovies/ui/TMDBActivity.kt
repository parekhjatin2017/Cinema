package com.sample.topmovies.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.sample.topmovies.Constants
import com.sample.topmovies.model.Movie
import com.sample.topmovies.ui.theme.JetpackcomposeTheme
import com.sample.topmovies.viewModel.ApiStatus
import com.sample.topmovies.viewModel.TMDBViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TMDBActivity : ComponentActivity() {

    private val tmdbViewModel : TMDBViewModel by viewModels()
    private var isDetailShowing = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetpackcomposeTheme {
                tmdbViewModel.getPopularMovies(Constants.API_KEY)
                RegisterStateFlow(TMDBViewModel = tmdbViewModel, context = applicationContext, {
                    isDetailShowing = true
                    tmdbViewModel.getMovieDetail(Constants.API_KEY, it.id.toString())
                }){
                    isDetailShowing = false
                    tmdbViewModel.getPopularMovies(Constants.API_KEY)
                }
            }
        }
    }

    override fun onBackPressed() {
        if(isDetailShowing){
            isDetailShowing = false
            tmdbViewModel.getPopularMovies(Constants.API_KEY)
        }else{
            super.onBackPressed()
        }
    }
}

@Composable
fun RegisterStateFlow(TMDBViewModel: TMDBViewModel, context: Context, clickAction: (Movie) -> Unit, back: () -> Unit){

        when( val state = TMDBViewModel.movieStateFlow.collectAsState().value){
            is ApiStatus.Loading->{
                Toast.makeText(context, "Loading data...", Toast.LENGTH_SHORT).show()
            }
            is ApiStatus.Failure -> {
                Toast.makeText(context, state.msg.message, Toast.LENGTH_SHORT).show()
                val activity = (LocalContext.current as? Activity)
                activity?.finish()
            }
            is ApiStatus.MovieListSuccess->{
                MovieList(state.data.results, clickAction)
            }
            is ApiStatus.MovieDetailSuccess->{
                //Toast.makeText(context, state.data.toString(), Toast.LENGTH_SHORT).show()
                OpenDetailScreen(state.data, back)
            }
            is ApiStatus.Idle-> {}
        }

}



