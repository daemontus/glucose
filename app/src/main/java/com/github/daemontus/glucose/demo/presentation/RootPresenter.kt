package com.github.daemontus.glucose.demo.presentation

/*
class RootPresenter(context: PresenterHost, parent: ViewGroup?) : PresenterGroup(context, R.layout.presenter_root, parent) {

    val startButton = findView<MultiStateButton>(R.id.start_action_button).apply {
        this.setOnClickListener {
            if (this.state == this.backButtonState) {
                host.activity.onBackPressed()
            }
        }
    }

    init {

        Observable.merge(this.onChildAdd, this.onChildRemove)
                .observeOn(AndroidSchedulers.mainThread())
                .whileIn(Lifecycle.State.ALIVE)
                .subscribe {
                    startButton.state = if (presenters.size <= 1) null else startButton.backButtonState
                }

        this.onChildAddRecursive.subscribe {
            when (it) {
                is ShowListPresenter -> {
                    it.showClickSubject
                            .whileIn(it, Lifecycle.State.ALIVE)
                            .subscribe {
                                this.pushWithReveal(
                                        ShowDetailPresenter::class.java,
                                        (ShowDetailPresenter::showId.name with it.id)
                                                and (Presenter::id.name with it.id.toInt())
                                )
                            }
                }
                is SeriesPresenter -> {
                    it.episodeClicks
                            .whileIn(it, Lifecycle.State.ALIVE)
                            .subscribe {
                                Timber.d("Episode clicked!")
                                this.pushWithReveal(
                                        EpisodeDetailPresenter::class.java,
                                        (Presenter::id.name with R.id.episode_detail) and
                                                (EpisodeDetailPresenter::episodeName.name with it.name)
                                )
                            }
                }
            }
            if (it is ShowListPresenter) {
            }
        }
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (arguments.isFresh()) {
            attach(R.id.root_content, ShowListPresenter::class.java)
        }
    }

    override fun onBackPressed(): Boolean {
        //Only the stack top can override our pop action
        return presenters.last().onBackPressed() || popWithHide()
    }

    private fun <P:Presenter> pushWithReveal(clazz: Class<P>, arguments: Bundle = Bundle()) {
        Observable.fromCallable {
            this.attach(R.id.root_content, clazz, arguments)
        }.finishAnimation {
            it.view.alpha = 0f
            it.view.scaleX = 0.6f
            it.view.scaleY = 0.6f
            it.view.translationY = 800f
            it.view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(Duration.ENTER)
                    .setInterpolator(LinearOutSlowInInterpolator())
        }.postToThis()
                .asResult()
                .subscribe {
                    //NOTE: You don't want this in production. This will silently swallow
                    //any error in your app, confusing the user.
                    if (it is Result.Error)
                        Timber.e(it.error)
                    else
                        Timber.d("Push result: $it")
                }
    }

    private fun popWithHide(): Boolean {
        return if (presenters.size > 1) {
            Observable.fromCallable {
                val stack = presenters
                if (stack.size == 1) throw IllegalStateException("Cannot pop")
                stack.last()
            }.finishAnimation {
                it.view.animate()
                        .alpha(0f)
                        .scaleX(0.7f)
                        .scaleY(0.7f)
                        .translationY(400f)
                        .setDuration(Duration.LEAVE)
                        .setInterpolator(FastOutLinearInInterpolator())
            }.doOnNext{ detach(it) }
                .postToThis()
                .asResult()
                .subscribe {
                    //NOTE: You don't want this in production. This will silently swallow
                    //any error in your app, confusing the user.
                    if (it is Result.Error)
                        Timber.e(it.error)
                    else
                        Timber.d("Pop result: $it")
            }
            true
        } else false
    }


}*/