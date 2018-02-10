package com.example.moviehelper.vo

class MovieVo(
        _count: Int? = null,
        _start: Int? = null,
        _total: Int? = null,
        _subjects: List<Movie> = arrayListOf(),
        _title: String? = null
) {
    var count = _count
    var start = _start
    var total = _total
    var subjects = _subjects
    var title = _title
}