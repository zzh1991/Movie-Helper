package com.example.moviehelper.dao

import com.example.moviehelper.constant.MovieTypeEnum
import com.example.moviehelper.entity.FilmList
import org.springframework.data.repository.CrudRepository

interface FilmListRepository : CrudRepository<FilmList, Long> {
    fun findFirstByMovieId(movieId: Long?): FilmList?

    fun findByMovieIdIsInOrderByIdDesc(ids: List<Long>): List<FilmList>

    fun findByMovieTypeEnumOrderByRatingDesc(movieTypeEnum: MovieTypeEnum): List<FilmList>

    fun findAllByOrderByMovieYearDescRatingDesc(): List<FilmList>
}
