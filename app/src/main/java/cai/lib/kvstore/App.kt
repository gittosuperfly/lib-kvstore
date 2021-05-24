package cai.lib.kvstore

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        KVStore.init(this)
        KVStore.isSerializedName(true)
    }
}