package com.example.moviehelper.dao

import com.example.moviehelper.entity.TopFilm
import org.springframework.data.repository.CrudRepository

interface TopFilmRepository : CrudRepository<TopFilm, Long> {
    fun findByCurrentIsTrueOrderByRatingDesc(): List<TopFilm>

    fun findFirstByMovieIdOrderByIdDesc(id: Long?): TopFilm?
}