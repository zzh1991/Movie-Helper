package com.example.moviehelper.dao

import com.example.moviehelper.entity.ViewFilm
import org.springframework.data.repository.CrudRepository

interface ViewFilmRepository : CrudRepository<ViewFilm, Long> {
    fun findOneByMovieId(id: Long?): ViewFilm

    override fun findAll(): List<ViewFilm>
}