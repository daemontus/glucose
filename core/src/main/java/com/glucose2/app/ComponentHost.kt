package com.glucose2.app

import com.glucose2.app.transaction.TransactionHost

interface ComponentHost : ComponentFactory, TransactionHost {



}


interface ComponentFactory {
    fun <T: Component> obtain(clazz: Class<T>): T
    fun recycle(holder: Component)
}