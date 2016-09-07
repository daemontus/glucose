package com.github.daemontus.glucose.demo.domain

import com.github.daemontus.glucose.demo.data.Show
import com.github.daemontus.glucose.demo.data.showList
import rx.Observable

class ShowRepository {

    fun getShows(): Observable<List<Show>> {
        return Observable.just(showList)
    }

}