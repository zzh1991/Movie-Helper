package com.example.moviehelper.utils

import com.example.moviehelper.constant.MovieTypeEnum
import com.example.moviehelper.entity.Film
import com.google.common.collect.Lists
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

@Component
class JsoupUtils {
    private val NOWPLAYING = "nowplaying"
    private val RECENT_URL = "https://movie.douban.com/cinema/nowplaying/hangzhou/"
    private val DOUBAN_URL = "https://movie.douban.com/subject/"
    private val TOP_DOUBAN_URL = "https://movie.douban.com/top250?start="
    private val PROPERTY = "property"
    private val log = LoggerFactory.getLogger(this.javaClass.name)

    fun filmListFromDouban() : List<Film> {
            val filmList: MutableList<Film> = Lists.newArrayList<Film>()
            val updateTime = LocalDateTime.now()
            try {
                val document = Jsoup.parse(URL(RECENT_URL), 30000)
                val element = document.getElementById(NOWPLAYING)
                if (Objects.isNull(element)) {
                    return filmList
                }
                val elements = element!!.getElementsByClass("list-item")
                elements.forEach(Consumer<Element> { item: Element ->
                    run {
                        val newFilm = Film(
                            _movieId = item.attr("id").toLong(),
                            _title = item.attr("data-title"),
                            _rating = item.attr("data-score").toDouble(),
                            _url = DOUBAN_URL + item.attr("id"),
                            _movieYear = item.attr("data-release").toInt(),
                            _imageLarge = getImageUrl(item),
                            _casts = getJoinString(item.attr("data-actors"), "/"),
                            _directors = getJoinString(item.attr("data-director"), " "),
                            _movieTypeEnum = MovieTypeEnum.RECENT,
                            _updateTime = updateTime,
                            _countries = getJoinString(item.attr("data-region"), " ")
                        )
                        filmList.add(newFilm)
                    }
                })
            } catch (e: Exception) {
                log.error("get recent movies error: ", e)
            }
            return filmList
        }
    fun topFilmListFromDouban() : List<Film> {
            val gap = 25
            val n = 10
            val updateTime = LocalDateTime.now()
            val filmList: MutableList<Film> = Lists.newArrayList<Film>()
            for (i in 0 until n) {
                try {
                    val document = Jsoup.parse(URL(TOP_DOUBAN_URL + (i * gap).toString()), 30000)
                    val gridView = document.getElementById("content")
                    if (Objects.isNull(gridView)) {
                        return filmList
                    }
                    val liElements = gridView!!.getElementsByTag("li")
                    filmList.addAll(
                        liElements.stream()
                            .map<Film>(Function<Element, Film> { item: Element ->
                                val aElement = item.getElementsByTag("a")[0]
                                val imgElement = aElement.getElementsByTag("img")[0]
                                var doubanUrl = aElement.attr("href")
                                doubanUrl = doubanUrl.substring(0, doubanUrl.length - 1)
                                Film(
                                    _url = doubanUrl,
                                    _movieId = doubanUrl.substring(doubanUrl.lastIndexOf("/") + 1).toLong(),
                                    _imageLarge = imgElement.attr("src"),
                                    _title = imgElement.attr("alt"),
                                    _movieTypeEnum = MovieTypeEnum.TOP,
                                    _updateTime = updateTime
                                )
                            })
                            .collect(Collectors.toList<Film>())
                    )
                } catch (e: Exception) {
                    log.error("get top movies error: ", e)
                }
            }
            return filmList
        }

    fun getFilmDetailByMovieTypeAndUrl(movieTypeEnum: MovieTypeEnum?, url: String): Film? {
        if (MovieTypeEnum.RECENT.equals(movieTypeEnum)) {
            return getFilmDetailFromUrl(url)
        } else if (MovieTypeEnum.TOP.equals(movieTypeEnum)) {
            return getTopFilmDetailFromUrl(url)
        }
        return null
    }

    private fun getFilmDetailFromUrl(url: String): Film? {
        return if (StringUtils.isBlank(url)) {
            null
        } else try {
            val document = Jsoup.parse(URL(url), 30000)
            var summary: String? = null
            var genres: String? = null
            val summaryElement = document.getElementById("link-report-intra")
            if (Objects.nonNull(summaryElement)) {
                val summaryElements = summaryElement!!.getElementsByTag("span")
                summary = StringUtils.strip(summaryElements[0].text())
            }
            val infoElement = document.getElementById("info")
            if (Objects.nonNull(infoElement)) {
                val spanElements = infoElement!!.getElementsByTag("span")
                val genreList = spanElements.stream()
                    .filter { item: Element ->
                        "v:genre" == item.attr(
                            PROPERTY
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .collect(Collectors.toList())
                genres = StringUtils.join(genreList, ",")
            }
            Film(
                _summary = summary,
                _genres = genres
            )
        } catch (e: Exception) {
            log.error("failed to get detail from url: {}", url, e)
            null
        }
    }

    private fun getTopFilmDetailFromUrl(url: String): Film? {
        return if (StringUtils.isBlank(url)) {
            null
        } else try {
            val document = Jsoup.parse(URL(url), 30000)
            var summary: String? = null
            val genres: String
            val summaryElement = document.getElementById("link-report-intra")
            if (Objects.nonNull(summaryElement)) {
                val summaryElements = summaryElement!!.getElementsByTag("span")
                summary = StringUtils.strip(summaryElements[0].text())
            }
            val ratingElement = document.getElementById("interest_sectl")
            var rating = Optional.of(0.0)
            if (Objects.nonNull(ratingElement)) {
                val strongElements = ratingElement!!.getElementsByTag("strong")
                rating = strongElements.stream()
                    .filter { item: Element ->
                        "v:average" == item.attr(
                            PROPERTY
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .map { s: String -> s.toDouble() }
                    .limit(1)
                    .findFirst()
            }
            val film: Film = Film(
                _summary = summary,
                _rating = rating.orElse(0.0)
            )
            val infoElement = document.getElementById("info")
            if (Objects.nonNull(infoElement)) {
                val spanElements = infoElement!!.getElementsByTag("span")
                val genreList = spanElements.stream()
                    .filter { item: Element ->
                        "v:genre" == item.attr(
                            PROPERTY
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .collect(Collectors.toList())
                genres = StringUtils.join(genreList, ",")
                val aElements = infoElement.getElementsByTag("a")
                val directorList = aElements.stream()
                    .filter { item: Element ->
                        "v:directedBy" == item.attr(
                            "rel"
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .collect(Collectors.toList())
                val actorList = aElements.stream()
                    .filter { item: Element ->
                        "v:starring" == item.attr(
                            "rel"
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .limit(5)
                    .collect(Collectors.toList())
                val movieYear = spanElements.stream()
                    .filter { item: Element ->
                        "v:initialReleaseDate" == item.attr(
                            PROPERTY
                        )
                    }
                    .map { obj: Element -> obj.text() }
                    .map { item: String ->
                        item.substring(0, 4).toInt()
                    }
                    .limit(1)
                    .findFirst()
                film.genres = genres
                film.directors = StringUtils.join(directorList, "")
                film.casts = StringUtils.join(actorList, "")
                film.movieYear = movieYear.orElse(0)
            }
            film
        } catch (e: Exception) {
            log.error("failed to get detail from url: {}", url, e)
            null
        }
    }

    private fun getImageUrl(item: Element): String {
        val img = item.getElementsByTag("img")
        var url = ""
        try {
            url = img[0].attr("src")
        } catch (e: Exception) {
            log.error("get image url error: {}", img.html(), e)
        }
        return url
    }

    private fun getJoinString(text: String, split: String): String {
        if (StringUtils.isBlank(text)) {
            return ""
        }
        val strings = text.split(split.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val textList: MutableList<String> = Lists.newArrayList()
        for (string in strings) {
            textList.add(string.trim { it <= ' ' })
        }
        return StringUtils.join(textList, ",")
    }
}

