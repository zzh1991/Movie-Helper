package com.example.moviehelper.vo

class Avatar(
        _id: Long? = null,
        _name: String? = null,
        _alt: String? = null,
        _avatars: Map<String, String>? = hashMapOf()
) {
    var id = _id
    var name = _name
    var alt = _alt
    var avatars = _avatars
}