package com.glucose.app


/**
 * A Fragment implementation which provides a basic context for planting a viable
 * Presenter tree.
 *
 * There are two ways of creating a valid instance of [PresenterFragment], mainly in relation
 * to the root presenter class which will be displayed in the Presenter tree.
 *
 * You can either subclass it and call the constructor with [rootPresenter] and [rootArguments]
 * (just as you would an the [RootCompatActivity]):
 *
 *     class MyFragment() : PresenterFragment(MyPresenter::class.java, bundle("data" with 42))
 *
 * However, if you don't wish to implement any special features, you can provide this
 * data using the fragment arguments, using the [ROOT_PRESENTER_ARGS_KEY] and
 * [ROOT_PRESENTER_CLASS_KEY] keys:
 *
 *     val fragment = PresenterFragment()
 *     fragment.arguments = (PresenterFragment.ROOT_PRESENTER_CLASS_KEY with MyPresenter::class.java) and
 *                 (PresenterFragment.ROOT_PRESENTER_ARGS_KEY with myCustomArgs)
 *
 * (In both cases, the arguments are optional, just as everywhere else)
 *
 * Other usage is similar to the [RootCompatActivity], but due to the limitations of Fragments,
 * some functionality is not guaranteed.
 *
 * Known problems:
 * 1. Fragments don't receive onBackPressed and onTrimMemory callbacks. If you need this
 * functionality, you have implement your own notification mechanism. (The methods are provided,
 * you just have to call them yourself)
 * 2. You have to make sure state of the fragment is properly restored (the state of presenter will
 * be saved into the bundle in [onSaveInstanceState], just make sure it's not lost).
 * 3. If the fragment is used as a nested fragment, you have to make sure activity results and
 * permission results are delivered to that fragment.
 *//*
open class PresenterFragment(
        private var rootPresenter: Class<out Presenter>? = null,
        private var rootArguments: Bundle? = null
) : Fragment() {

    companion object {
        @JvmField val ROOT_PRESENTER_CLASS_KEY = "glucose:root_presenter_class"
        @JvmField val ROOT_PRESENTER_ARGS_KEY = "glucose:root_presenter_args"
    }

    protected var presenterContext: PresenterDelegate? = null
        private set

    private var savedState: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedState = savedInstanceState
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context !is Activity) {
            throw LifecycleException("PresenterFragment cannot be attached to $context which is not an Activity")
        }
        val clazz = rootPresenter ?:
            Presenter::class.java.javaClass.cast(arguments.getSerializable(ROOT_PRESENTER_CLASS_KEY))
        val args = rootArguments ?: arguments.getBundle(ROOT_PRESENTER_ARGS_KEY) ?: Bundle()
        presenterContext = PresenterDelegate(context, clazz, args)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return presenterContext!!.onCreate(savedState)
    }

    override fun onStart() {
        super.onStart()
        presenterContext!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenterContext!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenterContext!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        presenterContext!!.onStop()
    }

    override fun onDestroyView() {
        //have to do it here, because the presenters shouldn't outlive the UI
        presenterContext!!.onDestroy()
        presenterContext = null
        super.onDestroyView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        presenterContext?.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterContext?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenterContext?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenterContext?.onSaveInstanceState(outState)
    }

    fun onBackPressed(): Boolean {
        return presenterContext?.onBackPressed() ?: false
    }

    fun onTrimMemory(level: Int) {
        presenterContext?.onTrimMemory(level)
    }
}*/