package cai.lib.kvstore.annotations

/**
 * 重命名Store
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD)
annotation class StoreRename(val value: String = "")
