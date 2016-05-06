package glucose

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

open class AttachedFragment<A: Activity> : InjectableFragment() {

    var ctx: A? = null

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        ctx = context as A
    }

    override fun onDetach() {
        super.onDetach()
        ctx = null
    }

}

open class InjectableActivity: FragmentActivity() {

    protected val inject: BaseInjectable = BaseInjectable()

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        inject.onConfigChanged()
    }

}

open class InjectableFragment : Fragment() {

    val inject = BaseInjectable()

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inject.onViewChanged()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        inject.onConfigChanged()
    }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        inject.onArgsChanged()
    }
}

interface Injectable {
    val lastViewUpdate: Long
    val lastConfigUpdate: Long
    val lastArgumentsUpdate: Long
}

class BaseInjectable : Injectable {

    override val lastViewUpdate: Long
        get() = mView
    override val lastConfigUpdate: Long
        get() = mConfig
    override val lastArgumentsUpdate: Long
        get() = mArgs

    private var mView: Long = -1
    private var mConfig: Long = -1
    private var mArgs: Long = -1

    fun onViewChanged(): Unit {
        mView = System.currentTimeMillis()
    }

    fun onConfigChanged(): Unit {
        mConfig = System.currentTimeMillis()
    }

    fun onArgsChanged(): Unit {
        mArgs = System.currentTimeMillis()
    }
}

/** Finders **/

fun Fragment.findDimen(id: Int): Float =
        resources.getDimension(id)

fun Fragment.findInt(id: Int): Int =
        resources.getInteger(id)

fun Fragment.findColor(id: Int): Int =
        resources.getColor(id)

fun Fragment.findLongArg(name: String): Long? =
        arguments?.getLong(name)

fun Fragment.findStringArg(name: String): String? =
        arguments?.getString(name)

fun <T: Parcelable> Fragment.findParcelableArg(name: String): T?
        = arguments?.getParcelable(name)

fun <T: Parcelable> Fragment.findParcelableArrayListArg(name: String): ArrayList<T>?
        = arguments?.getParcelableArrayList(name)

@Suppress("UNCHECKED_CAST") fun <T: View> Fragment.findView(id: Int): T =
        view!!.findViewById(id) as T

@Suppress("UNCHECKED_CAST") fun <T: View> Activity.findView(id: Int): T =
        findViewById(id) as T

/*@Suppress("UNCHECKED_CAST") fun <T: View> RecyclerView.ViewHolder.findView(id: Int): T =
        itemView.findViewById(id)*/

/** Injecting arguments **/
fun Fragment.bindStringArgument(name: String, inj: Injectable) : ReadOnlyProperty<Fragment, String>
        = requiredDynamic(name, { findStringArg(name) }, { inj.lastArgumentsUpdate })

fun InjectableFragment.bindStringArgument(name: String) : ReadOnlyProperty<InjectableFragment, String>
        = bindStringArgument(name, this.inject)

fun Fragment.bindLongArgument(name: String, inj: Injectable) : ReadOnlyProperty<Fragment, Long>
        = requiredDynamic(name, { findLongArg(name) }, { inj.lastArgumentsUpdate })

fun InjectableFragment.bindLongArgument(name: String) : ReadOnlyProperty<InjectableFragment, Long>
        = bindLongArgument(name, this.inject)

fun <T: Parcelable> Fragment.bindParcelableArgument(name: String, inj: Injectable) : ReadOnlyProperty<Fragment, T>
        = requiredDynamic(name, { findParcelableArg(name) }, { inj.lastArgumentsUpdate })

fun <T: Parcelable> InjectableFragment.bindParcelableArgument(name: String) : ReadOnlyProperty<InjectableFragment, T>
        = bindParcelableArgument(name, this.inject)

fun <T: Parcelable> Fragment.bindParcelableArrayListArgument(name: String, inj: Injectable) : ReadOnlyProperty<Fragment, ArrayList<T>>
        = requiredDynamic(name, { findParcelableArrayListArg(name) }, { inj.lastArgumentsUpdate })

fun <T: Parcelable> InjectableFragment.bindParcelableArrayListArgument(name: String) : ReadOnlyProperty<InjectableFragment, ArrayList<T>>
        = bindParcelableArrayListArgument(name, this.inject)

/** Injecting views **/

fun <V : View> Fragment.bindView(id: Int, inj: Injectable): ReadOnlyProperty<Fragment, V>
        = requiredDynamic(id, { findView(id) }, { inj.lastViewUpdate })

fun <V : View> InjectableFragment.bindView(id: Int): ReadOnlyProperty<Fragment, V> = bindView(id, this.inject)

/*fun <T: View> RecyclerView.ViewHolder.bindView(id: Int): ReadOnlyProperty<RecyclerView.ViewHolder, T>
        = requiredStatic(id, { findView(id) })*/

fun <V: View> Activity.bindStaticView(id: Int): ReadOnlyProperty<Activity, V> =
        requiredStatic(id, { findView(id) })

/** Injecting integers **/

fun InjectableFragment.bindInteger(id: Int) = bindInteger(id, this.inject)

fun Fragment.bindInteger(id: Int, inj: Injectable): ReadOnlyProperty<InjectableFragment, Int>
        = requiredDynamic(id, { findInt(id) }, { inj.lastConfigUpdate })

/** Injecting dimensions **/

fun InjectableFragment.bindDimen(id: Int) = bindDimen(id, this.inject)

fun Fragment.bindDimen(id: Int, inj: Injectable): ReadOnlyProperty<InjectableFragment, Float>
        = requiredDynamic(id, { findDimen(id) }, { inj.lastConfigUpdate })

fun InjectableFragment.bindIntDimen(id: Int) = bindIntDimen(id, this.inject)

fun Fragment.bindIntDimen(id: Int, inj: Injectable): ReadOnlyProperty<InjectableFragment, Int>
        = requiredDynamic(id, { findDimen(id).toInt() }, { inj.lastConfigUpdate })

/** Injecting colors **/

fun InjectableFragment.bindColor(id: Int) = bindColor(id, this.inject)

fun Fragment.bindColor(id: Int, inj: Injectable): ReadOnlyProperty<InjectableFragment, Int>
        = requiredDynamic(id, { findColor(id) }, { inj.lastConfigUpdate })


private fun <T, V : Any> requiredStatic(id: Int, finder: T.(Int) -> V?)
        = requiredDynamic(id, finder, { -1 })

private fun <T, K, V : Any> requiredDynamic(
        id: K,
        finder: T.(K) -> V?,
        key: T.() -> Long
) = CheckedLazy({ f: T, desc -> f.finder(id) ?: notFound(id, desc) }, key)

private fun <T> notFound(id: T, desc: KProperty<*>): Nothing =
        throw IllegalStateException("Resource with ID $id for '${desc.name}' not found.")

private class CheckedLazy<T, V>(
        private val initializer : (T, KProperty<*>) -> V,
        private val currentKey: T.() -> Long
) : ReadOnlyProperty<T, V> {
    private object EMPTY
    private var value: Any? = EMPTY
    private var key: Long = -1

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY || key != thisRef.currentKey()) {
            key = thisRef.currentKey()
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }
}

