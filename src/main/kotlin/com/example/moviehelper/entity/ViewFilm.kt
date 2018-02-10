package com.example.moviehelper.entity

import javax.persistence.*

@Entity
@Table(name = "view_movie")
class ViewFilm(
        _id : Long? = null,
        _movieId: Long? = null,
        _time: String? = null,
        _viewed: Boolean? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = _id
    var movieId = _movieId
    var time = _time
    var viewed = _viewed
}