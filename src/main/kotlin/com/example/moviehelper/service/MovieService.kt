package com.example.moviehelper.service

import com.example.moviehelper.dao.FilmListRepository
import com.example.moviehelper.dao.FilmRepository
import com.example.moviehelper.dao.TopFilmRepository
import com.example.moviehelper.entity.Film
import com.example.moviehelper.entity.FilmList
import com.example.moviehelper.entity.TopFilm
import com.example.moviehelper.vo.Avatar
import com.example.moviehelper.vo.Movie
import com.example.moviehelper.vo.MovieSubject
import com.example.moviehelper.vo.MovieVo
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class MovieService(
        val filmRepository: FilmRepository,
        val topFilmRepository: TopFilmRepository,
        val filmListRepository: FilmListRepository
) {
    private val separator = ","
    private val large = "large"

    @Throws(IOException::class)
    fun getMovies(url: String): List<Movie> {
        var movieVo: MovieVo?
        val context = getUrlContent(url)
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        movieVo = mapper.readValue<MovieVo>(context,
                TypeFactory.defaultInstance().constructType(MovieVo::class.java))
        return movieVo.subjects
    }

    @Throws(IOException::class)
    private fun getUrlContent(url: String): String {
        val client = OkHttpClient()

        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()
        return response.body()!!.string()
    }

    @Throws(IOException::class)
    fun syncRecentMovies() {
        deleteOutDataMovie()
        saveMovie()
        saveDetailToMovie()
    }

    @Throws(IOException::class)
    private fun saveMovie() {
        val url = "https://api.douban.com/v2/movie/in_theaters?city=上海"
        val movieList = getMovies(url)
        movieList.forEach { movie ->
            filmRepository.save(Film(
                    _movieId = movie.id,
                    _title = movie.title,
                    _rating = movie.rating?.average,
                    _url = movie.alt,
                    _movieYear = movie.year,
                    _imageLarge = movie.images[large],
                    _casts = getNames(movie.casts),
                    _directors = getNames(movie.directors),
                    _genres = movie.genres.joinToString(separator = separator),
                    _current = true
            ))
            saveToMovieListDatabase(movie)
        }
    }

    @Throws(IOException::class)
    private fun saveTopMovie() {
        val topUrl = "https://api.douban.com/v2/movie/top250?start=0&count=100"
        val movieList = arrayListOf<Movie>()
        movieList.addAll(getMovies(topUrl))

        movieList.forEach { movie ->
            topFilmRepository.save(TopFilm(
                    _movieId = movie.id,
                    _title = movie.title,
                    _rating = movie.rating?.average,
                    _url = movie.alt,
                    _movieYear = movie.year,
                    _imageLarge = movie.images[large],
                    _casts = getNames(movie.casts),
                    _directors = getNames(movie.directors),
                    _genres = movie.genres.joinToString(separator = separator),
                    _current = true
            ))
            saveToMovieListDatabase(movie)
        }
    }

    private fun saveToMovieListDatabase(movie: Movie) {
        val filmList = FilmList(
                _movieId = movie.id,
                _title = movie.title,
                _rating = movie.rating?.average,
                _url = movie.alt,
                _movieYear = movie.year,
                _imageLarge = movie.images[large],
                _casts = getNames(movie.casts),
                _directors = getNames(movie.directors),
                _genres = movie.genres.joinToString(separator = separator)
                )

        val originFilm = filmListRepository.findFirstByMovieId(movie.id)
        if (originFilm !== null) {
            filmList.id = originFilm.id
        }
        filmListRepository.save(filmList)
    }

    private fun getNames(avatars: List<Avatar>): String {
        val nameList = avatars.asSequence().map { it.name!! }.toList()
        return nameList.joinToString(separator = separator)
    }

    fun getFilmList(): List<Film> {
        return filmRepository.findByCurrentIsTrueOrderByRatingDesc()
    }

    fun getTopFilmList(): List<TopFilm> {
        return topFilmRepository.findByCurrentIsTrueOrderByRatingDesc()
    }

    fun getFilmListById(id : Long) : FilmList? {
        return filmListRepository.findFirstByMovieId(id)
    }

    private fun deleteOutDataMovie() {
        val filmList = filmRepository.findByCurrentIsTrueOrderByRatingDesc()
        filmList.forEach { film ->
            film.current = false
            filmRepository.save(film)
        }
    }

    private fun deleteOutDataTopMovie() {
        val topFilmList = topFilmRepository.findByCurrentIsTrueOrderByRatingDesc()
        topFilmList.forEach { film ->
            film.current = false
            topFilmRepository.save(film)
        }
    }

    fun getSepecificFilmList(movieIdList : List<Long>) : List<FilmList> {
        return filmListRepository.findByMovieIdIsIn(movieIdList)
    }

    @Throws(IOException::class)
    fun getMovieSubject(id: Long?): MovieSubject? {
        val url = "https://api.douban.com/v2/movie/subject/" + id!!
        val context = getUrlContent(url)
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        var movieSubject: MovieSubject?
        try {
            movieSubject = mapper.readValue<MovieSubject>(context,
                    TypeFactory.defaultInstance().constructType(MovieSubject::class.java))
        } catch (e: Exception) {
            return null
        }

        return movieSubject
    }

    @Throws(IOException::class)
    private fun saveDetailToMovie() {
        val filmList = filmRepository.findByCurrentIsTrueOrderByRatingDesc()
        for (film in filmList) {
            val movieSubject = getMovieSubject(film.movieId)
            if (movieSubject !== null) {
                film.summary = movieSubject.summary
                film.countries = movieSubject.countries.joinToString(separator = separator)
                filmRepository.save(film)
                saveDetailToMovieList(film.movieId, movieSubject)
            }
        }
    }

    @Throws(IOException::class)
    private fun saveDetailToMovieList(id: Long?, movieSubject: MovieSubject) {
        val filmList = filmListRepository.findFirstByMovieId(id)
        if (filmList !== null) {
            filmList.summary = movieSubject.summary
            filmList.countries = movieSubject.countries.joinToString(separator = separator)
            filmListRepository.save(filmList)
        }
    }

    @Throws(IOException::class)
    private fun saveDetailToTopMovie() {
        val topFilmList = topFilmRepository.findByCurrentIsTrueOrderByRatingDesc()
        for (topFilm in topFilmList) {
            val movieSubject = getMovieSubject(topFilm.movieId)
            if (movieSubject !== null) {
                topFilm.summary = movieSubject.summary
                topFilm.countries = movieSubject.countries.joinToString(separator = separator)
                topFilmRepository.save(topFilm)
                saveDetailToMovieList(topFilm.movieId, movieSubject)
            }
        }
    }
}