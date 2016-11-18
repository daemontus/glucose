package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import com.github.daemontus.glucose.core.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class BundleUtilsTest {

    private val host = MockPresenterHost(setupEmptyActivity())

    @Test
    fun bundleUtils_nativeArguments() {
        val p = object : SimplePresenter(host) {
            val boolArgument by NativeState(false, booleanBundler)
            val byteArgument by NativeState(-1, byteBundler)
            val charArgument by NativeState('a', charBundler)
            val doubleArgument by NativeState(-1.0, doubleBundler)
            val floatArgument by NativeState(-1.0f, floatBundler)
            val intArgument by NativeState(-1, intBundler)
            val longArgument by NativeState(-1, longBundler)
            val shortArgument by NativeState(-1, shortBundler)

            var boolState by NativeState(false, booleanBundler)
            var byteState by NativeState(-1, byteBundler)
            var charState by NativeState('a', charBundler)
            var doubleState by NativeState(-1.0, doubleBundler)
            var floatState by NativeState(-1.0f, floatBundler)
            var intState by NativeState(-1, intBundler)
            var longState by NativeState(-1, longBundler)
            var shortState by NativeState(-1, shortBundler)
        }

        //default values
        p.performAttach(Bundle())

        assertEquals(false, p.boolArgument)
        assertEquals(-1, p.byteArgument)
        assertEquals('a', p.charArgument)
        assertEquals(-1.0, p.doubleArgument)
        assertEquals(-1.0f, p.floatArgument)
        assertEquals(-1, p.intArgument)
        assertEquals(-1, p.longArgument)
        assertEquals(-1, p.shortArgument)

        assertEquals(false, p.boolState)
        assertEquals(-1, p.byteState)
        assertEquals('a', p.charState)
        assertEquals(-1.0, p.doubleState)
        assertEquals(-1.0f, p.floatState)
        assertEquals(-1, p.intState)
        assertEquals(-1, p.longState)
        assertEquals(-1, p.shortState)

        p.performDetach()

        //external values
        p.performAttach(
                ("boolArgument" with true)
                        and ("byteArgument" with 12.toByte())
                        and ("charArgument" with 'f')
                        and ("doubleArgument" with 12.0)
                        and ("floatArgument" with 12.0f)
                        and ("intArgument" with 12.toInt())
                        and ("longArgument" with 12.toLong())
                        and ("shortArgument" with 12.toShort())
                        and ("boolState" with true)
                        and ("byteState" with 12.toByte())
                        and ("charState" with 'f')
                        and ("doubleState" with 12.0)
                        and ("floatState" with 12.0f)
                        and ("intState" with 12.toInt())
                        and ("longState" with 12.toLong())
                        and ("shortState" with 12.toShort())
        )

        assertEquals(true, p.boolArgument)
        assertEquals(12, p.byteArgument)
        assertEquals('f', p.charArgument)
        assertEquals(12.0, p.doubleArgument)
        assertEquals(12.0f, p.floatArgument)
        assertEquals(12, p.intArgument)
        assertEquals(12, p.longArgument)
        assertEquals(12, p.shortArgument)

        assertEquals(true, p.boolState)
        assertEquals(12, p.byteState)
        assertEquals('f', p.charState)
        assertEquals(12.0, p.doubleState)
        assertEquals(12.0f, p.floatState)
        assertEquals(12, p.intState)
        assertEquals(12, p.longState)
        assertEquals(12, p.shortState)

        //change state
        p.boolState = false
        p.byteState = 4
        p.charState = 'r'
        p.doubleState = 4.0
        p.floatState = 4.0f
        p.intState = 4
        p.longState = 4
        p.shortState = 4


        assertEquals(false, p.boolState)
        assertEquals(4, p.byteState)
        assertEquals('r', p.charState)
        assertEquals(4.0, p.doubleState)
        assertEquals(4.0f, p.floatState)
        assertEquals(4, p.intState)
        assertEquals(4, p.longState)
        assertEquals(4, p.shortState)

        //check that the state change is preserved

        val state = Bundle().apply {
            p.onSaveInstanceState(this)
        }
        p.performDetach()
        p.performAttach(state)

        assertEquals(true, p.boolArgument)
        assertEquals(12, p.byteArgument)
        assertEquals('f', p.charArgument)
        assertEquals(12.0, p.doubleArgument)
        assertEquals(12.0f, p.floatArgument)
        assertEquals(12, p.intArgument)
        assertEquals(12, p.longArgument)
        assertEquals(12, p.shortArgument)

        assertEquals(false, p.boolState)
        assertEquals(4, p.byteState)
        assertEquals('r', p.charState)
        assertEquals(4.0, p.doubleState)
        assertEquals(4.0f, p.floatState)
        assertEquals(4, p.intState)
        assertEquals(4, p.longState)
        assertEquals(4, p.shortState)
    }


    @Test
    fun bundleUtils_requiredArguments() {
        val p = object : SimplePresenter(host) {
            val charSequenceArgument by State(charSequenceBundler)
            val stringArgument by State(stringBundler)
            val bundleArgument by State(bundleBundler)
            val serializableArgument by State(serializableBundler)
            val parcelableArgument by State(parcelableBundler<Bundle>())
            val sparseParcelableArgument by State(sparseParcelableArrayBundler<Bundle>())

            var charSequenceState by State(charSequenceBundler)
            var stringState by State(stringBundler)
            var bundleState by State(bundleBundler)
            var serializableState by State(serializableBundler)
            var parcelableState by State(parcelableBundler<Bundle>())
            var sparseParcelableState by State(sparseParcelableArrayBundler<Bundle>())
        }

        val string1 = "abc"
        val string2 = "def"
        val bundle1 = Bundle().apply { this.putInt("test", 1) }
        val bundle2 = Bundle().apply { this.putInt("test", 2) }
        //can't access internal
        val parcel1 = bundle1//PresenterParcel("class1", bundle1, 1)
        val parcel2 = bundle2//PresenterParcel("class2", bundle2, 2)
        val parcelArray1 = SparseArray<Bundle>().apply { this.put(1, parcel1) }
        val parcelArray2 = SparseArray<Bundle>().apply { this.put(2, parcel2) }

        //Missing instanceState
        p.performAttach(Bundle())

        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.charSequenceArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.stringArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(bundle1, p.bundleArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.serializableArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(parcel1, p.parcelableArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(parcelArray1, p.sparseParcelableArgument)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.charSequenceState)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.stringState)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(bundle1, p.bundleState)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(string1, p.serializableState)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(parcel1, p.parcelableState)
        }
        assertFailsWith<NullPointerException> {
            assertEquals(parcelArray1, p.sparseParcelableState)
        }

        p.performDetach()

        //Valid instanceState
        p.performAttach(bundle("charSequenceArgument" with string1) and
                ("stringArgument" with string1) and
                ("bundleArgument" with bundle1) and
                ("serializableArgument" with string1 as Serializable) and
                ("parcelableArgument" with parcel1) and
                ("sparseParcelableArgument" with parcelArray1) and
                ("charSequenceState" with string1 as CharSequence) and
                ("stringState" with string1) and
                ("bundleState" with bundle1) and
                ("serializableState" with string1 as Serializable) and
                ("parcelableState" with parcel1 as Parcelable) and
                ("sparseParcelableState" with parcelArray1)
        )

        assertEquals(string1, p.charSequenceArgument)
        assertEquals(string1, p.stringArgument)
        assertEquals(bundle1, p.bundleArgument)
        assertEquals(string1, p.serializableArgument)
        assertEquals(parcel1, p.parcelableArgument)
        assertEquals(parcelArray1, p.sparseParcelableArgument)
        assertEquals(string1, p.charSequenceState)
        assertEquals(string1, p.stringState)
        assertEquals(bundle1, p.bundleState)
        assertEquals(string1, p.serializableState)
        assertEquals(parcel1, p.parcelableState)
        assertEquals(parcelArray1, p.sparseParcelableState)

        //Change state
        p.charSequenceState = string2
        p.stringState = string2
        p.bundleState = bundle2
        p.serializableState = string2
        p.parcelableState = parcel2
        p.sparseParcelableState = parcelArray2

        assertEquals(string2, p.charSequenceState)
        assertEquals(string2, p.stringState)
        assertEquals(bundle2, p.bundleState)
        assertEquals(string2, p.serializableState)
        assertEquals(parcel2, p.parcelableState)
        assertEquals(parcelArray2, p.sparseParcelableState)

        val state = Bundle().apply { p.onSaveInstanceState(this) }

        p.performDetach()
        p.performAttach(state)

        assertEquals(string1, p.charSequenceArgument)
        assertEquals(string1, p.stringArgument)
        assertEquals(bundle1, p.bundleArgument)
        assertEquals(string1, p.serializableArgument)
        assertEquals(parcel1, p.parcelableArgument)
        assertEquals(parcelArray1, p.sparseParcelableArgument)
        assertEquals(string2, p.charSequenceState)
        assertEquals(string2, p.stringState)
        assertEquals(bundle2, p.bundleState)
        assertEquals(string2, p.serializableState)
        assertEquals(parcel2, p.parcelableState)
        assertEquals(parcelArray2, p.sparseParcelableState)

        p.performDetach()
    }

    @Test
    fun bundleUtils_optionalArgument() {
        val p = object : SimplePresenter(host) {
            val optionalArgument by OptionalState(stringBundler)
            var optionalState by OptionalState(stringBundler)
        }

        //Empty instanceState
        p.performAttach(Bundle())
        assertEquals(null, p.optionalArgument)
        assertEquals(null, p.optionalState)
        p.performDetach()

        val string1 = "abc"
        val string2 = "def"

        //Valid instanceState
        p.performAttach(("optionalArgument" with string1) and ("optionalState" with string2))
        assertEquals(string1, p.optionalArgument)
        assertEquals(string2, p.optionalState)

        //Change state
        p.optionalState = null
        assertEquals(null, p.optionalState)

        val state = Bundle().apply { p.onSaveInstanceState(this) }

        p.performDetach()
        p.performAttach(state)

        assertEquals(string1, p.optionalArgument)
        assertEquals(null, p.optionalState)

        p.performDetach()
    }


}