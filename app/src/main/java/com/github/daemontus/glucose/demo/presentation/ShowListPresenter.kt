package com.github.daemontus.glucose.demo.presentation

/*
class ShowListPresenter(context: PresenterHost, parent: ViewGroup?)
    : Presenter(context, R.layout.presenter_show_list, parent) {

    val showClickSubject: PublishSubject<Show> = PublishSubject.create<Show>()

    val showList = findView<RecyclerView>(R.id.show_list).apply {
        this.layoutManager = LinearLayoutManager(host.activity)
    }

    val repository = ShowRepository()

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        repository.getShows().observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showList.adapter = ShowAdapter(it, host.activity, showClickSubject)
            }
    }

    class ShowAdapter(
            val items: List<Show>,
            val host: Context,
            val clickObserver: Observer<Show>
    ) : RecyclerView.Adapter<ShowAdapter.ShowHolder>() {

        override fun onBindViewHolder(holder: ShowHolder, position: Int) {
            holder.render(items[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ShowHolder
                = ShowHolder(LayoutInflater.from(host).inflate(R.layout.item_show, parent, false), clickObserver)

        override fun getItemCount(): Int {
            return items.size
        }

        class ShowHolder(
                itemView: View, clickObserver: Observer<Show>
        ) : RecyclerView.ViewHolder(itemView) {
            val image = itemView.findViewById(R.id.show_image) as ImageView
            val title = itemView.findViewById(R.id.show_title) as TextView

            private var item: Show? = null

            fun render(show: Show) {
                item = show

                title.text = show.name
            }

            init {
                itemView.clicks().map { item!! }.subscribe(clickObserver)
            }
        }
    }
}*/