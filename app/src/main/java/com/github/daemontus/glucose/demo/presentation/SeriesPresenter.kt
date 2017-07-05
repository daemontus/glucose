package com.github.daemontus.glucose.demo.presentation

/*
class SeriesPresenter(context: PresenterHost, parent: ViewGroup?) : Presenter(context, R.layout.presenter_series, parent) {

    val seriesId by NativeState(-1, longBundler)
    val episodeClicks: PublishSubject<Episode> = PublishSubject.create<Episode>()

    val repo = ShowRepository()

    val list = findView<RecyclerView>(R.id.episode_list).apply {
        this.layoutManager = LinearLayoutManager(host.activity)
    }


    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        repo.getEpisodesBySeriesId(seriesId).observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                list.adapter = EpisodeAdapter(it, host.activity, episodeClicks)
            }.until(Lifecycle.Event.DETACH)
    }

    class EpisodeAdapter(
            val episodes: List<Episode>,
            val context: Context,
            val episodeClicks: Observer<Episode>
    ) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_episode, parent, false), episodeClicks)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(episodes[position])
        }

        override fun getItemCount(): Int = episodes.size

        class ViewHolder(itemView: View, episodeClicks: Observer<Episode>) : RecyclerView.ViewHolder(itemView) {

            private val title = itemView.findViewById(R.id.episode_title) as TextView
            private val publishDate = itemView.findViewById(R.id.publish_date) as TextView

            private var episode: Episode? = null

            init {
                itemView.clicks().map { episode!! }.subscribe(episodeClicks)
            }

            fun bind(episode: Episode) {
                this.episode = episode
                this.title.text = episode.name
                this.publishDate.text = SimpleDateFormat.getDateInstance().format(episode.published)
            }

        }
    }
}*/