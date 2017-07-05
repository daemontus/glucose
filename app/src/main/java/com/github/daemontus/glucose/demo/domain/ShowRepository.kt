package com.github.daemontus.glucose.demo.domain

import com.github.daemontus.glucose.demo.data.*
import io.reactivex.Observable
import java.util.*

class ShowRepository {

    fun getShows(): Observable<List<Show>> {
        return Observable.just(showList)
    }

    fun getShowById(id: Long): Observable<Show?> {
        return Observable.just(showList.find { it.id == id })
    }

    fun getSeriesByShow(show: Show): Observable<List<Series>> {
        return Observable.just(series[show]!!)
    }

    fun getEpisodesBySeriesId(seriesId: Long): Observable<List<Episode>> {
        return Observable.just(generateEpisodes(seriesId))
    }

    private fun generateEpisodes(seriesId: Long): List<Episode> {
        val count = ((seriesId.hashCode() + 13) * 31 % 17) + 2
        return (1..count).map { Episode("Episode $it", Date(Random().nextLong() % System.currentTimeMillis())) }
    }

}