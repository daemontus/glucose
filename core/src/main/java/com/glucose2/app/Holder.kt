package com.glucose2.app

import android.os.Bundle
import android.view.View
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.booleanBundler
import com.glucose.app.presenter.intBundler
import com.glucose2.app.transaction.TransactionHost
import rx.Subscription
import rx.subscriptions.CompositeSubscription


open class Holder(
        val view: View,
        val host: PresenterHost
) : Attachable<Presenter>, Bindable<Bundle>, TransactionHost by host {



    /* ========== Configuration properties ============ */

    /**
     * Default: true. When false, the holder won't be saved into the parent's hierarchy bundle.
     *
     * Use this to safely provide holders inside adapters (holder won't be recreated
     * automatically, but rather by the adapter).
     *
     * Note that the state is still placed into the ID-State map, therefore if the adapter
     * restores the presenter using the same unique ID, the state can still be preserved.
     *
     * (Note that the view state is not restored, because this happens only once, right after
     * initial hierarchy inflation when adapter holders are usually not instantiated yet.
     * But this is a problem for all adapters)
     **/
    val saveIntoHierarchy by NativeState(true, booleanBundler)

    /**
     * Default: true. When false, the holder will be recreated upon configuration change.
     *
     * Use this to implement Holders that rely heavily on configuration and can't be simply
     * updated. Note that when recreated, the state of the holder is saved (including the
     * view hierarchy), similar to the full Activity restart. However, if this is a [Presenter],
     * all its children will also be recreated (since we don't know if they are still present
     * after the configuration change).
     **/
    val surviveConfigChange by NativeState(true, booleanBundler)

    /**
     * Default: [View.NO_ID]. Uniquely identifies this holder in the hierarchy.
     *
     * The ID is used to match this presenter when saving and restoring state.
     * In general, if you just want to restore the Holder in the same place,
     * all you need to do is place it into a [ViewGroup] with a fixed ID.
     * However, if the position of the holder can change after the state
     * restore, you have to also provide a proper Holder ID based on which
     * the identity of the new Holder can be determined.
     **/
    val id by NativeState(View.NO_ID, intBundler)



    /* ========== Attachable<Presenter> ============ */

    private var _parent: Presenter? = null

    override val parent: Presenter
        get() = _parent ?: throw LifecycleException("Holder ($this) is not attached to presenter.")

    override val isAttached: Boolean
        get() = _parent != null

    private val whileAttached = CompositeSubscription()

    override fun Subscription.whileAttached(): Subscription {
        if (isAttached) {
            whileAttached.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    /* ========== Attachable lifecycle ============ */

    /**
     * Only update the parent value. Presenter is responsible for placing this holder into the
     * view hierarchy BEFORE calling this.
     */
    internal open fun performAttach(parent: Presenter) {
        if (isAttached) {
            throw LifecycleException("Holder ($this) is already attached to ${this.parent}.")
        }
        onAttach(parent)
        if (!isAttached) {
            throw LifecycleException("Super.onAttach hasn't been called properly in $this.")
        }
    }

    /**
     * Only update parent value. Presenter is responsible for removing this holder from the
     * view hierarchy AFTER calling this.
     */
    internal open fun performDetach() {
        if (!isAttached) {
            throw LifecycleException("Holder ($this) is not attached to anything.")
        }
        onDetach()
        if (isAttached) {
            throw LifecycleException("Super.onDetach hasn't been called properly in $this.")
        }
    }

    /**
     * The holder is connected to the view hierarchy before onAttach is called.
     * After super.onAttach returns, the [parent] property should not be null.
     */
    protected open fun onAttach(parent: Presenter) {
        this._parent = parent
    }

    /**
     * After super.onDetach returns, the [parent] property should be null.
     * The holder is removed from the view hierarchy after onDetach is called.
     */
    protected open fun onDetach() {
        whileAttached.clear()
        this._parent = null
    }



}