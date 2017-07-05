package com.github.daemontus.glucose.demo.presentation

/*
class EpisodeDetailPresenter(context: PresenterHost, parent: ViewGroup?)
    : PresenterGroup(context, R.layout.presenter_episode_detail, parent) {

    val episodeName by State(stringBundler)

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        findView<TextView>(R.id.episode_title).text = episodeName
        if (arguments.isFresh()) {
            attach(R.id.episode_data, PersistentPresenter::class.java)
            attach(R.id.episode_data, RecreatedPresenter::class.java)
            attach(R.id.episode_data, NoRecyclePresenter::class.java)
            attach(R.id.episode_data, SimpleFragmentPresenter::class.java,
                    bundle(FragmentPresenter::fragmentClass.name with SimpleFragment::class.java)
            )
            attach(R.id.episode_data, NestedFragmentPresenter::class.java,
                    bundle( FragmentPresenter::fragmentClass.name with NestedPresenterTree::class.java  ) and (
                            FragmentPresenter::fragmentArguments.name with (
                                bundle( PresenterFragment.ROOT_PRESENTER_CLASS_KEY with PersistentPresenter::class.java) and (
                                        PresenterFragment.ROOT_PRESENTER_ARGS_KEY with
                                                bundle(
                                                        PersistentPresenter::text.name with
                                                        "And this is a Presenter inside a Fragment, inside a Presenter!"
                                                )
                                )
                            )
                    )
            )
        }
    }
}

class PersistentPresenter(context: PresenterHost, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    val text by OptionalState(stringBundler)

    override val canChangeConfiguration: Boolean = true

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        findView<TextView>(R.id.data_title).text = text ?: "canChangeConfiguration = true"
    }

}

class RecreatedPresenter(context: PresenterHost, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    override val canChangeConfiguration: Boolean = false

    init {
        findView<TextView>(R.id.data_title).text = "canChangeConfiguration = false"
    }
}

class NoRecyclePresenter(context: PresenterHost, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    override val canBeReused: Boolean = false

    init {
        findView<TextView>(R.id.data_title).text = "canBeReused = false"
    }
}

class SimpleFragmentPresenter(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : FragmentPresenter(
        host, (host.activity as AppCompatActivity).supportFragmentManager
)

class NestedFragmentPresenter(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : FragmentPresenter(
        host, (host.activity as AppCompatActivity).supportFragmentManager
)*/