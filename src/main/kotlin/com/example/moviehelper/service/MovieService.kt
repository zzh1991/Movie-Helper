package com.example.moviehelper.service

import com.example.moviehelper.constant.MovieTypeEnum
import com.example.moviehelper.dao.FilmListRepository
import com.example.moviehelper.entity.FilmList
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
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.concurrent.CompletableFuture

@Service
class MovieService(
        val filmListRepository: FilmListRepository
) {
    private val separator = ","
    private val large = "large"
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    fun syncMovies(movieTypeEnum: MovieTypeEnum) {
        saveMovies(movieTypeEnum)
        saveDetailToMovie(movieTypeEnum)
    }

    private fun saveMovies(movieTypeEnum: MovieTypeEnum) {
        var url = "https://api.douban.com/v2/movie/in_theaters?city=上海"
        if (MovieTypeEnum.TOP == movieTypeEnum) {
            url = "https://api.douban.com/v2/movie/top250?start=0&count=100"
        }
        val movieList = getMovies(url)
        if (movieList.isNotEmpty()) {
            this.deleteOutDataMovie(movieTypeEnum)
        }
        this.saveFilmList(movieList, movieTypeEnum)
    }

    private fun deleteOutDataMovie(movieTypeEnum: MovieTypeEnum) {
        val filmList = filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
        filmList.forEach { film -> film.movieTypeEnum = MovieTypeEnum.NORMAL }
        filmListRepository.saveAll(filmList)
        log.info("set old recent {} movies to normal movies", filmList.size)
    }

    private fun saveFilmList(movieList: List<Movie>, movieTypeEnum: MovieTypeEnum) {
        val filmList = mutableListOf<FilmList>()
        for (movie in movieList) {
            val film = filmListRepository.findFirstByMovieId(movie.id)
            val newFilm = FilmList(
                    _movieId = movie.id,
                    _title = movie.title,
                    _rating = movie.rating?.average,
                    _url = movie.alt,
                    _movieYear = movie.year,
                    _imageLarge = movie.images[large],
                    _casts = getNames(movie.casts),
                    _directors = getNames(movie.directors),
                    _genres = movie.genres.joinToString(separator = separator),
                    _movieTypeEnum = movieTypeEnum
            )
            if (film !== null) {
                newFilm.countries = film.countries
                newFilm.summary = film.summary
                newFilm.id = film.id
            }
            filmList.add(newFilm)
        }
        batchUpdateFilmList(filmList)
    }

    private fun batchUpdateFilmList(newFilmList: List<FilmList>) {
        if (newFilmList.isNotEmpty()) {
            filmListRepository.saveAll(newFilmList)
            log.info("update {} movie items", newFilmList.size)
        }
    }

    @Throws(IOException::class)
    private fun saveDetailToMovie(movieTypeEnum: MovieTypeEnum) {
        val filmList = filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
        val newFilmList = mutableListOf<FilmList>()
        val completableFuture = filmList.asSequence()
                .filter { film -> film.summary.isNullOrEmpty() }
                .map { film -> CompletableFuture.supplyAsync {
                    getMovieSubject(film.movieId)
                }
                        .thenApply { getDetail(it, film, newFilmList); it }
                }


        for (future in completableFuture) {
            try {
                val fetchStatus = future.get()
                log.warn("update summary success: {}", fetchStatus)
            } catch (e: Exception) {
                log.error("get movie summary error")
            }
        }

        batchUpdateFilmList(newFilmList)
    }

    private fun getDetail(movieSubject: MovieSubject?, film: FilmList, newFilmList: MutableList<FilmList>): Boolean {
        if (movieSubject !== null) {
            film.summary = movieSubject.summary
            film.countries = movieSubject.countries.joinToString(separator = separator)
            newFilmList.add(film)
            return true
        } else {
            return false
        }
    }

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
        return response.body!!.string()
    }

    private fun getNames(avatars: List<Avatar>): String {
        val nameList = avatars.asSequence().map { it.name!! }.toList()
        return nameList.joinToString(separator = separator)
    }

    fun getFilmList(movieTypeEnum: MovieTypeEnum): List<FilmList> {
        return filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
    }

    fun getAllMoviesList(): List<FilmList> {
        return filmListRepository.findAllByOrderByMovieYearDescRatingDesc()
    }

    fun getFilmListById(id : Long) : FilmList? {
        return filmListRepository.findFirstByMovieId(id)
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
                    _summary = movieSubject.summary,
                    _movieTypeEnum = MovieTypeEnum.NORMAL
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
        val filmLists = filmListRepository.findByMovieIdIsInOrderByIdDesc(movieIdList)
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
        return filmListRepository.findByMovieIdIsInOrderByIdDesc(movieIdList)
                .asSequence()
                .distinctBy { it.movieId }
                .sortedByDescending { it.rating }
                .toList()
    }
}