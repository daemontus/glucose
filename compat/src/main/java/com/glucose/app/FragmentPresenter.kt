package com.glucose.app

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.widget.FrameLayout
import com.glucose.app.presenter.*
import com.glucose.util.newSyntheticId

/**
 * FragmentPresenter allows you to insert a fully functioning [Fragment]
 * into your presenter hierarchy as a leaf of the presenter tree.
 *
 * To make sure all functionality works properly, the fragment is added
 * to a [FragmentManager]. You can provide the manager in the constructor
 * (default will use the manager provided by the host [FragmentActivity]).
 * The only requirement is that the provided manager is able to access
 * views of this presenter (after it has been added to the main tree).
 *
 * Also if you are using this in combination with [PresenterFragment], you
 * probably want to use the child fragment manager.
 *
 * To ensure proper fragment state restoration, the parent view has always
 * a new, unique, synthetic ID, so that the [FragmentManager] shouldn't be able
 * to restore the state and position of the fragment on it's own. Instead,
 * FragmentPresenter will use [Fragment.setInitialSavedState] to properly
 * inject the saved state into the Fragment every time the Presenter is started.
 *
 * To configure the presenter, use [fragmentClass], [fragmentArguments] and
 * possibly also [fragmentState].
 */
open class FragmentPresenter(
        host: PresenterHost,
        private val fragmentManager: FragmentManager = (host.activity as FragmentActivity).supportFragmentManager
) : Presenter(host, FrameLayout(host.activity)) {

    val fragmentClass by State(serializableBundler)
    val fragmentArguments by OptionalState(bundleBundler)
    var fragmentState by OptionalState(parcelableBundler<Fragment.SavedState>())

    var fragment: Fragment? = null
        private set

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        view.id = newSyntheticId()
    }

    override fun onStart() {
        super.onStart()
        //Here we have a guarantee that the root view is in the hierarchy,
        //so the fragment manager will find it.
        if (fragment == null) {
            //do only during the first start after attach, otherwise the fragment
            //should be retained.
            val clazz = Fragment::class.java.javaClass.cast(fragmentClass)
            val fragment = clazz.newInstance()
            this.fragment = fragment
            fragment.setInitialSavedState(fragmentState)
            fragment.arguments = fragmentArguments
            fragmentManager.beginTransaction()
                    .add(view.id, fragment)
                    .commitNowAllowingStateLoss()
        }
    }

    override fun onSaveInstanceState(out: Bundle) {
        fragment?.let {
            fragmentState = fragmentManager.saveFragmentInstanceState(it)
        }
        super.onSaveInstanceState(out)
    }

    override fun onDetach() {
        fragment?.let {
            //fragment can be null if we are detached before starting
            fragmentState = fragmentManager.saveFragmentInstanceState(it)
            fragmentManager.beginTransaction()
                    .remove(it)
                    .commitNowAllowingStateLoss()
        }
        fragment = null
        super.onDetach()
    }
}