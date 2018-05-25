package com.example.moviehelper.dao

import com.example.moviehelper.entity.Film
import org.springframework.data.repository.CrudRepository

interface FilmRepository : CrudRepository<Film, Long> {
    fun findByCurrentIsTrueOrderByRatingDesc(): List<Film>

    fun findFirstByMovieIdOrderByIdDesc(id: Long?): Film?
}