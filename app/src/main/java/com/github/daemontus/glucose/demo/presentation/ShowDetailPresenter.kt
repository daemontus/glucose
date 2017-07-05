package com.github.daemontus.glucose.demo.presentation

/*
class ShowDetailPresenter(context: PresenterHost, parent: ViewGroup?) : PresenterGroup(context, R.layout.presenter_show_detail, parent) {

    val showId: Long by NativeState(-1, longBundler)

    val repo = ShowRepository()

    val showTitle = findView<TextView>(R.id.show_title)
    val pager = findView<ViewPager>(R.id.show_list_pager)
    val tabs = findView<TabLayout>(R.id.show_list_tabs)

    init {
        tabs.setupWithViewPager(pager, true)
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        repo.getShowById(showId).observeOn(AndroidSchedulers.mainThread())
            .subscribe { show ->
                if (show == null) {
                    //Normally, you would display some kind of error, right? ;)
                    host.activity.onBackPressed()
                } else {
                    showTitle.text = show.name
                    loadSeries(show)
                }
            }.until(Lifecycle.Event.DETACH)
    }

    override fun onDetach() {
        pager.adapter = null
        showTitle.text = ""
        super.onDetach()
    }

    private fun loadSeries(show: Show) {
        repo.getSeriesByShow(show)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { series ->
                pager.adapter = null
                val adapter = SeriesAdapter(series, this)
                pager.adapter = adapter
            }
    }

    class SeriesAdapter(
            private val series: List<Series>,
            private val presenter: PresenterGroup
    ) : PagerAdapter() {

        private val presenters = arrayOfNulls<Presenter?>(series.size)

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val child = presenter.attach(container, SeriesPresenter::class.java,
                    (SeriesPresenter::seriesId.name with series[position].seriesId) and
                            (Presenter::canReattachAfterStateChange.name with false)
            )
            presenters[position]?.let { presenter.detach(it) }
            presenters[position] = child
            return child.view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
            presenters[position]?.let { presenter.detach(it) }
            presenters[position] = null
            container.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence = series[position].name

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean = `object` == view

        override fun getCount(): Int = series.size

    }
}*/