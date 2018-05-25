package com.example.moviehelper

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class MovieHelperApplication

fun main(args: Array<String>) {
    runApplication<MovieHelperApplication>(*args)
}
