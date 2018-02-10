package com.example.moviehelper.controller

import com.example.moviehelper.entity.Student
import com.example.moviehelper.service.StudentService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/student")
class StudentController(val studentService: StudentService) {
    @GetMapping("/list")
    fun getAllStudent() : List<Student> {
        return studentService.getAllStudent()
    }

    @PutMapping("/new/{name}")
    fun insertNewStudent(@PathVariable name: String) : Student {
        return studentService.insertNewStudent(name)
    }

    @GetMapping("/get/{id}")
    fun getStudentById(@PathVariable id: Long) : Student? {
        return studentService.findStudentById(id)
    }
}