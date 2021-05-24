package cai.lib.kvstore

import org.junit.Test

import org.junit.Assert.*
import java.lang.IllegalArgumentException

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun testFunction() {
        var typeKey = "caiyufei@"
        println("typeKey = $typeKey")
        val hz = "@" + typeKey.split("@").last()
        println("分隔符 = $hz")
        typeKey = typeKey.removeSuffix(hz)
        println("结果 = $typeKey")

    }


    /**
     * 输入TypeKey,获取原始Key
     */
    private fun getRealKey(typeKey: String?): String {


        return ""
    }


    /**
     * 获取带类型的Key
     *
     * @param realKey 原始Key值
     * @return 带类型的Key
     */
    private inline fun <reified T> getTypeKey(realKey: String?): String {
        val typeSuffix = "@" + T::class.simpleName
        return realKey + typeSuffix
    }

}