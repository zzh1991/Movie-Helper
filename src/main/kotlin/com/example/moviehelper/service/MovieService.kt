package com.example.moviehelper.service

import com.example.moviehelper.constant.MovieTypeEnum
import com.example.moviehelper.dao.FilmListRepository
import com.example.moviehelper.entity.Film
import com.example.moviehelper.utils.JsoupUtils
import com.example.moviehelper.vo.Avatar
import com.example.moviehelper.vo.MovieSubject
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.common.base.Strings
import com.google.common.collect.Lists
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.tomcat.util.threads.ThreadPoolExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
class MovieService(
        val filmListRepository: FilmListRepository,
        val jsoupUtils: JsoupUtils
) {
    private val separator = ","
    private val large = "large"
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    private val executorService: ExecutorService = ThreadPoolExecutor(
        2, 2, 60, TimeUnit.SECONDS,
        LinkedBlockingQueue()
    )

    fun syncMovies(movieTypeEnum: MovieTypeEnum) {
        val filmList: List<Film> = if (MovieTypeEnum.TOP == movieTypeEnum) {
            jsoupUtils.topFilmListFromDouban()
        } else if (MovieTypeEnum.RECENT == movieTypeEnum) {
            jsoupUtils.filmListFromDouban()
        } else {
            return
        }
        if (CollectionUtils.isEmpty(filmList)) {
            return
        }
        deleteOutDatedMovie(movieTypeEnum)
        saveFilmList(filmList)
        saveDetailToMovie(movieTypeEnum)
    }

    private fun deleteOutDatedMovie(movieTypeEnum: MovieTypeEnum) {
        val filmList: List<Film> = filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
        filmList.forEach(Consumer { film: Film -> film.movieTypeEnum = MovieTypeEnum.NORMAL })
        filmListRepository.saveAll(filmList)
        log.info("set old {} {} movies to normal movies", movieTypeEnum, filmList.size)
    }

    private fun saveFilmList(movieList: List<Film>) {
        val filmList = mutableListOf<Film>()
        for (film in movieList) {
            val oldFilm = filmListRepository.findFirstByMovieId(film.movieId)
            film.transformMovieAndOldFilmToNewFilm(oldFilm)
            filmList.add(film)
        }
        batchUpdateFilmList(filmList)
    }

    private fun batchUpdateFilmList(newFilmList: List<Film>) {
        if (newFilmList.isNotEmpty()) {
            filmListRepository.saveAll(newFilmList)
            log.info("update {} movie items", newFilmList.size)
        }
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

    fun getFilmList(movieTypeEnum: MovieTypeEnum): List<Film> {
        return filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
    }

    fun getAllMoviesList(): List<Film> {
        return filmListRepository.findAllByOrderByMovieYearDescRatingDesc()
    }

    fun getFilmListById(id : Long) : Film? {
        return filmListRepository.findFirstByMovieId(id)
    }

    fun getSpecificFilmList(movieIdList: List<Long>) : List<Film> {
        return this.getFilmLists(movieIdList)
    }

    @Throws(IOException::class)
    fun getMovieSubject(id: Long?): MovieSubject? {
        val url = "https://api.douban.com/v2/movie/subject/" + id!!
        val context = getUrlContent(url)
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        val movieSubject: MovieSubject?
        try {
            movieSubject = mapper.readValue<MovieSubject>(context,
                    TypeFactory.defaultInstance().constructType(MovieSubject::class.java))
        } catch (e: Exception) {
            return null
        }
        return movieSubject
    }

    fun syncOneMovieToMovieList(movieId: Long): Film {
        val filmList = filmListRepository.findFirstByMovieId(movieId)
        val movieSubject: MovieSubject?
        var syncedMovie = Film()
        try {
            movieSubject = getMovieSubject(movieId)
        } catch (e: IOException) {
            return syncedMovie
        }

        if (movieSubject !== null) {
            syncedMovie = Film(
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

    private fun getFilmLists(movieIdList: List<Long>): List<Film> {
        val filmLists = filmListRepository.findByMovieIdIsInOrderByIdDesc(movieIdList)
        if (filmLists.isEmpty()) {
            for (movieId in movieIdList) {
                this.syncOneMovieToMovieList(movieId)
            }
        } else {
            val existedIdList = filmLists.asSequence()
                    .map(Film::movieId)
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

    private fun saveDetailToMovie(movieTypeEnum: MovieTypeEnum) {
        val filmList: List<Film> = filmListRepository.findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum)
        val newFilmList: MutableList<Film> = Lists.newArrayList()
        val completableFuture = filmList.stream()
            .filter { film: Film -> Strings.isNullOrEmpty(film.summary) }
            .map { film: Film ->
                CompletableFuture.supplyAsync(
                    {
                        film.url?.let {
                            jsoupUtils
                                .getFilmDetailByMovieTypeAndUrl(movieTypeEnum, it)
                        }
                    },
                    executorService
                ).thenApply { movieSubject: Film? ->
                    if (Objects.nonNull(movieSubject)) {
                        film.transformMovieAndOldFilmToNewFilm(movieSubject)
                        newFilmList.add(film)
                        true
                    } else {
                        false
                    }
                }
            }
            .collect(Collectors.toList())
        for (future in completableFuture) {
            try {
                val fetchStatus = future.get()
                log.warn("update summary success: {}", fetchStatus)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: java.lang.Exception) {
                log.error("get movie summary error", e)
            }
        }
        batchUpdateFilmList(newFilmList)
    }
}