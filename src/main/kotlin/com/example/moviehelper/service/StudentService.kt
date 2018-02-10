package com.example.moviehelper.service

import com.example.moviehelper.dao.StudentRepository
import com.example.moviehelper.entity.Student
import org.springframework.stereotype.Service

@Service
class StudentService(val studentRepository: StudentRepository) {
    fun getAllStudent() : List<Student> {
        return studentRepository.findAll()
    }

    fun insertNewStudent(name: String) : Student {
        return studentRepository.save(Student(_name = name))
    }

    fun findStudentById(id : Long) : Student? {
        return studentRepository.getById(id)
    }
}