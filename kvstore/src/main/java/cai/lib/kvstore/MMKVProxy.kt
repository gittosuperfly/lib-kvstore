package cai.lib.kvstore

import android.content.SharedPreferences
import com.tencent.mmkv.MMKV
import java.lang.IllegalArgumentException

/**
 * MMKV代理类
 *
 * 实现了getAll方法，该方法MMKV中不实现
 * 数据储存key定制，分为 realKey, typeKey 两类
 * typeKey = realKey@type，如 userName -> userName@String
 *
 * 以下方法不做实现:
 * getStringSet、putStringSet、
 * contains、remove、
 * registerOnSharedPreferenceChangeListener、unregisterOnSharedPreferenceChangeListener
 */
internal class MMKVProxy(private val mmkv: MMKV?) : SharedPreferences, SharedPreferences.Editor {

    companion object {
        //分隔符
        private const val SEPARATOR = "@"
    }

    override fun getAll(): MutableMap<String, *> {
        val allTypeKeys = mmkv?.allKeys()
        val allMap = mutableMapOf<String, Any>()
        allTypeKeys?.forEach { typeKey ->
            val realKey = getRealKey(typeKey)
            when (getTypeName(typeKey)) {
                String::class.simpleName -> allMap[realKey] = getString(realKey, "") ?: ""
                Int::class.simpleName -> allMap[realKey] = getInt(realKey, 0)
                Long::class.simpleName -> allMap[realKey] = getLong(realKey, 0L)
                Float::class.simpleName -> allMap[realKey] = getFloat(realKey, 0f)
                Boolean::class.simpleName -> allMap[realKey] = getBoolean(realKey, false)
            }
        }
        return allMap
    }

    override fun getString(key: String?, defValue: String?): String? {
        val typeKey = getTypeKey<String>(key)
        return mmkv?.getString(typeKey, defValue)
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun getInt(key: String?, defValue: Int): Int {
        val typeKey = getTypeKey<Int>(key)
        return mmkv?.getInt(typeKey, defValue) ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long {
        val typeKey = getTypeKey<Long>(key)
        return mmkv?.getLong(typeKey, defValue) ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        val typeKey = getTypeKey<Float>(key)
        return mmkv?.getFloat(typeKey, defValue) ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        val typeKey = getTypeKey<Boolean>(key)
        return mmkv?.getBoolean(typeKey, defValue) ?: defValue
    }

    override fun contains(key: String?): Boolean {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun edit(): SharedPreferences.Editor {
        return this
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun putString(key: String?, value: String?): SharedPreferences.Editor? {
        val typeKey = getTypeKey<String>(key)
        return mmkv?.putString(typeKey, value)
    }

    override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor? {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor? {
        val typeKey = getTypeKey<Int>(key)
        return mmkv?.putInt(typeKey, value)
    }

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor? {
        val typeKey = getTypeKey<Long>(key)
        return mmkv?.putLong(typeKey, value)
    }

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor? {
        val typeKey = getTypeKey<Float>(key)
        return mmkv?.putFloat(typeKey, value)
    }

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor? {
        val typeKey = getTypeKey<Boolean>(key)
        return mmkv?.putBoolean(typeKey, value)
    }

    override fun remove(key: String?): SharedPreferences.Editor? {
        throw UnsupportedOperationException("Not implement in KVStore")
    }

    override fun clear(): SharedPreferences.Editor? {
        return mmkv?.clear()
    }

    override fun commit(): Boolean {
        return mmkv?.commit() ?: false
    }

    override fun apply() {
        mmkv?.apply()
    }

    private inline fun <reified T> getTypeKey(realKey: String?): String {
        val typeSuffix = SEPARATOR + T::class.simpleName
        return realKey + typeSuffix
    }

    private fun getRealKey(typeKey: String?): String {
        val suffix = SEPARATOR + typeKey!!.split(SEPARATOR).last()
        return typeKey.removeSuffix(suffix)
    }

    private fun getTypeName(typeKey: String?): String = typeKey!!.split(SEPARATOR).last()

}
