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
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
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
    private val log = LoggerFactory.getLogger(this.javaClass.name)


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
        val newFilmList = mutableListOf<Film>()
        deleteOutDataMovie()
        saveMovie()
        saveDetailToMovie(newFilmList)
        filmRepository.saveAll(newFilmList)
        log.info("update movie summary and country")
    }

    @Throws(IOException::class)
    fun syncTopMovies() {
        val newTopFilmList = mutableListOf<TopFilm>()
        deleteOutDataTopMovie()
        saveTopMovie()
        saveDetailToTopMovie(newTopFilmList)
        topFilmRepository.saveAll(newTopFilmList)
        log.info("update top movie summary and country")
    }

    @Throws(IOException::class)
    private fun saveMovie() {
        val url = "https://api.douban.com/v2/movie/in_theaters?city=上海"
        val movieList = getMovies(url)
        val filmList = mutableListOf<Film>()
        val filmItemList = mutableListOf<FilmList>()
        this.getRecentFilmList(movieList, filmList, filmItemList)
        filmRepository.saveAll(filmList)
        log.info("save movies")
        this.saveToMovieListDatabase(filmItemList)
        log.info("save movie list")
    }

    @Throws(IOException::class)
    private fun saveTopMovie() {
        val topUrl = "https://api.douban.com/v2/movie/top250?start=0&count=100"
        val movieList = getMovies(topUrl)
        val topFilmList = mutableListOf<TopFilm>()
        val filmItemList = mutableListOf<FilmList>()
        this.getTopFilmList(movieList, topFilmList, filmItemList)
        topFilmRepository.saveAll(topFilmList)
        log.info("save top movies")
        this.saveToMovieListDatabase(filmItemList)
        log.info("save top movie list")
    }

    private fun saveToMovieListDatabase(filmItemList: List<FilmList>) {
        for (filmItem in filmItemList) {
            val originFilm = filmListRepository.findFirstByMovieId(filmItem.id)
            if (originFilm != null) {
                filmItem.id = originFilm.id
            }
        }
        filmListRepository.saveAll(filmItemList)
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
        }
        filmRepository.saveAll(filmList)
        log.info("delete old movies")
    }

    private fun deleteOutDataTopMovie() {
        val topFilmList = topFilmRepository.findByCurrentIsTrueOrderByRatingDesc()
        topFilmList.forEach { film ->
            film.current = false
        }
        topFilmRepository.saveAll(topFilmList)
        log.info("delete old top movies")
    }

    fun getSpecificFilmList(movieIdList: List<Long>) : List<FilmList> {
        return this.getFilmLists(movieIdList)
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
    private fun saveDetailToMovie(newFilmList: MutableList<Film>) {
        val filmList = filmRepository.findByCurrentIsTrueOrderByRatingDesc()
        for (film in filmList) {
            if (film.summary.isNullOrEmpty()) {
                val movieSubject = getMovieSubject(film.movieId)
                if (movieSubject !== null) {
                    film.summary = movieSubject.summary
                    film.countries = movieSubject.countries.joinToString(separator = separator)
                    newFilmList.add(film)
                    this.saveDetailToMovieList(film.id, movieSubject)
                }
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
        log.info("update movie item summary and country")
    }

    @Throws(IOException::class)
    private fun saveDetailToTopMovie(newFilmList: MutableList<TopFilm>) {
        val topFilmList = topFilmRepository.findByCurrentIsTrueOrderByRatingDesc()
        for (topFilm in topFilmList) {
            if (topFilm.summary.isNullOrEmpty()) {
                val movieSubject = getMovieSubject(topFilm.movieId)
                if (movieSubject !== null) {
                    topFilm.summary = movieSubject.summary
                    topFilm.countries = movieSubject.countries.joinToString(separator = separator)
                    newFilmList.add(topFilm)
                    this.saveDetailToMovieList(topFilm.id, movieSubject)
                }
            }
        }
    }

    fun syncOneMovieToMovieList(movieId: Long): FilmList {
        val filmList = filmListRepository.findFirstByMovieId(movieId)
        val movieSubject: MovieSubject?
        var syncedMovie = FilmList()
        try {
            movieSubject = getMovieSubject(movieId)
        } catch (e: IOException) {
            return syncedMovie
        }

        if (movieSubject !== null) {
            syncedMovie = FilmList(
                    _movieId = movieSubject.id,
                    _title = movieSubject.title,
                    _rating = movieSubject.rating?.average,
                    _url = movieSubject.alt,
                    _movieYear = movieSubject.year,
                    _imageLarge = movieSubject.images[large],
                    _casts = getNames(movieSubject.casts),
                    _directors = getNames(movieSubject.directors),
                    _genres = movieSubject.genres.joinToString(separator = separator),
                    _countries = movieSubject.countries.joinToString(separator = separator),
                    _summary = movieSubject.summary
            )
        } else {
            return syncedMovie
        }

        if (filmList !== null) {
            syncedMovie.id = filmList.id
        }

        return filmListRepository.save(syncedMovie)
    }

    private fun getFilmLists(movieIdList: List<Long>): List<FilmList> {
        val filmLists = filmListRepository.findByMovieIdIsIn(movieIdList)
        if (filmLists.isEmpty()) {
            for (movieId in movieIdList) {
                this.syncOneMovieToMovieList(movieId)
            }
        } else {
            val existedIdList = filmLists.asSequence()
                    .map(FilmList::movieId)
                    .toList()
            for (movieId in movieIdList) {
                if (!existedIdList.contains(movieId)) {
                    this.syncOneMovieToMovieList(movieId)
                }
            }
        }
        return filmListRepository.findByMovieIdIsIn(movieIdList)
    }

    private fun getRecentFilmList(movieList: List<Movie>, filmList: MutableList<Film>,
                                  filmItemList: MutableList<FilmList>) {
        for (movie in movieList) {
            val filmItem = FilmList()
            val film = filmRepository.findFirstByMovieIdOrderByIdDesc(movie.id)
            val newFilm = Film(
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
            )
            BeanUtils.copyProperties(newFilm, filmItem)
            if (film !== null) {
                newFilm.countries = film.countries
                newFilm.summary = film.summary
                BeanUtils.copyProperties(newFilm, filmItem)
                newFilm.id = film.id
            }
            filmList.add(newFilm)
            filmItemList.add(filmItem)
        }
    }

    private fun getTopFilmList(movieList: List<Movie>, filmList: MutableList<TopFilm>,
                               filmItemList: MutableList<FilmList>) {
        for (movie in movieList) {
            val filmItem = FilmList()
            val film = topFilmRepository.findFirstByMovieIdOrderByIdDesc(movie.id)
            val newFilm = TopFilm(
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
            )
            BeanUtils.copyProperties(newFilm, filmItem)
            if (film !== null) {
                newFilm.countries = film.countries
                newFilm.summary = film.summary
                BeanUtils.copyProperties(newFilm, filmItem)
                newFilm.id = film.id
            }
            filmList.add(newFilm)
            filmItemList.add(filmItem)
        }
    }
}