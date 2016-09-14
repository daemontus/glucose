package com.github.daemontus.glucose.demo.data

data class Show(
        val id: Long,
        val name: String
)

val showList = listOf(
        Show(1, "Show 1"),
        Show(2, "Show 2"),
        Show(3, "Show 3"),
        Show(4, "Show 4"),
        Show(5, "Show 5")
)