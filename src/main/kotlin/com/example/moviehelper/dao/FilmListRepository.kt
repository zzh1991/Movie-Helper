package com.example.moviehelper.dao

import com.example.moviehelper.entity.FilmList
import org.springframework.data.repository.CrudRepository

interface FilmListRepository : CrudRepository<FilmList, Long> {
    fun findFirstByMovieId(movieId: Long?): FilmList?

    fun findByMovieIdIsIn(ids: List<Long>): List<FilmList>
}
