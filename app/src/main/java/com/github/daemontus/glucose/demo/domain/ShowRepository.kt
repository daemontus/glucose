package com.github.daemontus.glucose.demo.domain

import com.github.daemontus.glucose.demo.data.Series
import com.github.daemontus.glucose.demo.data.Show
import com.github.daemontus.glucose.demo.data.series
import com.github.daemontus.glucose.demo.data.showList
import rx.Observable

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

}