package com.github.daemontus.glucose.demo.data

data class Series(
        val showId: Long,
        val seriesId: Long,
        val name: String
)

val series = mapOf(
        showList[0] to listOf(Series(1, 1, "Series 1"), Series(1, 2, "Series 2"), Series(1, 3, "Series 3")),
        showList[1] to listOf(Series(1, 4, "Series 1"), Series(1, 5, "Series 2")),
        showList[2] to listOf(Series(1, 6, "Series 1"), Series(1, 7, "Series 2")),
        showList[3] to listOf(Series(1, 8, "Series 1"), Series(1, 9, "Series 2"), Series(1, 10, "Series 3")),
        showList[4] to listOf(Series(1, 11, "Series 1"), Series(1, 12, "Series 2"), Series(1, 13, "Series 3"))
)