package com.example.moviehelper.vo

class MovieSubject(
        _id: Long? = null,
        _rating: Rating? = null,
        _reviews_count: Long? = null,
        _wish_count: Long? = null,
        _douban_site: String? = null,

        _genres: List<String> = arrayListOf(),
        _title: String? = null,
        _collect_count: String? = null,
        _original_title: String? = null,
        _subtype: String? = null,
        _alt: String? = null,
        _year: Int? = null,
        _images: Map<String, String> = hashMapOf(),
        _casts: List<Avatar> = arrayListOf(),
        _directors: List<Avatar> = arrayListOf(),

        _mobile_url: String? = null,
        _share_url: String? = null,
        _schedule_url: String? = null,

        _do_count: Long? = null,
        _seasons_count: Long? = null,
        _episodes_count: Long? = null,
        _comments_count: Long? = null,
        _ratings_count: Long? = null,

        _countries: List<String> = arrayListOf(),
        _current_season: String? = null,
        _summary: String? = null,
        _aka: List<String>? = null
) {
    var id = _id
    var rating = _rating
    var reviews_count = _reviews_count
    var wish_count = _wish_count
    var douban_site = _douban_site

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

    var mobile_url = _mobile_url
    var share_url = _share_url
    var schedule_url = _schedule_url

    var do_count = _do_count
    var seasons_count = _seasons_count
    var episodes_count = _episodes_count
    var comments_count = _comments_count
    var ratings_count = _ratings_count

    var countries = _countries
    var current_season = _current_season
    var summary = _summary
    var aka = _aka
}