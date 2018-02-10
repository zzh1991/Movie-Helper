package com.example.moviehelper.dao

import com.example.moviehelper.entity.Student
import org.springframework.data.repository.CrudRepository

interface StudentRepository : CrudRepository<Student, Long> {
    override fun findAll(): List<Student>

    fun getById(id: Long?): Student?
}