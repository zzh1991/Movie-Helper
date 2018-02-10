package com.example.moviehelper.entity

import javax.persistence.*

@Entity
@Table(name = "student")
class Student(_id : Long? = null, _name : String? = null) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = _id

    var name: String? = _name
}