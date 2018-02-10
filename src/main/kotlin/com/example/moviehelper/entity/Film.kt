package com.example.moviehelper.entity

import javax.persistence.*

@Entity
@Table(name = "movie")
class Film(
        _id : Long? = null,
        _movieId: Long? = null,
        _title: String? = null,
        _rating: Double? = null,
        _movieYear: Int? = null,
        _url: String? = null,
        _imageLarge: String? = null,
        _casts: String? = null,
        _directors: String? = null,
        _genres: String? = null,
        _summary: String? = null,
        _countries: String? = null,
        _current: Boolean? = false,
        _viewed: Boolean? = false,
        _star: Boolean? = false,
        _updateTime: Long? = null
        ) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = _id
    var movieId = _movieId
    var title = _title
    var rating = _rating
    var movieYear = _movieYear
    var url = _url
    var imageLarge = _imageLarge
    var casts = _casts
    var directors = _directors
    var genres = _genres
    var summary = _summary
    var countries = _countries
    var current = _current
    var viewed = _viewed
    var star = _star
    var updateTime = _updateTime
}