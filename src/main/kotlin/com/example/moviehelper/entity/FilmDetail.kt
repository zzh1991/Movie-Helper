package com.example.moviehelper.entity

import javax.persistence.*

@Entity
@Table(name = "movie_detail")
class FilmDetail(
        _id : Long? = null,
        _movieId: Long? = null,
        _summary: String? = null,
        _countries: String? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = _id
    var movieId = _movieId
    var summary = _summary
    var countries = _countries
}