package com.github.daemontus.glucose.demo.presentation

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.daemontus.glucose.demo.R
import com.github.daemontus.glucose.demo.data.Show
import com.github.daemontus.glucose.demo.domain.ShowRepository
import com.glucose.app.Presenter
import com.glucose.app.PresenterContext
import com.glucose.app.presenter.findView
import com.jakewharton.rxbinding.view.clicks
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject

class ShowListPresenter(context: PresenterContext, parent: ViewGroup?)
    : Presenter(context, R.layout.presenter_show_list, parent) {

    val showClickSubject: PublishSubject<Show> = PublishSubject.create<Show>()

    val showList = findView<RecyclerView>(R.id.show_list).apply {
        this.layoutManager = LinearLayoutManager(ctx.activity)
    }

    val repository = ShowRepository()

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        repository.getShows().observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                showList.adapter = ShowAdapter(it, ctx.activity, showClickSubject)
            }
    }

    class ShowAdapter(
            val items: List<Show>,
            val ctx: Context,
            val clickObserver: Observer<Show>
    ) : RecyclerView.Adapter<ShowAdapter.ShowHolder>() {

        override fun onBindViewHolder(holder: ShowHolder, position: Int) {
            holder.render(items[position])
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ShowHolder
                = ShowHolder(LayoutInflater.from(ctx).inflate(R.layout.item_show, parent, false), clickObserver)

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
}