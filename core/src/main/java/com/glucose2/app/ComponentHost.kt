package com.glucose2.app

import com.glucose2.app.transaction.TransactionHost

interface ComponentHost : HolderFactory, TransactionHost {



}


interface HolderFactory {
    fun <T: Holder> obtain(clazz: Class<T>): T
    fun recycle(holder: Holder)
}