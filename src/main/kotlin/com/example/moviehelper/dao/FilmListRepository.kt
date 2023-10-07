package com.example.moviehelper.dao

import com.example.moviehelper.constant.MovieTypeEnum
import com.example.moviehelper.entity.Film
import org.springframework.data.repository.CrudRepository

interface FilmListRepository : CrudRepository<Film, Long> {
    fun findFirstByMovieId(movieId: Long?): Film?

    fun findByMovieIdIsInOrderByIdDesc(ids: List<Long>): List<Film>

    fun findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum: MovieTypeEnum): List<Film>

    fun findAllByOrderByMovieYearDescRatingDesc(): List<Film>
}
