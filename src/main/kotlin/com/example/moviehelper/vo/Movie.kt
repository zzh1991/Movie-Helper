package com.example.moviehelper.vo

class Movie(
        _id: Long? = null,
        _rating: Rating? = null,
        _genres: List<String> = arrayListOf(),
        _title: String? = null,
        _collect_count: String? = null,
        _original_title: String? = null,
        _subtype: String? = null,
        _alt: String? = null,
        _year: Int? = null,
        _images: Map<String, String> = hashMapOf(),
        _casts: List<Avatar> = arrayListOf(),
        _directors: List<Avatar> = arrayListOf()
) {
    var id = _id
    var rating = _rating
    var genres = _genres
    var title = _title
    var collect_count = _collect_count
    var original_title = _original_title
    var subtype = _subtype
    var alt = _alt
    var year = _year
    var images = _images
    var casts = _casts
    var directors = _directors
}