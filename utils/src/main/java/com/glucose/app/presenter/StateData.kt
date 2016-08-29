package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import com.glucose.app.Presenter


internal class ChildState(
        val clazz: Class<out Presenter>,
        val tree: Bundle,
        val map: SparseArray<Bundle>,
        val viewState: SparseArray<Parcelable>
)

internal fun Presenter.saveWholeState(): ChildState {
    val map = SparseArray<Bundle>()
    val tree = this.saveHierarchyState(map)
    val views = SparseArray<Parcelable>()
    this.view.saveHierarchyState(views)
    return ChildState(this.javaClass, tree, map, views)
}

/**
 * A parcelable representation of a [Presenter]s state.
 */
internal data class PresenterParcel(
        val clazz: String,
        val state: Bundle
) : Parcelable {

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(clazz)
        p0.writeParcelable(state, p1)
    }

    override fun describeContents(): Int = 0

    companion object {

        @Suppress("unused")
        @JvmField val CREATOR: Parcelable.Creator<PresenterParcel> = object : Parcelable.Creator<PresenterParcel> {
            override fun createFromParcel(p0: Parcel): PresenterParcel
                    = PresenterParcel(p0.readString(), p0.readParcelable(Bundle::class.java.classLoader))

            override fun newArray(p0: Int): Array<out PresenterParcel?> = arrayOfNulls(p0)

        }
    }

}