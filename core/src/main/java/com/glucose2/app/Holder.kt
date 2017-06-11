package com.glucose2.app

import android.view.View
import com.glucose2.app.transaction.TransactionHost

/**
 * Holder is a very simple class intended to display mainly static, small pieces of information,
 * such as items in adapters.
 *
 * It cannot react to the main lifecycle directly. The only immediate callbacks are
 * [AbstractComponent.onDataBind], [AbstractComponent.onDataReset],
 * [AbstractComponent.onAttach] and [AbstractComponent.onDetach]
 *
 * However, it an still communicate using the [EventHost].
 */
open class Holder(
        view: View,
        host: ComponentHost
) : AbstractComponent(view, host), TransactionHost by host