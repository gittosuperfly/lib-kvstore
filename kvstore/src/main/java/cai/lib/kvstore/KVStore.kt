@file:Suppress("unused")

package cai.lib.kvstore

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import cai.lib.kvstore.annotations.FieldIgnore
import cai.lib.kvstore.annotations.FieldRename
import cai.lib.kvstore.annotations.StoreRename
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.tencent.mmkv.MMKV
import java.lang.reflect.Field

/**
 *
 */

class KVStore internal constructor(clazz: Class<*>) {

    // 绑定的具体实体类
    internal var entity: BaseStore

    // 所有的可操作变量
    private var fields = mutableMapOf<String, Field>()

    // 绑定的SharedPreference实例
    private var preferences: SharedPreferences

    // 存储待同步的数据的key值
    private var modifierKeys = mutableListOf<String>()

    // 是否使用Gson的Rename
    private var isGsonRename = false

    init {
        if (BaseStore::class.java.isAssignableFrom(clazz).not()) {
            throw RuntimeException("[KVStore] The class [${clazz.simpleName}] must be subclass of BaseStore")
        }

        entity = clazz.newInstance() as BaseStore
        isGsonRename = sIsGsonAnnotation
        preferences = MMKVProxy(
            MMKV.mmkvWithID(
                getValid(clazz.getAnnotation(StoreRename::class.java)?.value, clazz.simpleName),
                MMKV.SINGLE_PROCESS_MODE
            )
        )
        var type = clazz
        while (type != BaseStore::class.java) {
            for (field in type.declaredFields) {
                if (field.isAnnotationPresent(FieldIgnore::class.java)) {
                    // 指定过滤此字段
                    continue
                }

                val key = if (isGsonRename) {
                    getValid(field.getAnnotation(SerializedName::class.java)?.value, field.name)
                } else {
                    getValid(field.getAnnotation(FieldRename::class.java)?.value, field.name)
                }

                if (!fields.containsKey(key)) {
                    // 对于父类、子类均存在的字段。使用子类的数据进行存储
                    fields[key] = field
                    if (!field.isAccessible) {
                        field.isAccessible = true
                    }
                }
            }
            type = type.superclass
        }
        read()
    }

    private val handler: Handler = Handler(thread.looper) { msg ->
        return@Handler when (msg.what) {
            READ -> {
                // 更新指定字段的数据
                synchronized(modifierKeys) {
                    val keys = modifierKeys.toTypedArray()
                    modifierKeys.clear()
                    val map = preferences.all
                    for (key in keys) {
                        val field = fields[key]
                        val value = map[key]
                        if (field == null || value == null) continue

                        readSingle(field, value)
                    }
                }
                true
            }
            WRITE -> {
                write()
                true
            }
            else -> false
        }
    }


    // 从SP中读取数据。注入到实体类中。
    private fun read() {
        synchronized(this) {
            val map = preferences.all
            for ((name, field) in fields) {
                readSingle(field, map[name])
            }
        }
    }

    private fun readSingle(field: Field, value: Any?) {
        if (value == null) return

        val type: Class<*> = field.type
        try {
            val result: Any? = when {
                type == Int::class.java -> value as Int
                type == Long::class.java -> value as Long
                type == Boolean::class.java -> value as Boolean
                type == Float::class.java -> value as Float
                type == String::class.java -> value as String
                type == Byte::class.java -> (value as String).toByte()
                type == Short::class.java -> (value as String).toShort()
                type == Char::class.java -> (value as String).toCharArray()[0]
                type == Double::class.java -> (value as String).toDouble()
                type == StringBuilder::class.java -> StringBuilder(value as String)
                type == StringBuffer::class.java -> StringBuffer(value as String)
                GSON -> Gson().fromJson(value as String, type)
                else -> null
            }

            result?.let { field.set(entity, it) }
        } catch (e: ClassCastException) {
            // ignore 只过滤此类异常。其他异常正常抛出
            Log.e(TAG, "readSingle : ${e.message}")
        }
    }

    // 将实体类中的数据。注入到SP容器中。
    private fun write() {
        synchronized(this) {
            val editor = preferences.edit()
            for ((name, field) in fields) {
                val value = field.get(entity)
                val type = field.type
                when {
                    type == Int::class.java -> editor.putInt(name, value as? Int ?: 0)
                    type == Long::class.java -> editor.putLong(name, value as? Long ?: 0L)
                    type == Boolean::class.java -> editor.putBoolean(
                        name,
                        value as? Boolean ?: false
                    )
                    type == Float::class.java -> editor.putFloat(name, value as? Float ?: 0f)
                    type == String::class.java -> editor.putString(name, value as? String ?: "")
                    type == Byte::class.java
                            || type == Char::class.java
                            || type == Double::class.java
                            || type == Short::class.java
                            || type == StringBuilder::class.java
                            || type == StringBuffer::class.java
                    -> editor.putString(name, value!!.toString())
                    GSON -> value?.let { editor.putString(name, Gson().toJson(it)) }
                }
            }
            editor.apply()
        }
    }

    /**
     * 将类中的数据同步到SP容器中去。(在子线程中进行)
     */
    fun apply() {
        if (handler.hasMessages(WRITE)) return
        handler.sendEmptyMessageDelayed(WRITE, 100)
        handler.apply { }
    }

    /**
     * 将类中的数据同步到SP容器中去。(同步执行)
     */
    fun commit() {
        write()
    }

    private fun getValid(value: String?, default: String): String =
        if (value.isNullOrEmpty()) default else value

    companion object {

        private const val READ = 1
        private const val WRITE = 2

        private const val TAG = "KVStore"

        // 缓存容器
        private val container = mutableMapOf<Class<*>, KVStore>()

        private var sIsGsonAnnotation = false

        // 后台刷新线程
        private val thread: HandlerThread by lazy {
            val thread = HandlerThread("shared_update_thread")
            thread.start()
            return@lazy thread
        }

        private val GSON by lazy { return@lazy exist("com.google.gson.Gson") }

        private fun exist(name: String): Boolean = try {
            Class.forName(name)
            true
        } catch (e: Exception) {
            false
        }

        /**
         * 初始化框架
         */
        fun init(application: Application) {
            MMKV.initialize(application)
        }

        /**
         * 加载储存类
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> load(clazz: Class<T>): T {
            synchronized(container) {
                container[clazz]?.let { return it.entity as T }
                val instance = KVStore(clazz)
                container[clazz] = instance
                return instance.entity as T
            }
        }

        internal fun find(clazz: Class<*>): KVStore {
            if (container[clazz] != null) {
                return container[clazz]!!
            } else {
                throw RuntimeException("Could not find KVStore by this clazz:[${clazz.canonicalName}]")
            }
        }

        /**
         * 使用Gson的 [SerializedName] 来对变量储存重命名，而不是KVStore提供的 [FieldRename]
         */
        fun isSerializedName(use: Boolean) {
            sIsGsonAnnotation = use
        }
    }
}
