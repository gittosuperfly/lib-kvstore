package cai.lib.kvstore

abstract class BaseStore {

    fun apply() {
        KVStore.find(javaClass).apply()
    }

    fun commit() {
        KVStore.find(javaClass).commit()
    }

}
