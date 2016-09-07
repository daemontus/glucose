package com.github.daemontus.glucose.demo.data

data class Show(
        val id: Long,
        val name: String,
        val imageUrl: String
)

val showList = listOf(
        Show(1, "BoJack Horseman", "http://www.indiewire.com/wp-content/uploads/2016/05/bojack-horseman-2.jpg"),
        Show(2, "Mr. Robot", "http://serialdog.eu/wp-content/uploads/2015/12/Mr.-Robot.jpg"),
        Show(3, "Rick and Morty", "http://cdn3.thr.com/sites/default/files/2015/07/rick_and_morty_s02_still.jpg"),
        Show(4, "House of Cards", "http://esq.h-cdn.co/assets/15/10/1425318206-house-of-cards-season-3-thumb-image.jpg"),
        Show(5, "Sherlock", "http://d2buyft38glmwk.cloudfront.net/media/images/canonical/mast-Sherlock-Benedict-Martin-COVE-hires.jpg")
)