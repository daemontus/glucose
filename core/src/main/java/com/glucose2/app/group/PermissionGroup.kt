package com.glucose2.app.group

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.view.ViewGroup
import android.widget.Toast
import com.glucose2.app.*
import com.glucose2.bundle.parcelableBundler
import com.glucose2.rx.subscribeWhile
import com.glucose2.state.StateOptional
import io.reactivex.android.schedulers.AndroidSchedulers

class PermissionGroup(
        private val view: ViewGroup,
        private val showIfSuccess: Class<out Component>?,
        private val permissions: Array<String>,
        private val presenter: Presenter
) : ComponentGroup<Unit>(presenter) {

    private val PERMISSION_REQUEST_CODE = 0x123

    private var componentState: Bundle? by StateOptional(parcelableBundler())

    private var attached: Component? = null

    private var isStarted: Boolean = false
    private var isResumed: Boolean = false

    private fun updateToPermission() {
        val missingPermission = permissions.firstOrNull { permission ->
            val status = activity.checkPermission(permission, Process.myPid(), Process.myUid())
            status != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermission == null) {
            if (showIfSuccess != null && attached == null) {
                val component = presenter.host.factory.obtain(showIfSuccess, view)
                component.attach(this, Unit, componentState ?: Bundle())
            }
        } else {
            // maybe user just took away our permission, in which case, remove this shit!
            attached?.let { remove ->
                remove.detach()
                presenter.host.factory.recycle(remove)
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || activity.shouldShowRequestPermissionRationale(missingPermission)) {
                //TODO - add error component!
                Toast.makeText(presenter.host.activity, "WTF Dude?", Toast.LENGTH_SHORT).show()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(arrayOf(missingPermission), PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun attach(state: Bundle) {
        super.attach(state)
        presenter.observeAction(PermissionResultAction::class.java)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWhile(presenter.attached)
                .subscribe {
                    updateToPermission()
                }
        updateToPermission()
    }

    override fun detach(): Bundle {
        attached?.let { detaching ->
            componentState = detaching.detach()
            presenter.host.factory.recycle(detaching)
        }
        return super.detach()
    }

    override fun saveInstanceState(): Bundle {
        componentState = attached?.saveInstanceState()
        return super.saveInstanceState()
    }

    override fun addChild(child: Component, location: Unit) {
        super.addChild(child, location)
        view.addView(child.view)
    }

    override fun attachChild(child: Component) {
        super.attachChild(child)
        attached = child
        // sync lifecycle
        if (child is Presenter) {
            if (child.isResumed && !this.isResumed) child.pause()
            if (child.isStarted && !this.isStarted) child.stop()
            if (!child.isStarted && this.isStarted) child.start()
            if (!child.isResumed && this.isResumed) child.resume()
        }
    }

    override fun detachChild(child: Component) {
        attached = null
        super.detachChild(child)
    }

    override fun removeChild(child: Component) {
        view.removeView(child.view)
        super.removeChild(child)
    }

    override fun configurationChange(newConfig: Configuration) {
        super.configurationChange(newConfig)
        //TODO Config change
    }

    override fun start() {
        super.start()
        updateToPermission()    // maybe user deleted out permission, right?
        isStarted = true
        (attached as? Presenter)?.start()
    }

    override fun resume() {
        super.resume()
        isResumed = true
        (attached as? Presenter)?.resume()
    }

    override fun pause() {
        (attached as? Presenter)?.pause()
        isResumed = false
        super.pause()
    }

    override fun stop() {
        (attached as? Presenter)?.stop()
        isStarted = false
        super.stop()
    }

}