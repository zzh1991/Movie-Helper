package com.example.moviehelper.vo

class Rating(
        _max: Int? = null,
        _average: Double? = null,
        _stars: Int? = null,
        _min: Int? = null
) {
    var max = _max
    var average =_average
    var stars = _stars
    var min = _min
}