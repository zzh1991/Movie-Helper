package com.example.moviehelper.schedule

import com.example.moviehelper.service.MovieService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduleTask(val movieService: MovieService) {
    companion object {
        private val log = LoggerFactory.getLogger(ScheduleTask::class.java)
    }

    @Scheduled(cron = "0 0 22 * * ?", zone = "GMT+8")
    fun updateMovie() {
        try {
            movieService.syncRecentMovies()
            log.info("update movie successfully")
        } catch (e: Exception) {
            log.error("update movie failed: {}", e.message)
        }

    }

    @Scheduled(cron = "0 30 22 * * ?", zone = "GMT+8")
    fun updateTopMovie() {
        try {
            movieService.syncTopMovies()
            log.info("update top movie successfully")
        } catch (e: Exception) {
            log.error("update top movie failed: {}", e.message)
        }

    }
}