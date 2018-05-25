package com.example.moviehelper.controller

import com.example.moviehelper.entity.Film
import com.example.moviehelper.entity.FilmList
import com.example.moviehelper.entity.TopFilm
import com.example.moviehelper.service.MovieService
import com.example.moviehelper.vo.MovieSubject
import org.springframework.web.bind.annotation.*
import java.io.IOException

@RestController
class MovieController(val movieService: MovieService) {
    @PostMapping("/sync/recent")
    @Throws(IOException::class)
    fun syncRecent() {
        movieService.syncRecentMovies()
    }

    @PostMapping("sync/top")
    @Throws(IOException::class)
    fun syncTop() {
        movieService.syncTopMovies()
    }

    @GetMapping("/movie/recent")
    @Throws(IOException::class)
    fun getRecentMovie(): List<Film> {
        return movieService.getFilmList()
    }

    @GetMapping("/movie/top250")
    @Throws(IOException::class)
    fun getTopMovie(): List<TopFilm> {
        return movieService.getTopFilmList()
    }

    @GetMapping("/movie/subject/{id}")
    @Throws(IOException::class)
    fun getMovieSubject(@PathVariable id: Long?): MovieSubject {
        return movieService.getMovieSubject(id)!!
    }

    @GetMapping("/movie/viewed/{id}/{viewed}")
    fun updateMovieViewedState(@PathVariable id: Long?, @PathVariable viewed: Boolean?): Boolean? {
        return true
    }

    @GetMapping("/list/{id}")
    fun getFilmListById(@PathVariable id: Long?): FilmList? {
        return movieService.getFilmListById(id!!)
    }

    @PostMapping("/movie/star")
    fun getStarList(@RequestBody movieIdList: List<Long>): List<FilmList> {
        return movieService.getSpecificFilmList(movieIdList)
    }

    @PostMapping("/movie/viewed")
    fun getViewedList(@RequestBody movieIdList: List<Long>): List<FilmList> {
        return movieService.getSpecificFilmList(movieIdList)
    }

    @PostMapping("movie/sync/{movieId}")
    fun syncOneMovieToMovieList(@PathVariable movieId: Long): FilmList {
        return movieService.syncOneMovieToMovieList(movieId)
    }
}